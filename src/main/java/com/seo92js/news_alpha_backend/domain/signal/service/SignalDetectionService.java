package com.seo92js.news_alpha_backend.domain.signal.service;

import com.seo92js.news_alpha_backend.domain.ai.dto.NewsMetadata;
import com.seo92js.news_alpha_backend.domain.ai.service.VectorStoreService;
import com.seo92js.news_alpha_backend.domain.news.News;
import com.seo92js.news_alpha_backend.domain.news.repository.NewsRepository;
import com.seo92js.news_alpha_backend.domain.signal.Signal;
import com.seo92js.news_alpha_backend.domain.signal.SignalEvidence;
import com.seo92js.news_alpha_backend.domain.signal.SignalType;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalEvidenceRepository;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalRepository;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalDetectionService {

    /**
     * seed 뉴스 1건으로 유사도 검색 시 가져올 최대 chunk 수
     * 너무 작으면 관련 뉴스를 놓치고, 너무 크면 비용과 중복 후보가 늘어남
     */
    private static final int SIMILAR_TOP_K = 6;

    /**
     * 벡터 검색 결과를 같은 이슈 군집으로 볼 최소 유사도
     * 높일수록 정밀도는 올라가고, 낮출수록 더 넓은 관련 뉴스를 포함
     */
    private static final double SIMILARITY_THRESHOLD = 0.70d;

    /**
     * 시그널로 인정하기 위한 최소 뉴스 건수
     * 단일 기사나 단순 중복 기사만으로 시그널이 생성되는 것을 방지
     */
    private static final int MIN_CLUSTER_NEWS_COUNT = 3;

    /**
     * 시그널 후보 군집에 포함할 최근 뉴스 조회 범위
     * 현재는 최근 24시간 내 유사 뉴스만 하나의 시그널 후보로 묶음
     */
    private static final int RECENT_WINDOW_HOURS = 24;

    /**
     * 짧은 시간 내 기사 집중도를 판단하는 burst 범위
     * 이 범위 안에 일정 수 이상의 뉴스가 있어야 초기 급등 시그널로 봄
     */
    private static final int BURST_WINDOW_HOURS = 6;

    /**
     * 한 키워드 수집 실행에서 LLM 분석 및 저장까지 진행할 최대 시그널 수
     * 후보를 먼저 모은 뒤 상위 후보만 분석해서 LLM 비용 상한을 둠
     */
    private static final int KEYWORD_SIGNAL_LIMIT = 3;

    /**
     * 두 후보의 근거 뉴스가 이 비율 이상 겹치면 같은 이슈로 판단
     * 작은 군집 크기를 분모로 사용해 중복 seed에서 생기는 유사 후보를 제거
     */
    private static final double CLUSTER_OVERLAP_THRESHOLD = 0.5d;

    /**
     * LLM 시그널 분석에 전달할 뉴스별 본문 preview 최대 길이
     * 전체 본문을 넣지 않고 핵심 문맥만 제공해 토큰 사용량을 제한
     */
    private static final int ANALYSIS_NEWS_PREVIEW_MAX_LENGTH = 300;

    /**
     * 저장 직전 같은 종목의 최근 시그널과 비교할 조회 범위
     * 키워드가 달라도 같은 사건이면 새 시그널로 저장하지 않기 위함
     */
    private static final int DUPLICATE_LOOKBACK_HOURS = 24;

    /**
     * 저장 직전 비교할 최근 시그널 최대 수
     */
    private static final int DUPLICATE_COMPARE_LIMIT = 30;

    /**
     * 제목/요약 토큰이 이 비율 이상 겹치면 같은 사건으로 판단
     */
    private static final double TEXT_OVERLAP_THRESHOLD = 0.40d;

    /**
     * 정확한 근거 뉴스가 이 비율 이상 겹치면 같은 사건으로 판단
     * 서로 다른 매체의 같은 이슈 기사가 많아질 수 있어 리포트 후보 overlap보다 낮게 둠
     */
    private static final double RECENT_EVIDENCE_OVERLAP_THRESHOLD = 0.35d;

    private final VectorStoreService vectorStoreService;
    private final NewsRepository newsRepository;
    private final SignalRepository signalRepository;
    private final SignalEvidenceRepository signalEvidenceRepository;
    private final SignalAnalysisService signalAnalysisService;
    private final SignalSimilarityPolicy signalSimilarityPolicy;
    private final TransactionTemplate transactionTemplate;

    /**
     * 수집 키워드에서 발견된 뉴스를 seed로 삼아 유사 뉴스 군집을 찾고 시그널, 근거를 저장
     */
    public void detect(Stock stock, String keyword, List<News> discoveredNews) {
        if (stock == null) {
            return;
        }
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        if (discoveredNews == null || discoveredNews.isEmpty()) {
            return;
        }

        Set<Long> discoveredNewsIds = discoveredNews.stream()
                .map(News::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<SignalCandidate> candidates = new ArrayList<>();
        for (News news : discoveredNews) {
            try {
                buildSignalCandidate(stock, keyword, news, discoveredNewsIds)
                        .ifPresent(candidates::add);
            } catch (Exception e) {
                log.warn("시그널 탐지에 실패했습니다. newsId={}, keyword={}", news.getId(), keyword, e);
            }
        }

        selectSignalCandidates(candidates).forEach(candidate -> {
            if (hasSimilarRecentSignal(stock, toComparableText(candidate), toNewsIds(candidate.cluster()))) {
                log.info(
                        "유사한 최근 시그널이 있어 저장 스킵. stockId={}, keyword={}, title={}",
                        stock.getId(),
                        keyword,
                        candidate.defaultTitle()
                );
                return;
            }
            Signal signal = createSignal(stock, keyword, candidate);
            transactionTemplate.executeWithoutResult(status -> {
                Signal savedSignal = signalRepository.save(signal);
                saveEvidences(savedSignal, candidate.cluster());
            });
        });
    }

    /**
     * 단일 뉴스를 기준으로 LLM 호출 전 시그널 후보를 만들고 최소 군집 크기와 burst 조건을 검증
     */
    private Optional<SignalCandidate> buildSignalCandidate(Stock stock, String keyword, News seedNews, Set<Long> discoveredNewsIds) {
        if (seedNews.getId() == null || seedNews.getContent() == null || seedNews.getContent().isBlank()) {
            return Optional.empty();
        }

        List<ClusteredNews> cluster = buildCluster(seedNews, keyword, discoveredNewsIds);
        if (cluster.size() < MIN_CLUSTER_NEWS_COUNT) {
            return Optional.empty();
        }

        long burstCount = cluster.stream()
                .filter(item -> !item.publishedAt().isBefore(LocalDateTime.now().minusHours(BURST_WINDOW_HOURS)))
                .count();
        if (burstCount < 2) {
            return Optional.empty();
        }

        LocalDateTime firstPublishedAt = cluster.stream()
                .map(ClusteredNews::publishedAt)
                .min(LocalDateTime::compareTo)
                .orElse(resolvePublishedAt(seedNews));
        LocalDateTime lastPublishedAt = cluster.stream()
                .map(ClusteredNews::publishedAt)
                .max(LocalDateTime::compareTo)
                .orElse(resolvePublishedAt(seedNews));

        double score = calculateScore(cluster.size(), burstCount, lastPublishedAt);
        String signalKey = createSignalKey(stock, keyword, cluster);
        String defaultTitle = cluster.get(0).title();
        String defaultSummary = buildSummary(keyword, cluster.size(), burstCount);

        return Optional.of(new SignalCandidate(
                signalKey,
                defaultTitle,
                defaultSummary,
                score,
                burstCount,
                firstPublishedAt,
                lastPublishedAt,
                cluster
        ));
    }

    /**
     * 후보를 점수순으로 정렬하고 DB 중복, 실행 내 signalKey 중복, 근거 뉴스 overlap 중복을 제거한 뒤 상위 후보만 선택
     */
    private List<SignalCandidate> selectSignalCandidates(List<SignalCandidate> candidates) {
        if (candidates.isEmpty()) {
            return List.of();
        }

        Set<String> existingSignalKeys = signalRepository.findExistingSignalKeys(
                candidates.stream()
                        .map(SignalCandidate::signalKey)
                        .collect(Collectors.toSet())
        );

        List<SignalCandidate> selected = new ArrayList<>();
        Set<String> selectedSignalKeys = new HashSet<>();

        List<SignalCandidate> sortedCandidates = candidates.stream()
                .filter(candidate -> !existingSignalKeys.contains(candidate.signalKey()))
                .sorted(Comparator.comparing(SignalCandidate::score).reversed()
                        .thenComparing(SignalCandidate::lastPublishedAt, Comparator.reverseOrder()))
                .toList();

        for (SignalCandidate candidate : sortedCandidates) {
            if (selected.size() >= KEYWORD_SIGNAL_LIMIT) {
                break;
            }
            if (!selectedSignalKeys.add(candidate.signalKey())) {
                continue;
            }
            if (hasOverlappedCluster(selected, candidate)) {
                continue;
            }
            selected.add(candidate);
        }

        return selected;
    }

    private boolean hasOverlappedCluster(List<SignalCandidate> selected, SignalCandidate candidate) {
        return selected.stream()
                .anyMatch(selectedCandidate -> calculateClusterOverlap(selectedCandidate.cluster(), candidate.cluster()) >= CLUSTER_OVERLAP_THRESHOLD);
    }

    private double calculateClusterOverlap(List<ClusteredNews> first, List<ClusteredNews> second) {
        Set<Long> firstNewsIds = first.stream()
                .map(ClusteredNews::newsId)
                .collect(Collectors.toSet());
        Set<Long> secondNewsIds = second.stream()
                .map(ClusteredNews::newsId)
                .collect(Collectors.toSet());
        long intersectionCount = firstNewsIds.stream()
                .filter(secondNewsIds::contains)
                .count();
        int smallerClusterSize = Math.min(firstNewsIds.size(), secondNewsIds.size());
        if (smallerClusterSize == 0) {
            return 0d;
        }
        return (double) intersectionCount / smallerClusterSize;
    }

    /**
     * 키워드가 달라도 같은 종목에서 최근에 이미 저장된 의미상 유사 시그널은 중복 저장하지 않음
     */
    private boolean hasSimilarRecentSignal(Stock stock, String comparableText, Set<Long> candidateNewsIds) {
        List<Signal> recentSignals = signalRepository.findRecentSignalsByStockId(
                stock.getId(),
                LocalDateTime.now().minusHours(DUPLICATE_LOOKBACK_HOURS),
                PageRequest.of(0, DUPLICATE_COMPARE_LIMIT)
        );
        if (recentSignals.isEmpty()) {
            return false;
        }

        Map<Long, Set<Long>> evidenceNewsIdsBySignalId = signalEvidenceRepository.findEvidenceRowsBySignalIds(
                        recentSignals.stream().map(Signal::getId).toList()
                )
                .stream()
                .collect(Collectors.groupingBy(
                        row -> row.signalId(),
                        Collectors.mapping(row -> row.newsId(), Collectors.toSet())
                ));

        return recentSignals.stream()
                .anyMatch(existingSignal -> {
                    if (signalSimilarityPolicy.hasNewsOverlap(
                            candidateNewsIds,
                            evidenceNewsIdsBySignalId.getOrDefault(existingSignal.getId(), Set.of()),
                            RECENT_EVIDENCE_OVERLAP_THRESHOLD
                    )) {
                        return true;
                    }

                    return signalSimilarityPolicy.isTextSimilar(
                            comparableText,
                            toComparableText(existingSignal),
                            TEXT_OVERLAP_THRESHOLD
                    );
                });
    }

    private Set<Long> toNewsIds(List<ClusteredNews> cluster) {
        return cluster.stream()
                .map(ClusteredNews::newsId)
                .collect(Collectors.toSet());
    }

    private String toComparableText(SignalCandidate candidate) {
        String clusterText = candidate.cluster().stream()
                .map(item -> item.title() + " " + item.preview())
                .collect(Collectors.joining(" "));
        return "%s %s %s".formatted(candidate.defaultTitle(), candidate.defaultSummary(), clusterText);
    }

    private String toComparableText(Signal signal) {
        return "%s %s %s".formatted(
                signal.getTitle(),
                signal.getSummary(),
                signal.getInvestorSummary() == null ? "" : signal.getInvestorSummary()
        );
    }

    /**
     * 선택된 후보에 대해서만 LLM 분석을 수행하고 저장 가능한 Signal 엔티티 생성
     */
    private Signal createSignal(Stock stock, String keyword, SignalCandidate candidate) {
        SignalAnalysisResult analysis = signalAnalysisService.analyze(
                stock,
                keyword,
                candidate.defaultTitle(),
                candidate.defaultSummary(),
                candidate.cluster().size(),
                candidate.burstCount(),
                toAnalysisNews(candidate.cluster())
        );

        return Signal.of(
                candidate.signalKey(),
                stock,
                SignalType.EMERGING_CLUSTER,
                keyword,
                analysis.title(),
                analysis.summary(),
                analysis.eventType(),
                analysis.sentiment(),
                analysis.confidence(),
                analysis.investorSummary(),
                candidate.score(),
                candidate.cluster().size(),
                candidate.firstPublishedAt(),
                candidate.lastPublishedAt(),
                LocalDateTime.now()
        );
    }

    /**
     * LLM 분석에 필요한 근거 뉴스 정보를 최신순으로 변환
     */
    private List<SignalAnalysisNews> toAnalysisNews(List<ClusteredNews> cluster) {
        return cluster.stream()
                .map(item -> new SignalAnalysisNews(item.title(), item.url(), item.publishedAt(), item.preview()))
                .toList();
    }

    /**
     * 시그널에 연결된 근거 뉴스 목록을 SignalEvidence로 저장
     */
    private List<SignalEvidence> saveEvidences(Signal signal, List<ClusteredNews> cluster) {
        List<News> evidenceNews = newsRepository.findAllById(
                cluster.stream()
                        .map(ClusteredNews::newsId)
                        .toList()
        );
        Map<Long, News> newsById = evidenceNews.stream()
                .collect(Collectors.toMap(News::getId, news -> news));

        List<SignalEvidence> evidences = new ArrayList<>();
        for (int i = 0; i < cluster.size(); i++) {
            ClusteredNews item = cluster.get(i);
            News news = newsById.get(item.newsId());
            if (news != null) {
                evidences.add(SignalEvidence.of(signal, news.getId(), news.getTitle(), news.getLink(), resolvePublishedAt(news), i + 1));
            }
        }

        return signalEvidenceRepository.saveAll(evidences);
    }

    /**
     * seed 뉴스와 의미가 비슷한 최근 뉴스들을 벡터 검색으로 찾아 뉴스 단위 군집 생성
     */
    private List<ClusteredNews> buildCluster(News seedNews, String keyword, Set<Long> discoveredNewsIds) {
        Map<Long, ClusteredNews> cluster = new LinkedHashMap<>();
        ClusteredNews seed = new ClusteredNews(
                seedNews.getId(),
                keyword,
                seedNews.getTitle(),
                seedNews.getLink(),
                resolvePublishedAt(seedNews),
                buildPreview(seedNews)
        );
        cluster.put(seed.newsId(), seed);

        List<Document> documents = vectorStoreService.similaritySearch(buildQuery(keyword, seedNews), SIMILAR_TOP_K, SIMILARITY_THRESHOLD);
        LocalDateTime windowStart = LocalDateTime.now().minusHours(RECENT_WINDOW_HOURS);

        for (Document document : documents) {
            toClusteredNews(document)
                    .filter(item -> discoveredNewsIds.contains(item.newsId()))
                    .filter(item -> !item.publishedAt().isBefore(windowStart))
                    .ifPresent(item -> cluster.putIfAbsent(item.newsId(), item));
        }

        return cluster.values().stream()
                .sorted(Comparator.comparing(ClusteredNews::publishedAt).reversed())
                .toList();
    }

    /**
     * 벡터 검색 결과 Document의 metadata를 시그널 군집용 뉴스 정보로 변환
     */
    private Optional<ClusteredNews> toClusteredNews(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        Long newsId = parseLong(metadata.get(NewsMetadata.Keys.ID));
        LocalDateTime publishedAt = parseDateTime(metadata.get(NewsMetadata.Keys.PUBLISHED_AT));
        String keyword = parseString(metadata.get(NewsMetadata.Keys.KEYWORD));
        String title = parseString(metadata.get(NewsMetadata.Keys.TITLE));
        String url = parseString(metadata.get(NewsMetadata.Keys.URL));
        String preview = truncate(document.getText(), ANALYSIS_NEWS_PREVIEW_MAX_LENGTH);

        if (newsId == null || publishedAt == null || keyword == null || title == null) {
            return Optional.empty();
        }

        return Optional.of(new ClusteredNews(newsId, keyword, title, url, publishedAt, preview));
    }

    /**
     * 유사 뉴스 검색에 사용할 query 문자열 생성
     */
    private String buildQuery(String keyword, News news) {
        List<String> fragments = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            fragments.add(keyword);
        }
        if (news.getTitle() != null && !news.getTitle().isBlank()) {
            fragments.add(news.getTitle());
        }
        if (news.getDescription() != null && !news.getDescription().isBlank()) {
            fragments.add(news.getDescription());
        }
        return String.join(" ", fragments);
    }

    /**
     * 뉴스 발행 시각이 없을 때 수집 시각 또는 현재 시각으로 대체
     */
    private LocalDateTime resolvePublishedAt(News news) {
        if (news.getPubDate() != null) {
            return news.getPubDate();
        }
        if (news.getCreatedAt() != null) {
            return news.getCreatedAt();
        }
        return LocalDateTime.now();
    }

    /**
     * 군집 크기, 최근 burst 수, 최신성을 기반으로 0~100 사이의 시그널 점수 계산
     */
    private double calculateScore(int clusterSize, long burstCount, LocalDateTime lastPublishedAt) {
        long ageMinutes = Math.max(0, Duration.between(lastPublishedAt, LocalDateTime.now()).toMinutes());
        double volumeScore = Math.min(40d, Math.log1p(clusterSize) * 18d);
        double burstScore = Math.min(30d, Math.log1p(burstCount) * 16d);
        double freshnessScore = Math.max(5d, 25d - (ageMinutes / 20d));
        return Math.round(Math.min(100d, volumeScore + burstScore + freshnessScore) * 10d) / 10d;
    }

    /**
     * LLM 보고서 생성 전 기본 시그널 요약 문구 생성
     */
    private String buildSummary(String keyword, int clusterSize, long burstCount) {
        return "%s 관련 유사 뉴스 %d건이 최근 %d시간 내에 집중적으로 포착되었습니다. 최근 %d시간 내 급증 기사 %d건을 기반으로 형성된 초기 시그널입니다."
                .formatted(keyword, clusterSize, RECENT_WINDOW_HOURS, BURST_WINDOW_HOURS, burstCount);
    }

    /**
     * seed 뉴스에서 LLM 분석용 짧은 본문 preview 생성
     */
    private String buildPreview(News news) {
        if (news.getDescription() != null && !news.getDescription().isBlank()) {
            return truncate(news.getDescription(), ANALYSIS_NEWS_PREVIEW_MAX_LENGTH);
        }
        if (news.getContent() != null && !news.getContent().isBlank()) {
            return truncate(news.getContent(), ANALYSIS_NEWS_PREVIEW_MAX_LENGTH);
        }
        return "";
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength);
    }

    /**
     * 동일한 뉴스 군집으로 중복 시그널이 생성되지 않도록 결정적 signalKey 생성
     */
    private String createSignalKey(Stock stock, String keyword, List<ClusteredNews> cluster) {
        String fingerprintSource = stock.getId() + ":" + keyword + ":" + cluster.stream()
                .map(item -> Long.toString(item.newsId()))
                .sorted()
                .limit(6)
                .collect(Collectors.joining(","));
        return UUID.nameUUIDFromBytes(fingerprintSource.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /**
     * metadata 값을 Long 타입으로 안전하게 변환
     */
    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * metadata 값을 LocalDateTime 타입으로 안전하게 변환
     */
    private LocalDateTime parseDateTime(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * metadata 값을 비어 있지 않은 문자열로 변환
     */
    private String parseString(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return text.isBlank() ? null : text;
    }

    private record ClusteredNews(
            Long newsId,
            String keyword,
            String title,
            String url,
            LocalDateTime publishedAt,
            String preview
    ) {}

    private record SignalCandidate(
            String signalKey,
            String defaultTitle,
            String defaultSummary,
            double score,
            long burstCount,
            LocalDateTime firstPublishedAt,
            LocalDateTime lastPublishedAt,
            List<ClusteredNews> cluster
    ) {}
}
