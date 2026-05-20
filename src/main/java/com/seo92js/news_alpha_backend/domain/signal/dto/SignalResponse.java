package com.seo92js.news_alpha_backend.domain.signal.dto;

import com.seo92js.news_alpha_backend.domain.signal.SignalEventType;
import com.seo92js.news_alpha_backend.domain.signal.SignalSentiment;
import com.seo92js.news_alpha_backend.domain.signal.SignalType;

import java.time.LocalDateTime;
import java.util.List;

public record SignalResponse(
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
        List<SignalEvidenceResponse> evidences
) {
    public static SignalResponse from(SignalView signal, List<SignalEvidenceResponse> evidences) {
        return new SignalResponse(
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
