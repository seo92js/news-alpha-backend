package com.seo92js.news_alpha_backend.scheduler;

import com.seo92js.news_alpha_backend.domain.news.News;
import com.seo92js.news_alpha_backend.domain.news.service.NewsPipelineService;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockKeywordRepository;
import com.seo92js.news_alpha_backend.domain.stock.service.StockReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Profile("prod")
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
        Map<Stock, List<News>> accumulatedNewsByStock = new HashMap<>();

        for (StockKeyword stockKeyword : stockKeywords) {
            Stock stock = stockKeyword.getStock();
            try {
                // 종목을 처음 발견한 시점에 종목명 단독 기본 수집 쿼리 실행 (포괄성 확보)
                if (processedStocks.add(stock)) {
                    log.info("종목 기본 수집 쿼리 가동: stock={}", stock.getName());
                    StockKeyword defaultKeyword = StockKeyword.of(stock, "");
                    List<News> defaultNews = newsPipelineService.collectAndEmbed(defaultKeyword);
                    accumulatedNewsByStock.computeIfAbsent(stock, k -> new ArrayList<>()).addAll(defaultNews);
                    Thread.sleep(NEWS_COLLECTION_DELAY_MILLIS);
                }

                List<News> keywordNews = newsPipelineService.collectAndEmbed(stockKeyword);
                accumulatedNewsByStock.computeIfAbsent(stock, k -> new ArrayList<>()).addAll(keywordNews);

                // IP 차단 방지를 위한 딜레이
                Thread.sleep(NEWS_COLLECTION_DELAY_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn(
                        "종목 뉴스 분석 파이프라인 실행에 실패했습니다. stockId={}, keyword={}",
                        stock.getId(),
                        stockKeyword.getKeyword(),
                        e
                );
            }
        }

        for (Stock stock : processedStocks) {
            try {
                List<News> allNewsForStock = accumulatedNewsByStock.getOrDefault(stock, List.of());
                
                // 중복 발견된 뉴스 ID 제거
                List<News> uniqueNews = new ArrayList<>();
                Set<Long> seenIds = new HashSet<>();
                for (News n : allNewsForStock) {
                    if (n.getId() != null && seenIds.add(n.getId())) {
                        uniqueNews.add(n);
                    }
                }

                // 종목별 수집 완료된 모든 뉴스를 기반으로 일괄 시그널 탐지
                newsPipelineService.detectSignals(stock, uniqueNews);

                // 종목 리포트 생성
                stockReportService.generateLatestReport(stock);
            } catch (Exception e) {
                log.warn("종목 리포트 생성 및 시그널 탐지에 실패했습니다. stockId={}, stock={}", stock.getId(), stock.getName(), e);
            }
        }
    }
}
