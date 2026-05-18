package com.seo92js.news_alpha_backend.domain.signal;

public enum SignalEventType {
    EARNINGS("실적"),
    REGULATION("규제/정책"),
    PRODUCT("제품/사업"),
    LEGAL("소송/법무"),
    MANAGEMENT("경영진/지배구조"),
    MARKET("시장/거시"),
    ETC("기타");

    private final String label;

    SignalEventType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
