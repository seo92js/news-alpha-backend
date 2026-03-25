package com.seo92js.news_alpha_backend.config.filter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@ConfigurationProperties(prefix = "custom.access-log")
public record StaticResourceProperties(
        List<String> staticPatterns
) {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    public boolean isStaticResource(String path) {
        if (path == null || path.isBlank()) return false;

        return staticPatterns.stream()
                .anyMatch(pattern -> MATCHER.match(pattern, path));
    }
}
