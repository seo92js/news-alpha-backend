package com.seo92js.news_alpha_backend.domain.stock.service;

import com.seo92js.news_alpha_backend.domain.signal.SignalEventType;
import com.seo92js.news_alpha_backend.domain.signal.SignalSentiment;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockReport;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockLatestReportResponse;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockSignalSummaryResponse;
import com.seo92js.news_alpha_backend.domain.stock.exception.StockNotFoundException;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockKeywordRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportSignalRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockKeywordRepository stockKeywordRepository;

    @Mock
    private StockReportRepository stockReportRepository;

    @Mock
    private StockReportSignalRepository stockReportSignalRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    void 최신_종목_리포트_조회시_핵심_시그널을_함께_반환한다() {
        Long stockId = 1L;
        Stock stock = Stock.of("TSLA", "테슬라", "NASDAQ");
        ReflectionTestUtils.setField(stock, "id", stockId);

        StockReport stockReport = StockReport.of(
                stock,
                LocalDate.of(2026, 5, 7),
                2,
                "테슬라 종합 리포트",
                LocalDateTime.of(2026, 5, 7, 10, 30)
        );

        List<StockSignalSummaryResponse> signals = List.of(
                new StockSignalSummaryResponse(
                        stockReport.getId(), 101L, "로보택시 기대감 재점화", "최근 기사 급증",
                        SignalEventType.PRODUCT, SignalSentiment.POSITIVE, 78,
                        "로보택시 기대가 투자 심리에 영향을 줄 수 있습니다.",
                        88.2, 6, LocalDateTime.of(2026, 5, 7, 9, 0)
                ),
                new StockSignalSummaryResponse(
                        stockReport.getId(), 102L, "일론 머스크 발언 영향", "변동성 확대",
                        SignalEventType.MANAGEMENT, SignalSentiment.MIXED, 70,
                        "CEO 발언에 따른 변동성 확대 여부를 확인해야 합니다.",
                        81.5, 4, LocalDateTime.of(2026, 5, 7, 8, 30)
                )
        );

        when(stockRepository.existsById(stockId)).thenReturn(true);
        when(stockReportRepository.findTopByStockIdOrderByGeneratedAtDesc(stockId))
                .thenReturn(Optional.of(stockReport));
        when(stockReportSignalRepository.findSignalSummariesByStockReportId(stockReport.getId()))
                .thenReturn(signals);

        StockLatestReportResponse response = stockService.findLatestReport(stockId);

        assertNotNull(response);
        assertEquals(stockId, response.stockId());
        assertEquals("테슬라", response.stockName());
        assertEquals("테슬라 종합 리포트", response.report());
        assertEquals(2, response.signals().size());
        assertEquals(101L, response.signals().get(0).signalId());
        assertEquals(SignalEventType.PRODUCT, response.signals().get(0).eventType());
        assertEquals("제품/사업", response.signals().get(0).eventTypeLabel());
        assertEquals(SignalSentiment.POSITIVE, response.signals().get(0).sentiment());
        assertEquals("긍정", response.signals().get(0).sentimentLabel());
        assertEquals(78, response.signals().get(0).confidence());
        verify(stockReportRepository).findTopByStockIdOrderByGeneratedAtDesc(stockId);
    }

    @Test
    void 최신_종목_리포트가_없으면_null을_반환한다() {
        Long stockId = 1L;

        when(stockRepository.existsById(stockId)).thenReturn(true);
        when(stockReportRepository.findTopByStockIdOrderByGeneratedAtDesc(stockId))
                .thenReturn(Optional.empty());

        StockLatestReportResponse response = stockService.findLatestReport(stockId);

        assertNull(response);
    }

    @Test
    void 존재하지_않는_종목의_리포트_조회시_예외가_발생한다() {
        Long stockId = 999L;
        when(stockRepository.existsById(stockId)).thenReturn(false);

        assertThrows(StockNotFoundException.class, () -> stockService.findLatestReport(stockId));
    }
}
