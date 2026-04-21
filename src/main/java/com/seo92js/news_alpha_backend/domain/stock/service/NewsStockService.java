package com.seo92js.news_alpha_backend.domain.stock.service;

import com.seo92js.news_alpha_backend.domain.news.News;
import com.seo92js.news_alpha_backend.domain.stock.NewsStock;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.repository.NewsStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsStockService {

    private final NewsStockRepository newsStockRepository;

    /**
     * 수집된 뉴스들을 해당 Stock과 연결
     */
    @Transactional
    public void connect(List<News> newsList, Stock stock, String matchedKeyword) {
        for (News news : newsList) {
            if (!newsStockRepository.existsByNewsIdAndStockId(news.getId(), stock.getId())) {
                newsStockRepository.save(NewsStock.of(news, stock, matchedKeyword));
            }
        }
    }
}
