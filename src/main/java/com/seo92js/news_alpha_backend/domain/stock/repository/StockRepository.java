package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.seo92js.news_alpha_backend.domain.stock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    boolean existsByTickerAndMarket(String ticker, String market);
    Optional<Stock> findByTickerAndMarket(String ticker, String market);
}
