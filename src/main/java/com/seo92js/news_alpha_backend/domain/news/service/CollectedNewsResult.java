package com.seo92js.news_alpha_backend.domain.news.service;

import com.seo92js.news_alpha_backend.domain.news.News;

import java.util.List;

public record CollectedNewsResult(
        List<News> discoveredNews,
        List<News> newlySavedNews
) {
}
