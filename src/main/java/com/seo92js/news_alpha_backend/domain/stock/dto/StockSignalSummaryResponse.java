package com.seo92js.news_alpha_backend.domain.stock.dto;

import java.time.LocalDateTime;

public record StockSignalSummaryResponse(
        Long signalId,
        String title,
        String summary,
        double score,
        int relatedNewsCount,
        LocalDateTime detectedAt
) {
}
