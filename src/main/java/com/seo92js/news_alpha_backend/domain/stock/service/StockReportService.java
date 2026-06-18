package com.seo92js.news_alpha_backend.domain.stock.service;

import com.seo92js.news_alpha_backend.domain.ai.service.AiService;
import com.seo92js.news_alpha_backend.domain.signal.Signal;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceRow;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalEvidenceRepository;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalRepository;
import com.seo92js.news_alpha_backend.domain.signal.service.SignalSimilarityPolicy;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockReport;
import com.seo92js.news_alpha_backend.domain.stock.StockReportSignal;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportSignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockReportService {

    /**
     * 종목 리포트 생성 시 반영할 최대 시그널 수
     */
    private static final int MAX_SIGNALS = 5;

    /**
     * 리포트 후보는 넉넉히 가져온 뒤 중복 이슈를 제거하고 상위 5개만 사용
     */
    private static final int REPORT_CANDIDATE_SIGNAL_LIMIT = 20;

    /**
     * 종목 리포트 생성 시 참고할 최근 시그널 조회 기간
     */
    private static final int SIGNAL_LOOKBACK_HOURS = 24;

    /**
     * 리포트 안에서 같은 사건으로 볼 제목/요약 토큰 overlap 기준
     */
    private static final double REPORT_TEXT_OVERLAP_THRESHOLD = 0.40d;

    /**
     * 리포트 안에서 같은 사건으로 볼 근거 뉴스 overlap 기준
     */
    private static final double REPORT_EVIDENCE_OVERLAP_THRESHOLD = 0.35d;

    /**
     * LLM 프롬프트에 넣을 탐지 시각 출력 형식
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AiService aiService;
    private final SignalRepository signalRepository;
    private final SignalEvidenceRepository signalEvidenceRepository;
    private final SignalSimilarityPolicy signalSimilarityPolicy;
    private final StockReportRepository stockReportRepository;
    private final StockReportSignalRepository stockReportSignalRepository;
    private final TransactionTemplate transactionTemplate;

    @Value("classpath:/prompts/stock-report.st")
    private Resource stockReportPromptResource;

    /**
     * 종목에 연결된 최근 시그널들을 모아 종목별 종합 리포트 스냅샷을 새로 생성
     */
    public void generateLatestReport(Stock stock) {
        List<Signal> signals = transactionTemplate.execute(status -> {
            List<Signal> candidateSignals = signalRepository.findRecentSignalsByStockId(
                    stock.getId(),
                    LocalDateTime.now().minusHours(SIGNAL_LOOKBACK_HOURS),
                    PageRequest.of(0, REPORT_CANDIDATE_SIGNAL_LIMIT)
            );
            return selectRepresentativeSignals(candidateSignals);
        });

        if (signals == null || signals.isEmpty()) {
            return;
        }

        try {
            LocalDateTime generatedAt = LocalDateTime.now();
            LocalDate reportDate = generatedAt.toLocalDate();

            // DB 커넥션 미점유
            String report = aiService.chat(buildPrompt(stock, reportDate, signals));

            // 통신 성공 시에만
            transactionTemplate.executeWithoutResult(status -> {
                StockReport savedStockReport = stockReportRepository.save(
                        StockReport.of(stock, reportDate, signals.size(), report, generatedAt)
                );
                saveReportSignals(savedStockReport, signals);
            });
        } catch (Exception e) {
            log.warn("종목 리포트 생성에 실패했습니다. stockId={}, stock={}", stock.getId(), stock.getName(), e);
        }
    }

    private void saveReportSignals(StockReport stockReport, List<Signal> signals) {
        stockReportSignalRepository.saveAll(
                IntStream.range(0, signals.size())
                        .mapToObj(index -> StockReportSignal.of(stockReport, signals.get(index), index + 1))
                        .toList()
        );
    }

    private List<Signal> selectRepresentativeSignals(List<Signal> candidateSignals) {
        if (candidateSignals.isEmpty()) {
            return List.of();
        }

        Map<Long, Set<Long>> evidenceNewsIdsBySignalId = signalEvidenceRepository.findEvidenceRowsBySignalIds(
                        candidateSignals.stream().map(Signal::getId).toList()
                )
                .stream()
                .collect(Collectors.groupingBy(
                        SignalEvidenceRow::signalId,
                        Collectors.mapping(SignalEvidenceRow::newsId, Collectors.toSet())
                ));

        List<Signal> selected = new ArrayList<>();
        for (Signal candidate : candidateSignals) {
            if (selected.size() >= MAX_SIGNALS) {
                break;
            }
            if (isDuplicateReportSignal(selected, candidate, evidenceNewsIdsBySignalId)) {
                continue;
            }
            selected.add(candidate);
        }
        return selected;
    }

    private boolean isDuplicateReportSignal(
            List<Signal> selected,
            Signal candidate,
            Map<Long, Set<Long>> evidenceNewsIdsBySignalId
    ) {
        Set<Long> candidateNewsIds = evidenceNewsIdsBySignalId.getOrDefault(candidate.getId(), Set.of());
        String candidateText = toComparableText(candidate);

        return selected.stream()
                .anyMatch(existingSignal -> {
                    if (signalSimilarityPolicy.hasNewsOverlap(
                            candidateNewsIds,
                            evidenceNewsIdsBySignalId.getOrDefault(existingSignal.getId(), Set.of()),
                            REPORT_EVIDENCE_OVERLAP_THRESHOLD
                    )) {
                        return true;
                    }

                    return signalSimilarityPolicy.isTextSimilar(
                            candidateText,
                            toComparableText(existingSignal),
                            REPORT_TEXT_OVERLAP_THRESHOLD
                    );
                });
    }

    private String toComparableText(Signal signal) {
        return "%s %s %s".formatted(
                signal.getTitle(),
                signal.getSummary(),
                signal.getInvestorSummary() == null ? "" : signal.getInvestorSummary()
        );
    }

    private String buildPrompt(Stock stock, LocalDate reportDate, List<Signal> signals) {
        String signalBlock = signals.stream()
                .map(signal -> "- 제목: %s | 이벤트: %s | 방향성: %s | 확신도: %d | 점수: %.1f | 관련 기사 수: %d | 탐지 시각: %s | 요약: %s | 투자자 관점: %s"
                        .formatted(
                                signal.getTitle(),
                                signal.getEventType() == null ? "ETC" : signal.getEventType(),
                                signal.getSentiment() == null ? "NEUTRAL" : signal.getSentiment(),
                                signal.getConfidence() == null ? 50 : signal.getConfidence(),
                                signal.getScore(),
                                signal.getRelatedNewsCount(),
                                signal.getDetectedAt().format(DATE_TIME_FORMATTER),
                                signal.getSummary(),
                                signal.getInvestorSummary() == null ? signal.getSummary() : signal.getInvestorSummary()
                        ))
                .collect(Collectors.joining("\n"));

        PromptTemplate promptTemplate = new PromptTemplate(stockReportPromptResource);
        return promptTemplate.render(Map.of(
                "stockName", stock.getName(),
                "ticker", stock.getTicker(),
                "market", stock.getMarket(),
                "reportDate", reportDate.toString(),
                "signalBlock", signalBlock
        ));
    }
}
