package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seo92js.news_alpha_backend.domain.stock.QStockReport;
import com.seo92js.news_alpha_backend.domain.stock.StockReport;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.seo92js.news_alpha_backend.domain.stock.QStockReport.stockReport;

@RequiredArgsConstructor
public class StockReportRepositoryImpl implements StockReportRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<StockReport> findLatestStockReports() {
        QStockReport latestStockReport = new QStockReport("latestStockReport");
        return queryFactory
                .select(stockReport)
                .from(stockReport)
                .where(stockReport.generatedAt.eq(
                        JPAExpressions.select(latestStockReport.generatedAt.max())
                                .from(latestStockReport)
                                .where(latestStockReport.stock.id.eq(stockReport.stock.id))
                ))
                .fetch();
    }
}
