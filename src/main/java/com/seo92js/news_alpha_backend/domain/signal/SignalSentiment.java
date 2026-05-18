package com.seo92js.news_alpha_backend.domain.signal;

public enum SignalSentiment {
    POSITIVE("긍정"),
    NEGATIVE("부정"),
    NEUTRAL("중립"),
    MIXED("혼재");

    private final String label;

    SignalSentiment(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
