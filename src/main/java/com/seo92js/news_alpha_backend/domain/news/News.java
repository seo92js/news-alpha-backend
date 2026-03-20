package com.seo92js.news_alpha_backend.domain.news;

import com.seo92js.news_alpha_backend.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class News extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword; //검색 키워드

    private String title;

    @Column(unique = true)
    private String originalLink;

    private String link;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content; // 기사 전문 (크롤링)

    private LocalDateTime pubDate;

    public static News of(String keyword, String title, String originalLink, String link,
                          String description, String content, LocalDateTime pubDate) {
        News news = new News();
        news.keyword = keyword;
        news.title = title;
        news.originalLink = originalLink;
        news.link = link;
        news.description = description;
        news.content = content;
        news.pubDate = pubDate;
        return news;
    }
}
