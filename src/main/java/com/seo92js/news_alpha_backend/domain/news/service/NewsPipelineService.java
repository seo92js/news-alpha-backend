package com.seo92js.news_alpha_backend.domain.news.service;

import com.seo92js.news_alpha_backend.domain.news.News;
import com.seo92js.news_alpha_backend.domain.signal.service.SignalDetectionService;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
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
     * 특정 수집 쿼리(StockKeyword) 기준으로 뉴스를 수집하고 신규 뉴스를 임베딩하여 발견된 뉴스 목록을 반환
     */
    public List<News> collectAndEmbed(StockKeyword stockKeyword) {
        String keyword = stockKeyword.getKeyword();
        String combinedQuery = (keyword == null || keyword.isBlank())
                ? stockKeyword.getStock().getName()
                : stockKeyword.getStock().getName() + " " + keyword;

        CollectedNewsResult collectedNews = newsService.collectNews(combinedQuery);
        if (collectedNews.discoveredNews().isEmpty()) {
            return collectedNews.discoveredNews();
        }

        newsDocumentService.process(collectedNews.newlySavedNews());

        log.info(
                "종목 뉴스 수집 및 임베딩 완료. stock={}, keyword={}, discoveredCount={}, savedCount={}",
                stockKeyword.getStock().getName(),
                (keyword == null || keyword.isBlank()) ? "기본" : keyword,
                collectedNews.discoveredNews().size(),
                collectedNews.newlySavedNews().size()
        );
        return collectedNews.discoveredNews();
    }

    /**
     * 종목에 대해 누적 수집된 뉴스 목록을 기반으로 시그널을 일괄 탐지 및 분석
     */
    public void detectSignals(Stock stock, List<News> allDiscoveredNews) {
        if (allDiscoveredNews == null || allDiscoveredNews.isEmpty()) {
            return;
        }

        signalDetectionService.detect(stock, stock.getName(), allDiscoveredNews);
        log.info("종목 시그널 일괄 탐지 완료. stock={}, totalNewsCount={}", stock.getName(), allDiscoveredNews.size());
    }

    /**
     * StockKeyword 기준으로 뉴스 수집, 신규 뉴스 임베딩 저장, 발견 뉴스 기반 시그널 탐지를 순서대로 실행 (하위 호환성 유지)
     */
    public List<News> collectEmbedAndDetect(StockKeyword stockKeyword) {
        List<News> discovered = collectAndEmbed(stockKeyword);
        detectSignals(stockKeyword.getStock(), discovered);
        return discovered;
    }
}
