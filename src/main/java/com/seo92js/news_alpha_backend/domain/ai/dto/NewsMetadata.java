package com.seo92js.news_alpha_backend.domain.ai.dto;

import com.seo92js.news_alpha_backend.domain.news.News;

import java.time.LocalDateTime;
import java.util.Map;

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
                resolvePublishedAt(news).toString()
        );
    }

    private static LocalDateTime resolvePublishedAt(News news) {
        if (news.getPubDate() != null) {
            return news.getPubDate();
        }
        if (news.getCreatedAt() != null) {
            return news.getCreatedAt();
        }
        return LocalDateTime.now();
    }

    public static final class Keys {
        private Keys() {}
        public static final String ID = "id";
        public static final String CHUNK_INDEX = "chunkIndex";
        public static final String KEYWORD = "keyword";
        public static final String TITLE = "title";
        public static final String URL = "url";
        public static final String PUBLISHED_AT = "publishedAt";
    }

    public Map<String, Object> toMap() {
        return Map.of(
                Keys.ID, id,
                Keys.CHUNK_INDEX, chunkIndex,
                Keys.KEYWORD, keyword,
                Keys.TITLE, title,
                Keys.URL, url,
                Keys.PUBLISHED_AT, publishedAt
        );
    }
}
