package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.seo92js.news_alpha_backend.domain.stock.StockReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StockReportRepository extends JpaRepository<StockReport, Long> {
    Optional<StockReport> findByStockIdAndReportDate(Long stockId, LocalDate reportDate);
    Optional<StockReport> findTopByStockIdOrderByGeneratedAtDesc(Long stockId);
}
