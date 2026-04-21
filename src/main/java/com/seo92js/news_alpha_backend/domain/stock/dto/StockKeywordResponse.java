package com.seo92js.news_alpha_backend.domain.stock.dto;

import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;

import java.time.LocalDateTime;

public record StockKeywordResponse(
        Long id,
        Long stockId,
        String keyword,
        boolean enabled,
        LocalDateTime createdAt
) {
    public static StockKeywordResponse from(StockKeyword stockKeyword) {
        return new StockKeywordResponse(
                stockKeyword.getId(),
                stockKeyword.getStock().getId(),
                stockKeyword.getKeyword(),
                stockKeyword.isEnabled(),
                stockKeyword.getCreatedAt()
        );
    }
}
