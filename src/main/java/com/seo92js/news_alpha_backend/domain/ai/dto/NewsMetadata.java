package com.seo92js.news_alpha_backend.domain.ai.dto;

import com.seo92js.news_alpha_backend.domain.news.News;

public record NewsMetadata(
    long id,
    int chunkIndex,
    String keyword,
    String title,
    String url,
    String publishedAt
) {
    public NewsMetadata(News news, int chunkIndex) {
        this(
                news.getId(),
                chunkIndex,
                news.getKeyword(),
                news.getTitle(),
                news.getLink(),
                news.getPubDate().toString()
        );
    }
}
