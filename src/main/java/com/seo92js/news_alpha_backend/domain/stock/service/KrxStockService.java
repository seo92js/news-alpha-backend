package com.seo92js.news_alpha_backend.domain.stock.service;

import com.seo92js.news_alpha_backend.domain.stock.KrxStock;
import com.seo92js.news_alpha_backend.domain.stock.KrxStockClient;
import com.seo92js.news_alpha_backend.domain.stock.dto.KrxStockItem;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockMetaResponse;
import com.seo92js.news_alpha_backend.domain.stock.repository.KrxStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KrxStockService {

    private final KrxStockClient krxStockClient;
    private final KrxStockRepository krxStockRepository;

    /**
     * 전 종목 meta 정보 upsert
     */
    @Transactional
    public void syncAllStockInfo(String baseDate) {
        List<KrxStockItem> items = krxStockClient.fetch(baseDate);

        if (ObjectUtils.isEmpty(items)) {
            log.info("{} 일 기준 상장종목정보 없음", baseDate);
            return;
        }

        List<String> tickers = items.stream()
                .map(KrxStockItem::ticker)
                .toList();
        Map<String, KrxStock> existingByTicker = krxStockRepository.findAllByTickerIn(tickers)
                .stream()
                .collect(Collectors.toMap(KrxStock::getTicker, Function.identity()));

        List<KrxStock> toInsert = new ArrayList<>();

        for (KrxStockItem item : items) {
            KrxStock existing = existingByTicker.get(item.ticker());
            if (ObjectUtils.isEmpty(existing)) {

                toInsert.add(KrxStock.of(item.baseDate(), item.ticker(), item.isinCode(), item.market(), item.name()
                        , item.corpRegNo(), item.corpName()));
            }
            else {

                existing.update(item.baseDate(), item.isinCode(), item.market(), item.name(), item.corpRegNo()
                        , item.corpName());
            }
        }

        if (!toInsert.isEmpty()) krxStockRepository.saveAll(toInsert);

        log.info("종목 정보 현행화 완료 | 기준일자: {}, 처리건수: {}", baseDate, items.size());
    }

    @Transactional(readOnly = true)
    public List<StockMetaResponse> findAll() {
        return krxStockRepository.findAll()
                .stream()
                .map(StockMetaResponse::from)
                .toList();
    }
}