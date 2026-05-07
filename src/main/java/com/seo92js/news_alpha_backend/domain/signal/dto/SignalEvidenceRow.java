package com.seo92js.news_alpha_backend.domain.signal.dto;

import java.time.LocalDateTime;

public record SignalEvidenceRow(
        Long signalId,
        Long newsId,
        int rankOrder,
        String title,
        String url,
        LocalDateTime publishedAt
) {
}
