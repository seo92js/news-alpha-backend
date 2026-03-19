package com.seo92js.news_alpha_backend.news.service;

import com.seo92js.news_alpha_backend.config.security.jwt.JwtTokenProvider;
import com.seo92js.news_alpha_backend.domain.news.NaverNewsClient;
import com.seo92js.news_alpha_backend.domain.news.service.NewsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WithMockUser
@SpringBootTest
class NewsServiceTest {
    @Autowired
    NewsService newsService;

    @Autowired
    NaverNewsClient naverNewsClient;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Test
    void fetchNews() {
        newsService.fetchNews("비트");
    }
}