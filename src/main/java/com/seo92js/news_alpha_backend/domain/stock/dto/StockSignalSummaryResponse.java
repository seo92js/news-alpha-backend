package com.seo92js.news_alpha_backend.domain.stock.dto;

import com.seo92js.news_alpha_backend.domain.signal.SignalEventType;
import com.seo92js.news_alpha_backend.domain.signal.SignalSentiment;

import java.time.LocalDateTime;

public record StockSignalSummaryResponse(
        Long stockReportId,
        Long signalId,
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
        LocalDateTime detectedAt
) {
    public StockSignalSummaryResponse(
            Long stockReportId,
            Long signalId,
            String title,
            String summary,
            SignalEventType eventType,
            SignalSentiment sentiment,
            Integer confidence,
            String investorSummary,
            double score,
            int relatedNewsCount,
            LocalDateTime detectedAt
    ) {
        this(
                stockReportId,
                signalId,
                title,
                summary,
                eventType,
                null,
                sentiment,
                null,
                confidence,
                investorSummary,
                score,
                relatedNewsCount,
                detectedAt
        );
    }

    public StockSignalSummaryResponse {
        if (eventType == null) {
            eventType = SignalEventType.ETC;
        }
        eventTypeLabel = eventType.getLabel();
        if (sentiment == null) {
            sentiment = SignalSentiment.NEUTRAL;
        }
        sentimentLabel = sentiment.getLabel();
        if (confidence == null) {
            confidence = 50;
        }
        if (investorSummary == null || investorSummary.isBlank()) {
            investorSummary = summary;
        }
    }
}
