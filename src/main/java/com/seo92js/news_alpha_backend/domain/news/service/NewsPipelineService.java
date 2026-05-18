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
     * StockKeyword 기준으로 뉴스 수집, Stock 연결, 임베딩 저장, 시그널 탐지를 순서대로 실행
     */
    public List<News> collectEmbedAndDetect(StockKeyword stockKeyword) {
        List<News> savedNews = newsService.fetchAndSaveNews(stockKeyword.getKeyword());
        if (savedNews.isEmpty()) {
            return savedNews;
        }

        newsDocumentService.process(savedNews);
        signalDetectionService.detect(stockKeyword.getStock(), savedNews);
        log.info(
                "종목 뉴스 파이프라인 처리 완료. stock={}, keyword={}, savedCount={}",
                stockKeyword.getStock().getName(),
                stockKeyword.getKeyword(),
                savedNews.size()
        );
        return savedNews;
    }
}
