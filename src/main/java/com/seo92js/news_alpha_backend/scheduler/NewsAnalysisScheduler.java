package com.seo92js.news_alpha_backend.scheduler;

import com.seo92js.news_alpha_backend.domain.news.service.NewsPipelineService;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockKeywordRepository;
import com.seo92js.news_alpha_backend.domain.stock.service.StockReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
//@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class NewsAnalysisScheduler {

    /**
     * 네이버 뉴스 상세 페이지 크롤링 사이 기본 대기 시간
     */
    private static final long NEWS_COLLECTION_DELAY_MILLIS = 500L;

    private final StockKeywordRepository stockKeywordRepository;
    private final NewsPipelineService newsPipelineService;
    private final StockReportService stockReportService;

    @PostConstruct
    public void init() {
        runStockAnalysisPipeline();
    }

    /**
     * StockKeyword 기준 수집부터 StockReport 생성까지 종목 뉴스 분석 파이프라인 실행
     */
    @Scheduled(cron = "0 0 8,10,12,14,16,18 * * MON-FRI")
    public void runStockAnalysisPipeline() {
        List<StockKeyword> stockKeywords = stockKeywordRepository.findEnabledWithStock();

        if (stockKeywords.isEmpty()) {
            return;
        }

        Set<Stock> processedStocks = new HashSet<>();

        for (StockKeyword stockKeyword : stockKeywords) {
            try {
                newsPipelineService.collectEmbedAndDetect(stockKeyword);
                processedStocks.add(stockKeyword.getStock());

                // IP 차단 방지를 위한 딜레이
                Thread.sleep(NEWS_COLLECTION_DELAY_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn(
                        "종목 뉴스 분석 파이프라인 실행에 실패했습니다. stockId={}, keyword={}",
                        stockKeyword.getStock().getId(),
                        stockKeyword.getKeyword(),
                        e
                );
            }
        }

        for (Stock stock : processedStocks) {
            try {
                stockReportService.generateLatestReport(stock);
            } catch (Exception e) {
                log.warn("종목 리포트 생성에 실패했습니다. stockId={}, stock={}", stock.getId(), stock.getName(), e);
            }
        }
    }
}
