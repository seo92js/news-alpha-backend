package com.seo92js.news_alpha_backend.domain.stock.service;

import com.seo92js.news_alpha_backend.domain.ai.service.AiService;
import com.seo92js.news_alpha_backend.domain.signal.Signal;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalRepository;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockReport;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockReportService {

    /**
     * 종목 리포트에 반영할 최대 시그널 수
     * 너무 많으면 LLM 입력이 길어지고 리포트 초점이 흐려져 상위 5개만 사용
     */
    private static final int MAX_SIGNALS = 5;

    /**
     * 종목 리포트 생성 시 참고할 시그널 조회 기간
     * 현재는 오늘 투자 판단에 필요한 최근 24시간 흐름을 기준으로 함
     */
    private static final int SIGNAL_LOOKBACK_HOURS = 24;

    /**
     * LLM 프롬프트에 넣을 탐지 시각 출력 형식
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AiService aiService;
    private final SignalRepository signalRepository;
    private final StockReportRepository stockReportRepository;

    @Value("classpath:/prompts/stock-report.st")
    private Resource stockReportPromptResource;

    /**
     * 종목에 연결된 최근 시그널들을 모아 종목별 종합 리포트를 생성하거나 갱신
     */
    @Transactional
    public void generateLatestReport(Stock stock) {
        List<Signal> signals = signalRepository.findRecentSignalsByStockId(
                stock.getId(),
                LocalDateTime.now().minusHours(SIGNAL_LOOKBACK_HOURS),
                PageRequest.of(0, MAX_SIGNALS)
        );

        if (signals.isEmpty()) {
            return;
        }

        try {
            LocalDate reportDate = LocalDate.now();
            String report = aiService.chat(buildPrompt(stock, reportDate, signals));
            StockReport stockReport = stockReportRepository.findByStockIdAndReportDate(stock.getId(), reportDate)
                    .orElseGet(() -> StockReport.of(stock, reportDate, signals.size(), report, LocalDateTime.now()));
            stockReport.update(signals.size(), report, LocalDateTime.now());
            stockReportRepository.save(stockReport);
        } catch (Exception e) {
            log.warn("종목 리포트 생성에 실패했습니다. stockId={}, stock={}", stock.getId(), stock.getName(), e);
        }
    }

    private String buildPrompt(Stock stock, LocalDate reportDate, List<Signal> signals) {
        String signalBlock = signals.stream()
                .map(signal -> "- 제목: %s | 점수: %.1f | 관련 기사 수: %d | 탐지 시각: %s | 요약: %s"
                        .formatted(
                                signal.getTitle(),
                                signal.getScore(),
                                signal.getRelatedNewsCount(),
                                signal.getDetectedAt().format(DATE_TIME_FORMATTER),
                                signal.getSummary()
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
