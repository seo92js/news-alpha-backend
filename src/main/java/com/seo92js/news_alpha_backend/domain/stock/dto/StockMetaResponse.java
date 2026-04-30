package com.seo92js.news_alpha_backend.domain.stock.dto;

import com.seo92js.news_alpha_backend.domain.stock.KrxStock;

public record StockMetaResponse(
        String ticker,
        String name,
        String market
) {
    public static StockMetaResponse from(KrxStock krxStock) {
        return new StockMetaResponse(
                krxStock.getTicker(),
                krxStock.getName(),
                krxStock.getMarket()
        );
    }
}