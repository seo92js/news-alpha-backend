package com.seo92js.news_alpha_backend.domain.news.service;

import com.seo92js.news_alpha_backend.domain.news.News;
import com.seo92js.news_alpha_backend.domain.signal.service.SignalDetectionService;
import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsPipelineService {

    private final NewsService newsService;
    private final NewsDocumentService newsDocumentService;
    private final SignalDetectionService signalDetectionService;

    /**
     * StockKeyword 기준으로 뉴스 수집, 신규 뉴스 임베딩 저장, 발견 뉴스 기반 시그널 탐지를 순서대로 실행
     */
    public List<News> collectEmbedAndDetect(StockKeyword stockKeyword) {
        String combinedQuery = stockKeyword.getStock().getName() + " " + stockKeyword.getKeyword();
        CollectedNewsResult collectedNews = newsService.collectNews(combinedQuery);
        if (collectedNews.discoveredNews().isEmpty()) {
            return collectedNews.discoveredNews();
        }

        newsDocumentService.process(collectedNews.newlySavedNews());
        signalDetectionService.detect(stockKeyword.getStock(), stockKeyword.getKeyword(), collectedNews.discoveredNews());
        log.info(
                "종목 뉴스 파이프라인 처리 완료. stock={}, keyword={}, discoveredCount={}, savedCount={}",
                stockKeyword.getStock().getName(),
                stockKeyword.getKeyword(),
                collectedNews.discoveredNews().size(),
                collectedNews.newlySavedNews().size()
        );
        return collectedNews.discoveredNews();
    }
}
