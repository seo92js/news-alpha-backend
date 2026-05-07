package com.seo92js.news_alpha_backend.domain.signal.dto;

import java.time.LocalDateTime;

public record SignalEvidenceDetailResponse(
        Long newsId,
        int rankOrder,
        String title,
        String url,
        LocalDateTime publishedAt,
        String preview
) {
    public static SignalEvidenceDetailResponse from(SignalEvidenceDetailRow evidence) {
        return new SignalEvidenceDetailResponse(
                evidence.newsId(),
                evidence.rankOrder(),
                evidence.title(),
                evidence.url(),
                evidence.publishedAt(),
                buildPreview(evidence.description(), evidence.content())
        );
    }

    private static String buildPreview(String description, String content) {
        String source = description;
        if (content != null && !content.isBlank()) {
            source = content;
        }
        if (source == null || source.isBlank()) {
            return null;
        }

        String normalized = source.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 220) {
            return normalized;
        }
        return normalized.substring(0, 220) + "...";
    }
}
