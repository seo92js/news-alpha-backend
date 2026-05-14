package com.seo92js.news_alpha_backend.domain.stock.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockSignalSummaryResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.seo92js.news_alpha_backend.domain.signal.QSignal.signal;
import static com.seo92js.news_alpha_backend.domain.stock.QStockReportSignal.stockReportSignal;

@RequiredArgsConstructor
public class StockReportSignalRepositoryImpl implements StockReportSignalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<StockSignalSummaryResponse> findSignalSummariesByStockReportId(Long stockReportId) {
        return queryFactory
                .select(Projections.constructor(
                        StockSignalSummaryResponse.class,
                        signal.id,
                        signal.title,
                        signal.summary,
                        signal.score,
                        signal.relatedNewsCount,
                        signal.detectedAt
                ))
                .from(stockReportSignal)
                .join(stockReportSignal.signal, signal)
                .where(stockReportSignal.stockReport.id.eq(stockReportId))
                .orderBy(stockReportSignal.rankOrder.asc())
                .fetch();
    }
}
