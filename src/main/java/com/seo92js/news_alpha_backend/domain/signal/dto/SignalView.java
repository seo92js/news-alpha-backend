package com.seo92js.news_alpha_backend.domain.signal.dto;

import com.seo92js.news_alpha_backend.domain.signal.SignalEventType;
import com.seo92js.news_alpha_backend.domain.signal.SignalSentiment;
import com.seo92js.news_alpha_backend.domain.signal.SignalType;

import java.time.LocalDateTime;

public record SignalView(
        Long id,
        String keyword,
        SignalType type,
        String title,
        String summary,
        SignalEventType eventType,
        SignalSentiment sentiment,
        Integer confidence,
        String investorSummary,
        double score,
        int relatedNewsCount,
        LocalDateTime firstPublishedAt,
        LocalDateTime lastPublishedAt,
        LocalDateTime detectedAt
) {
    public SignalView {
        if (eventType == null) {
            eventType = SignalEventType.ETC;
        }
        if (sentiment == null) {
            sentiment = SignalSentiment.NEUTRAL;
        }
        if (confidence == null) {
            confidence = 50;
        }
        if (investorSummary == null || investorSummary.isBlank()) {
            investorSummary = summary;
        }
    }
}
