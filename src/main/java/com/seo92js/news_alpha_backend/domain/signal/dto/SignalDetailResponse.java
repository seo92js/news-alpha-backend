package com.seo92js.news_alpha_backend.domain.signal.dto;

import com.seo92js.news_alpha_backend.domain.signal.SignalEventType;
import com.seo92js.news_alpha_backend.domain.signal.SignalSentiment;
import com.seo92js.news_alpha_backend.domain.signal.SignalType;

import java.time.LocalDateTime;
import java.util.List;

public record SignalDetailResponse(
        Long id,
        String keyword,
        SignalType type,
        String title,
        String summary,
        SignalEventType eventType,
        String eventTypeLabel,
        SignalSentiment sentiment,
        String sentimentLabel,
        Integer confidence,
        String investorSummary,
        double score,
        int relatedNewsCount,
        LocalDateTime firstPublishedAt,
        LocalDateTime lastPublishedAt,
        LocalDateTime detectedAt,
        List<SignalEvidenceDetailResponse> evidences
) {
    public static SignalDetailResponse from(SignalView signal, List<SignalEvidenceDetailResponse> evidences) {
        return new SignalDetailResponse(
                signal.id(),
                signal.keyword(),
                signal.type(),
                signal.title(),
                signal.summary(),
                signal.eventType(),
                signal.eventType().getLabel(),
                signal.sentiment(),
                signal.sentiment().getLabel(),
                signal.confidence(),
                signal.investorSummary(),
                signal.score(),
                signal.relatedNewsCount(),
                signal.firstPublishedAt(),
                signal.lastPublishedAt(),
                signal.detectedAt(),
                evidences
        );
    }
}
