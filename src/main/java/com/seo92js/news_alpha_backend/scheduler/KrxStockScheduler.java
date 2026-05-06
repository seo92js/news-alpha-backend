package com.seo92js.news_alpha_backend.scheduler;

import com.seo92js.news_alpha_backend.domain.stock.service.KrxStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class KrxStockScheduler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final KrxStockService krxStockService;

//    @Scheduled(cron = "0 0 14 * * MON-FRI")
    public void syncStockMeta() {

        // 상장종목 정보가 다음날 13시에 update, 이전일 기준으로 조회
        String baseDate = LocalDate.now()
                .minusDays(1)
                .format(DATE_FORMATTER);

        try {

            krxStockService.syncAllStockInfo(baseDate);
        }
        catch (Exception e) {

            log.warn("종목 정보 현행화 실패 | baseDate={}", baseDate, e);
        }
    }
}