package com.seo92js.news_alpha_backend.domain.stock;

import com.seo92js.news_alpha_backend.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_stock_keyword_stock_keyword", columnNames = {"stock_id", "keyword"})
        }
)
public class StockKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private boolean enabled;

    public static StockKeyword of(Stock stock, String keyword) {
        StockKeyword stockKeyword = new StockKeyword();
        stockKeyword.stock = stock;
        stockKeyword.keyword = keyword;
        stockKeyword.enabled = true;
        return stockKeyword;
    }
}
