package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.seo92js.news_alpha_backend.domain.stock.KrxStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KrxStockRepository extends JpaRepository<KrxStock, Long> {
    List<KrxStock> findAllByTickerIn(List<String> tickers);
}