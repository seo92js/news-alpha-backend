package com.seo92js.news_alpha_backend.domain.signal.service;

import com.seo92js.news_alpha_backend.domain.signal.SignalEventType;
import com.seo92js.news_alpha_backend.domain.signal.SignalSentiment;

public record SignalAnalysisResult(
        String title,
        String summary,
        SignalEventType eventType,
        SignalSentiment sentiment,
        int confidence,
        String investorSummary
) {
    public static SignalAnalysisResult defaultResult(String title, String summary) {
        return new SignalAnalysisResult(
                title,
                summary,
                SignalEventType.ETC,
                SignalSentiment.NEUTRAL,
                50,
                summary
        );
    }
}
