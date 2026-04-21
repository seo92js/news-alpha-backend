package com.seo92js.news_alpha_backend.domain.signal.dto;

import com.seo92js.news_alpha_backend.domain.signal.Signal;
import com.seo92js.news_alpha_backend.domain.signal.SignalType;

import java.time.LocalDateTime;
import java.util.List;

public record SignalResponse(
        Long id,
        String keyword,
        SignalType type,
        String title,
        String summary,
        double score,
        int relatedNewsCount,
        LocalDateTime firstPublishedAt,
        LocalDateTime lastPublishedAt,
        LocalDateTime detectedAt,
        List<SignalEvidenceResponse> evidences
) {
    public static SignalResponse from(
            Signal signal,
            List<SignalEvidenceResponse> evidences
    ) {
        return new SignalResponse(
                signal.getId(),
                signal.getKeyword(),
                signal.getType(),
                signal.getTitle(),
                signal.getSummary(),
                signal.getScore(),
                signal.getRelatedNewsCount(),
                signal.getFirstPublishedAt(),
                signal.getLastPublishedAt(),
                signal.getDetectedAt(),
                evidences
        );
    }
}
