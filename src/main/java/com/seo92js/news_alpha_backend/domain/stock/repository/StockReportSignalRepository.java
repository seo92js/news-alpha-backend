package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.seo92js.news_alpha_backend.domain.stock.StockReportSignal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReportSignalRepository extends JpaRepository<StockReportSignal, Long>, StockReportSignalRepositoryCustom {
    void deleteByStockReportStockId(Long stockId);
}
