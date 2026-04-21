package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seo92js.news_alpha_backend.domain.stock.QStockKeyword;
import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class StockKeywordRepositoryImpl implements StockKeywordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<StockKeyword> findEnabledWithStock() {
        QStockKeyword stockKeyword = QStockKeyword.stockKeyword;

        return queryFactory
                .selectFrom(stockKeyword)
                .join(stockKeyword.stock).fetchJoin()
                .where(stockKeyword.enabled.isTrue())
                .fetch();
    }
}
