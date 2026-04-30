package com.seo92js.news_alpha_backend.domain.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KrxStockItem(
        @JsonProperty("basDt") String baseDate,
        @JsonProperty("srtnCd") String ticker,
        @JsonProperty("isinCd") String isinCode,
        @JsonProperty("mrktCtg") String market,
        @JsonProperty("itmsNm") String name,
        @JsonProperty("crno") String corpRegNo,
        @JsonProperty("corpNm") String corpName
) {}