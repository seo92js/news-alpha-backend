package com.seo92js.news_alpha_backend.domain.stock;

import com.seo92js.news_alpha_backend.BaseEntity;
import com.seo92js.news_alpha_backend.domain.news.News;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_news_stock_news_stock", columnNames = {"news_id", "stock_id"})
        }
)
public class NewsStock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private String matchedKeyword;

    public static NewsStock of(News news, Stock stock, String matchedKeyword) {
        NewsStock newsStock = new NewsStock();
        newsStock.news = news;
        newsStock.stock = stock;
        newsStock.matchedKeyword = matchedKeyword;
        return newsStock;
    }
}
