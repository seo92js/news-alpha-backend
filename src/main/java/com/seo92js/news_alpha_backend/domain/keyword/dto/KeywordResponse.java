package com.seo92js.news_alpha_backend.domain.keyword.dto;

import com.seo92js.news_alpha_backend.domain.keyword.Keyword;

import java.time.LocalDateTime;

public record KeywordResponse(Long id, String name, LocalDateTime createdAt) {

    public static KeywordResponse from(Keyword keyword) {
        return new KeywordResponse(keyword.getId(), keyword.getName(), keyword.getCreatedAt());
    }
}
