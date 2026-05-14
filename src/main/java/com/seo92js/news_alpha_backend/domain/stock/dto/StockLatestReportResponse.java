package com.seo92js.news_alpha_backend.domain.stock.dto;

import com.seo92js.news_alpha_backend.domain.stock.StockReport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StockLatestReportResponse(
        Long stockId,
        String stockName,
        String ticker,
        String market,
        LocalDate reportDate,
        int signalCount,
        String report,
        LocalDateTime generatedAt,
        List<StockSignalSummaryResponse> signals
) {
    public static StockLatestReportResponse from(StockReport stockReport, List<StockSignalSummaryResponse> signals) {
        return new StockLatestReportResponse(
                stockReport.getStock().getId(),
                stockReport.getStock().getName(),
                stockReport.getStock().getTicker(),
                stockReport.getStock().getMarket(),
                stockReport.getReportDate(),
                stockReport.getSignalCount(),
                stockReport.getReport(),
                stockReport.getGeneratedAt(),
                signals
        );
    }
}
