package com.seo92js.news_alpha_backend.domain.stock.dto;

public record StockSaveRequest(
        String ticker,
        String name,
        String market
) {
}
