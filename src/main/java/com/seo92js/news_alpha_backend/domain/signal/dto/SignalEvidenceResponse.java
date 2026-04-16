package com.seo92js.news_alpha_backend.domain.signal.dto;

import com.seo92js.news_alpha_backend.domain.signal.SignalEvidence;

import java.time.LocalDateTime;

public record SignalEvidenceResponse(
        Long newsId,
        int rankOrder,
        String title,
        String url,
        LocalDateTime publishedAt
) {
    public static SignalEvidenceResponse from(SignalEvidence evidence) {
        return new SignalEvidenceResponse(
                evidence.getNewsId(),
                evidence.getRankOrder(),
                evidence.getTitle(),
                evidence.getUrl(),
                evidence.getPublishedAt()
        );
    }
}
