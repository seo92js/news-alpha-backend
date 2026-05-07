package com.seo92js.news_alpha_backend.domain.signal.dto;

import java.time.LocalDateTime;

public record SignalEvidenceDetailRow(
        Long newsId,
        int rankOrder,
        String title,
        String url,
        LocalDateTime publishedAt,
        String description,
        String content
) {
}
