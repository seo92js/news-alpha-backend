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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * 뉴스 조회 후 기존 뉴스는 재사용하고, 신규 뉴스만 크롤링/저장해서 수집 결과 반환
     */
    public CollectedNewsResult collectNews(String keyword) {
        NaverNewsResponse response = naverNewsClient.fetch(keyword, clientId, clientSecret);

        if (response == null || response.items() == null || response.items().isEmpty()) {
            return new CollectedNewsResult(Collections.emptyList(), Collections.emptyList());
        }

        List<NaverNewsResponse.Item> supportedItems = filterSupportedItems(response.items());
        if (supportedItems.isEmpty()) {
            return new CollectedNewsResult(Collections.emptyList(), Collections.emptyList());
        }

        Map<String, News> existingNewsByOriginalLink = newsRepository.findAllByOriginalLinkIn(
                        supportedItems.stream()
                                .map(NaverNewsResponse.Item::originallink)
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(News::getOriginalLink, Function.identity()));

        List<News> discoveredNews = new ArrayList<>();
        List<News> newlySavedNews = new ArrayList<>();

        for (NaverNewsResponse.Item item : supportedItems) {
            News existingNews = existingNewsByOriginalLink.get(item.originallink());
            if (existingNews != null) {
                discoveredNews.add(existingNews);
                continue;
            }

            crawlAndCreateNews(keyword, item).ifPresent(news -> {
                discoveredNews.add(news);
                newlySavedNews.add(news);
            });
        }

        if (!newlySavedNews.isEmpty()) {
            newsRepository.saveAll(newlySavedNews);
        }

        return new CollectedNewsResult(discoveredNews, newlySavedNews);
    }

    /**
     * 지원 URL이면서 originalLink가 있는 뉴스만 API 응답 순서대로 중복 제거
     */
    private List<NaverNewsResponse.Item> filterSupportedItems(List<NaverNewsResponse.Item> items) {
        Set<String> seenOriginalLinks = new LinkedHashSet<>();
        return items.stream()
                .filter(item -> item.originallink() != null && !item.originallink().isBlank())
                .filter(item -> naverNewsProperties.isSupportedUrl(item.link()))
                .filter(item -> seenOriginalLinks.add(item.originallink()))
                .toList();
    }

    /**
     * 신규 뉴스 본문 크롤링 후 저장 가능한 News 엔티티 생성
     */
    private Optional<News> crawlAndCreateNews(String keyword, NaverNewsResponse.Item item) {
        String content = naverArticleCrawler.crawl(item.link());
        if (content == null) {
            return Optional.empty();
        }

        return Optional.of(News.of(
                keyword,
                cleanText(item.title()),
                item.originallink(),
                item.link(),
                cleanText(item.description()),
                content,
                parsePubDate(item.pubDate())
        ));
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
