package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockKeywordRepository extends JpaRepository<StockKeyword, Long>, StockKeywordRepositoryCustom {
    List<StockKeyword> findByStockId(Long stockId);
    boolean existsByStockIdAndKeyword(Long stockId, String keyword);
    Optional<StockKeyword> findByStockIdAndKeyword(Long stockId, String keyword);
    void deleteByStockId(Long stockId);
}
