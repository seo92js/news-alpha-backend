package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.seo92js.news_alpha_backend.domain.stock.NewsStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsStockRepository extends JpaRepository<NewsStock, Long> {
    boolean existsByNewsIdAndStockId(Long newsId, Long stockId);
}
