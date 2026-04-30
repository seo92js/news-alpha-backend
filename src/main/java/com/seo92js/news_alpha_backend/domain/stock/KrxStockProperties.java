package com.seo92js.news_alpha_backend.domain.stock;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "public-data.krx")
public record KrxStockProperties(
        String apiKey
) {
}