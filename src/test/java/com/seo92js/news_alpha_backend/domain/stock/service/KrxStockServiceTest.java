package com.seo92js.news_alpha_backend.domain.stock.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SpringBootTest
class KrxStockServiceTest {

    @Autowired
    private KrxStockService krxStockService;

    @Test
    void 전종목_메타_현행화() {
        String baseDate = LocalDate.of(2026, 4, 30)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        krxStockService.syncAllStockInfo(baseDate);
    }
}