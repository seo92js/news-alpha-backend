package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.seo92js.news_alpha_backend.domain.stock.StockReport;

import java.util.List;

public interface StockReportRepositoryCustom {
    List<StockReport> findLatestStockReports();
}
