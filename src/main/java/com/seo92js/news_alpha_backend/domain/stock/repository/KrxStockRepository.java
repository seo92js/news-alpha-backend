package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.seo92js.news_alpha_backend.domain.stock.KrxStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KrxStockRepository extends JpaRepository<KrxStock, Long> {
    Optional<KrxStock> findByTicker(String ticker);
}