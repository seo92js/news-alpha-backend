package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.seo92js.news_alpha_backend.domain.stock.StockReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockReportRepository extends JpaRepository<StockReport, Long>, StockReportRepositoryCustom {
    Optional<StockReport> findTopByStockIdOrderByGeneratedAtDesc(Long stockId);
    void deleteByStockId(Long stockId);
}
