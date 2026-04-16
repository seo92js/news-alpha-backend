package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;

import java.util.List;

public interface StockKeywordRepositoryCustom {
    List<StockKeyword> findEnabledWithStock();
}
