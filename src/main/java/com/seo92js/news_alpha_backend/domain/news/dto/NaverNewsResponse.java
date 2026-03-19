package com.seo92js.news_alpha_backend.domain.news.dto;

import java.util.List;

public record NaverNewsResponse (
    String lastBuildDate,
    int total,
    int start,
    int display,
    List<Item> items
) {
    public record Item(
        String title,
        String originallink,
        String link,
        String description,
        String pubDate
    ) {}
}
