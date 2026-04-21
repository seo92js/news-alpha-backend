package com.seo92js.news_alpha_backend.domain.signal.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seo92js.news_alpha_backend.domain.signal.QSignal;
import com.seo92js.news_alpha_backend.domain.signal.QSignalEvidence;
import com.seo92js.news_alpha_backend.domain.signal.Signal;
import com.seo92js.news_alpha_backend.domain.stock.QNewsStock;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class SignalRepositoryImpl implements SignalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Signal> findRecentSignalsByStockId(Long stockId, LocalDateTime since, Pageable pageable) {
        QSignal signal = QSignal.signal;
        QSignalEvidence evidence = QSignalEvidence.signalEvidence;
        QNewsStock newsStock = QNewsStock.newsStock;

        return queryFactory
                .selectDistinct(signal)
                .from(signal)
                .join(evidence).on(evidence.signal.eq(signal))
                .join(newsStock).on(newsStock.news.id.eq(evidence.newsId))
                .where(
                        newsStock.stock.id.eq(stockId),
                        signal.detectedAt.goe(since)
                )
                .orderBy(signal.score.desc(), signal.detectedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
