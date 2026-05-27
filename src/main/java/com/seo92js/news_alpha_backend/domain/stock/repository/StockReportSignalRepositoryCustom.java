package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.seo92js.news_alpha_backend.domain.stock.dto.StockSignalSummaryResponse;

import java.util.List;

public interface StockReportSignalRepositoryCustom {
    List<StockSignalSummaryResponse> findSignalSummariesByStockReportId(Long stockReportId);
    List<StockSignalSummaryResponse> findSignalSummariesByStockReportIds(List<Long> stockReportIds);
}
