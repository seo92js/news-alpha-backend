package com.seo92js.news_alpha_backend.domain.stock.dto;

import java.util.List;

public record KrxStockResponse(Response response) {
    public record Response(Body body) {
        public record Body(Items items) {
            public record Items(List<KrxStockItem> item) {}
        }
    }
}
