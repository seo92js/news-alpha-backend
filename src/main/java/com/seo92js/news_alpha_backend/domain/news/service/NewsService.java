package com.seo92js.news_alpha_backend.domain.news.service;

import com.seo92js.news_alpha_backend.domain.news.NaverArticleCrawler;
import com.seo92js.news_alpha_backend.domain.news.NaverNewsClient;
import com.seo92js.news_alpha_backend.domain.news.NaverNewsProperties;
import com.seo92js.news_alpha_backend.domain.news.News;
import com.seo92js.news_alpha_backend.domain.news.dto.NaverNewsResponse;
import com.seo92js.news_alpha_backend.domain.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NaverNewsClient naverNewsClient;
    private final NewsRepository newsRepository;
    private final NaverArticleCrawler naverArticleCrawler;
    private final NaverNewsProperties naverNewsProperties;

    @Value("${naver.api.id}")
    private String clientId;

    @Value("${naver.api.secret}")
    private String clientSecret;

    /**
     * 네이버 뉴스 API pubDate 문자열을 LocalDateTime으로 변환하기 위한 RFC 1123 계열 포맷.
     */
    private static final DateTimeFormatter PUB_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    /**
     * 뉴스 조회 & 크롤링 후 저장한 신규 뉴스 반환
     */
    public List<News> fetchAndSaveNews(String keyword) {
        NaverNewsResponse response = naverNewsClient.fetch(keyword, clientId, clientSecret);

        if (response == null || response.items() == null || response.items().isEmpty()) {
            return Collections.emptyList();
        }

        List<News> newsList = response.items().stream()
                .filter(item -> naverNewsProperties.isSupportedUrl(item.link()))
                .filter(item -> !newsRepository.existsByOriginalLink(item.originallink()))
                .map(item -> {
                    String content = naverArticleCrawler.crawl(item.link());

                    if (content == null) {
                        return null; // 크롤링 실패 시 null 반환
                    }

                    return News.of(
                            keyword,
                            cleanText(item.title()),
                            item.originallink(),
                            item.link(),
                            cleanText(item.description()),
                            content,
                            parsePubDate(item.pubDate())
                    );
                })
                .filter(Objects::nonNull) //크롤링 실패한 뉴스 제거
                .toList();

        if (!newsList.isEmpty()) {
            newsRepository.saveAll(newsList);
        }

        return newsList;
    }

    /**
     * HTML 태그, 엔티티 제거
     */
    private String cleanText(String raw) {
        if (raw == null || raw.isBlank()) return null;

        String unescaped = HtmlUtils.htmlUnescape(raw);
        return unescaped.replaceAll("<[^>]*>", "").trim();
    }

    /**
     * pubDate -> LocalDateTime
     */
    private LocalDateTime parsePubDate(String pubDate) {
        if (pubDate == null || pubDate.isBlank()) return null;
        try {
            return ZonedDateTime.parse(pubDate, PUB_DATE_FORMATTER).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }
}
