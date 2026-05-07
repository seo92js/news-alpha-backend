package com.seo92js.news_alpha_backend.domain.signal.dto;

import com.seo92js.news_alpha_backend.domain.signal.SignalType;

import java.time.LocalDateTime;

public record SignalView(
        Long id,
        String keyword,
        SignalType type,
        String title,
        String summary,
        double score,
        int relatedNewsCount,
        LocalDateTime firstPublishedAt,
        LocalDateTime lastPublishedAt,
        LocalDateTime detectedAt
) {
}
