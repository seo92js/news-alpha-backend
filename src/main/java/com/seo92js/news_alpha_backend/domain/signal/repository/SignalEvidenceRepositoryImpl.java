package com.seo92js.news_alpha_backend.domain.signal.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceDetailRow;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceRow;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.seo92js.news_alpha_backend.domain.news.QNews.news;
import static com.seo92js.news_alpha_backend.domain.signal.QSignalEvidence.signalEvidence;

@RequiredArgsConstructor
public class SignalEvidenceRepositoryImpl implements SignalEvidenceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SignalEvidenceRow> findEvidenceRowsBySignalIds(List<Long> signalIds) {
        if (signalIds == null || signalIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(Projections.constructor(
                        SignalEvidenceRow.class,
                        signalEvidence.signal.id,
                        signalEvidence.newsId,
                        signalEvidence.rankOrder,
                        signalEvidence.title,
                        signalEvidence.url,
                        signalEvidence.publishedAt
                ))
                .from(signalEvidence)
                .where(signalEvidence.signal.id.in(signalIds))
                .orderBy(signalEvidence.signal.id.asc(), signalEvidence.rankOrder.asc())
                .fetch();
    }

    @Override
    public List<SignalEvidenceDetailRow> findEvidenceDetailRowsBySignalId(Long signalId) {
        return queryFactory
                .select(Projections.constructor(
                        SignalEvidenceDetailRow.class,
                        signalEvidence.newsId,
                        signalEvidence.rankOrder,
                        signalEvidence.title,
                        signalEvidence.url,
                        signalEvidence.publishedAt,
                        news.description,
                        news.content
                ))
                .from(signalEvidence)
                .leftJoin(news).on(news.id.eq(signalEvidence.newsId))
                .where(signalEvidence.signal.id.eq(signalId))
                .orderBy(signalEvidence.rankOrder.asc())
                .fetch();
    }
}
