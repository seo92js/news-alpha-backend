package com.seo92js.news_alpha_backend.domain.ai.dto;

import com.seo92js.news_alpha_backend.domain.news.News;

public record NewsMetadata(
    String url,
    String publishedAt,
    int chunkIndex,
    String keyword,
    String title
) {
    public NewsMetadata(News news, int chunkIndex) {
        this(
                news.getLink(),
                news.getPubDate().toString(),
                chunkIndex,
                news.getKeyword(),
                news.getTitle()
        );
    }
}
