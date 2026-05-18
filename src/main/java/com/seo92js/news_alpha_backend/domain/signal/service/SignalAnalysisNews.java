package com.seo92js.news_alpha_backend.domain.signal.service;

import java.time.LocalDateTime;

public record SignalAnalysisNews(
        String title,
        String url,
        LocalDateTime publishedAt,
        String preview
) {
}
