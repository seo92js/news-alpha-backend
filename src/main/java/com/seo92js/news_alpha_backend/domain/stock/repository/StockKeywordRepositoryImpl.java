package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.seo92js.news_alpha_backend.domain.stock.QStockKeyword.stockKeyword;

@RequiredArgsConstructor
public class StockKeywordRepositoryImpl implements StockKeywordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<StockKeyword> findEnabledWithStock() {
        return queryFactory
                .selectFrom(stockKeyword)
                .join(stockKeyword.stock).fetchJoin()
                .where(stockKeyword.enabled.isTrue())
                .fetch();
    }
}
