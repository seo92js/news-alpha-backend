package com.seo92js.news_alpha_backend.domain.news;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "naver.news")
public record NaverNewsProperties(
        List<String> supportedDomains
) {
    public boolean isSupportedUrl(String url) {
        if (url == null || url.isBlank()) return false;
        return supportedDomains.stream().anyMatch(url::startsWith);
    }
}
