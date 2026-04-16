package com.seo92js.news_alpha_backend.domain.stock.dto;

import com.seo92js.news_alpha_backend.domain.stock.Stock;

import java.time.LocalDateTime;
import java.util.List;

public record StockResponse(
        Long id,
        String ticker,
        String name,
        String market,
        LocalDateTime createdAt,
        List<StockKeywordResponse> keywords
) {
    public static StockResponse from(Stock stock, List<StockKeywordResponse> keywords) {
        return new StockResponse(
                stock.getId(),
                stock.getTicker(),
                stock.getName(),
                stock.getMarket(),
                stock.getCreatedAt(),
                keywords
        );
    }
}
