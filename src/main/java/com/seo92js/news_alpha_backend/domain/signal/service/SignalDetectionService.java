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
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    private static final int SIMILAR_TOP_K = 12;

    /**
     * 벡터 검색 결과를 같은 이슈 군집으로 볼 최소 유사도
     * 높일수록 정밀도는 올라가고, 낮출수록 더 넓은 관련 뉴스를 포함
     */
    private static final double SIMILARITY_THRESHOLD = 0.82d;

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

    private final VectorStoreService vectorStoreService;
    private final NewsRepository newsRepository;
    private final SignalRepository signalRepository;
    private final SignalEvidenceRepository signalEvidenceRepository;
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

        for (News news : discoveredNews) {
            try {
                buildSignalCandidate(stock, keyword, news, discoveredNewsIds)
                        .filter(candidate -> !signalRepository.existsBySignalKey(candidate.signal().getSignalKey()))
                        .ifPresent(candidate -> transactionTemplate.executeWithoutResult(status -> {
                            Signal savedSignal = signalRepository.save(candidate.signal());
                            saveEvidences(savedSignal, candidate.cluster());
                        }));
            } catch (Exception e) {
                log.warn("시그널 탐지에 실패했습니다. newsId={}, keyword={}", news.getId(), keyword, e);
            }
        }
    }

    /**
     * 단일 뉴스를 기준으로 시그널 후보를 만들고 최소 군집 크기와 burst 조건을 검증
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
        String title = cluster.get(0).title();
        Signal signal = Signal.of(
                signalKey,
                stock,
                SignalType.EMERGING_CLUSTER,
                keyword,
                title,
                buildSummary(keyword, cluster.size(), burstCount, firstPublishedAt, lastPublishedAt),
                score,
                cluster.size(),
                firstPublishedAt,
                lastPublishedAt,
                LocalDateTime.now()
        );

        return Optional.of(new SignalCandidate(signal, cluster));
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
                resolvePublishedAt(seedNews)
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

        if (newsId == null || publishedAt == null || keyword == null || title == null) {
            return Optional.empty();
        }

        return Optional.of(new ClusteredNews(newsId, keyword, title, url, publishedAt));
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
        double volumeScore = Math.min(55d, clusterSize * 15d);
        double burstScore = Math.min(25d, burstCount * 8d);
        double freshnessScore = Math.max(5d, 25d - (ageMinutes / 15d));
        return Math.round(Math.min(100d, volumeScore + burstScore + freshnessScore) * 10d) / 10d;
    }

    /**
     * LLM 보고서 생성 전 기본 시그널 요약 문구 생성
     */
    private String buildSummary(String keyword, int clusterSize, long burstCount, LocalDateTime firstPublishedAt, LocalDateTime lastPublishedAt) {
        return "%s 관련 유사 뉴스 %d건이 최근 %d시간 내에 집중적으로 포착되었습니다. 최근 %d시간 내 급증 기사 %d건을 기반으로 형성된 초기 시그널입니다."
                .formatted(keyword, clusterSize, RECENT_WINDOW_HOURS, BURST_WINDOW_HOURS, burstCount);
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
            LocalDateTime publishedAt
    ) {}

    private record SignalCandidate(
            Signal signal,
            List<ClusteredNews> cluster
    ) {}
}
