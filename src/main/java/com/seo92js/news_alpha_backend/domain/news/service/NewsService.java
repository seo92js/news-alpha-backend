package com.seo92js.news_alpha_backend.domain.news.service;

import com.seo92js.news_alpha_backend.domain.news.NaverNewsClient;
import com.seo92js.news_alpha_backend.domain.news.dto.NaverNewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NaverNewsClient naverNewsClient;

    @Value("${naver.api.id}")
    private String clientId;

    @Value("${naver.api.secret}")
    private String clientSecret;

    public void fetchNews(String keyword) {
        NaverNewsResponse response = naverNewsClient.fetch(keyword,  clientId, clientSecret);

        // TODO : 정제해서 db 삽입까지
    }
}
