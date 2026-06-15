package com.seo92js.news_alpha_backend.scheduler;

import com.seo92js.news_alpha_backend.domain.ai.service.AiService;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockKeywordRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class WeeklyKeywordRefreshScheduler {

    private final StockRepository stockRepository;
    private final StockKeywordRepository stockKeywordRepository;
    private final AiService aiService;
    private final TransactionTemplate transactionTemplate;

    /**
     * 모든 종목의 연관 키워드를 최신화
     */
    @Scheduled(cron = "0 0 23 * * SUN", zone = "Asia/Seoul")
    public void refreshAllStockKeywords() {
        log.info("주간 종목 연관 키워드 자동 갱신 스케줄러 시작");
        List<Stock> stocks = stockRepository.findAll();

        if (stocks.isEmpty()) {
            return;
        }

        for (Stock stock : stocks) {
            try {
                List<String> newKeywords = aiService.generateKeywordsForStock(stock);

                transactionTemplate.executeWithoutResult(status -> {
                    stockKeywordRepository.deleteByStockId(stock.getId());
                    for (String keyword : newKeywords) {
                        stockKeywordRepository.save(StockKeyword.of(stock, keyword));
                    }
                });

                log.info("주간 키워드 갱신 완료. stock={}, keywords={}", stock.getName(), newKeywords);
            } catch (Exception e) {
                log.warn("종목 키워드 주간 갱신 실패. stock={}", stock.getName(), e);
            }
        }
    }
}
