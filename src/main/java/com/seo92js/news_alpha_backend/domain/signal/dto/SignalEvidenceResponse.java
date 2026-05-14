package com.seo92js.news_alpha_backend.domain.signal.dto;

import java.time.LocalDateTime;

public record SignalEvidenceResponse(
        Long newsId,
        int rankOrder,
        String title,
        String url,
        LocalDateTime publishedAt
) {
    public static SignalEvidenceResponse from(SignalEvidenceRow evidence) {
        return new SignalEvidenceResponse(
                evidence.newsId(),
                evidence.rankOrder(),
                evidence.title(),
                evidence.url(),
                evidence.publishedAt()
        );
    }
}
