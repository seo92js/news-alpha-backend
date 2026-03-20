package com.seo92js.news_alpha_backend.domain.news;

import com.seo92js.news_alpha_backend.domain.news.dto.NaverNewsResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NaverNewsClient {

    private final RestClient restClient;

    public NaverNewsClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://openapi.naver.com")
                .build();
    }

    /**
     * 네이버 API 로 뉴스 조회
     */
    public NaverNewsResponse fetch(String keyword, String clientId, String clientSecret) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search/news.json")
                        .queryParam("query", keyword)
                        .queryParam("display", 100)
                        .queryParam("sort", "date")
                        .build())
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    // TODO : 에러 throw 해주셈
                })
                .body(NaverNewsResponse.class);
    }
}