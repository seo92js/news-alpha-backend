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
                @UniqueConstraint(name = "uk_stock_ticker_market", columnNames = {"ticker", "market"})
        }
)
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String market;

    public static Stock of(String ticker, String name, String market) {
        Stock stock = new Stock();
        stock.ticker = ticker;
        stock.name = name;
        stock.market = market;
        return stock;
    }
}
