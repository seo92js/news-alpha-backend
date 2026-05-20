package com.seo92js.news_alpha_backend.domain.signal.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seo92js.news_alpha_backend.domain.signal.Signal;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.seo92js.news_alpha_backend.domain.signal.QSignal.signal;

@RequiredArgsConstructor
public class SignalRepositoryImpl implements SignalRepositoryCustom {

    private static final int MAX_RECENT_SIGNAL_VIEWS = 5;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Signal> findRecentSignalsByStockId(Long stockId, LocalDateTime since, Pageable pageable) {
        return queryFactory
                .select(signal)
                .from(signal)
                .where(
                        signal.stock.id.eq(stockId),
                        signal.detectedAt.goe(since)
                )
                .orderBy(signal.score.desc(), signal.detectedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<SignalView> findTopRecentSignalViews() {
        return queryFactory
                .select(Projections.constructor(
                        SignalView.class,
                        signal.id,
                        signal.keyword,
                        signal.type,
                        signal.title,
                        signal.summary,
                        signal.eventType,
                        signal.sentiment,
                        signal.confidence,
                        signal.investorSummary,
                        signal.score,
                        signal.relatedNewsCount,
                        signal.firstPublishedAt,
                        signal.lastPublishedAt,
                        signal.detectedAt
                ))
                .from(signal)
                .orderBy(signal.score.desc(), signal.detectedAt.desc())
                .limit(MAX_RECENT_SIGNAL_VIEWS)
                .fetch();
    }

    @Override
    public SignalView findSignalViewById(Long signalId) {
        return queryFactory
                .select(Projections.constructor(
                        SignalView.class,
                        signal.id,
                        signal.keyword,
                        signal.type,
                        signal.title,
                        signal.summary,
                        signal.eventType,
                        signal.sentiment,
                        signal.confidence,
                        signal.investorSummary,
                        signal.score,
                        signal.relatedNewsCount,
                        signal.firstPublishedAt,
                        signal.lastPublishedAt,
                        signal.detectedAt
                ))
                .from(signal)
                .where(signal.id.eq(signalId))
                .fetchOne();
    }

    @Override
    public Set<String> findExistingSignalKeys(Collection<String> signalKeys) {
        if (signalKeys == null || signalKeys.isEmpty()) {
            return Set.of();
        }

        return queryFactory
                .select(signal.signalKey)
                .from(signal)
                .where(signal.signalKey.in(signalKeys))
                .fetch()
                .stream()
                .collect(Collectors.toSet());
    }
}
