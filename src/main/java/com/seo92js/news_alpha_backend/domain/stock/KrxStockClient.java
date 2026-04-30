package com.seo92js.news_alpha_backend.domain.stock;

import com.seo92js.news_alpha_backend.common.exception.ApiFetchException;
import com.seo92js.news_alpha_backend.common.exception.ErrorCode;
import com.seo92js.news_alpha_backend.domain.stock.dto.KrxStockItem;
import com.seo92js.news_alpha_backend.domain.stock.dto.KrxStockResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class KrxStockClient {

    private static final int PAGE_SIZE = 5000;

    private final RestClient restClient;
    private final KrxStockProperties properties;

    public KrxStockClient(KrxStockProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl("https://apis.data.go.kr")
                .build();
        this.properties = properties;
    }

    public List<KrxStockItem> fetch(String baseDate) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/1160100/service/GetKrxListedInfoService/getItemInfo")
                        .queryParam("serviceKey", properties.apiKey())
                        .queryParam("basDt", baseDate)
//                        .queryParam("numOfRows", PAGE_SIZE)
//                        .queryParam("pageNo", 1)
                        .queryParam("resultType", "json")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {

                    throw new ApiFetchException(ErrorCode.API_FETCH_FAILED
                            , "NaverNews"
                            , baseDate);
                })
                .body(KrxStockResponse.class)
                .response()
                .body()
                .items()
                .item();
    }
}