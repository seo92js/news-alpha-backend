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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KrxStockService {

    private final KrxStockClient krxStockClient;
    private final KrxStockRepository krxStockRepository;

    /**
     * 국장 전 종목 meta 정보 upsert
     */
    @Transactional
    public void syncAllStockInfo(String baseDate) {
        List<KrxStockItem> items = krxStockClient.fetch(baseDate);
        log.debug("{} 일 전체 종목 정보 조회 완료 | 건수: {}", baseDate, items.size());

        for (KrxStockItem item : items) {
            krxStockRepository.findByTicker(item.ticker())
                    .ifPresentOrElse(
                            meta -> meta.update(
                                    item.baseDate(),
                                    item.isinCode(),
                                    item.market(),
                                    item.name(),
                                    item.corpRegNo(),
                                    item.corpName()
                            ),
                            () -> krxStockRepository.save(
                                    KrxStock.of(
                                            item.baseDate(),
                                            item.ticker(),
                                            item.isinCode(),
                                            item.market(),
                                            item.name(),
                                            item.corpRegNo(),
                                            item.corpName()
                                    )
                            )
                    );
        }

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