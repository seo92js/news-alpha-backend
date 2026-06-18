package com.seo92js.news_alpha_backend.domain.stock.service;

import com.seo92js.news_alpha_backend.domain.signal.SignalEventType;
import com.seo92js.news_alpha_backend.domain.signal.SignalSentiment;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockReport;
import com.seo92js.news_alpha_backend.domain.ai.service.AiService;
import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockLatestReportResponse;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockResponse;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockSaveRequest;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockSignalSummaryResponse;
import com.seo92js.news_alpha_backend.domain.stock.exception.KeywordGenerationException;
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
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private AiService aiService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private StockService stockService;

    @Test
    void 종목_저장_시_LLM_키워드_추출에_성공하면_정상적으로_키워드와_함께_종목을_반환한다() {
        StockSaveRequest request = new StockSaveRequest("TSLA", "테슬라", "NASDAQ");
        Stock stock = Stock.of("TSLA", "테슬라", "NASDAQ");
        ReflectionTestUtils.setField(stock, "id", 1L);

        StockKeyword kw1 = StockKeyword.of(stock, "일론 머스크");
        StockKeyword kw2 = StockKeyword.of(stock, "자율주행");

        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        when(stockRepository.existsByTickerAndMarket("TSLA", "NASDAQ")).thenReturn(false);
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        when(aiService.generateKeywordsForStock(any(Stock.class))).thenReturn(List.of("일론 머스크", "자율주행"));
        when(stockKeywordRepository.save(any(StockKeyword.class))).thenReturn(kw1).thenReturn(kw2);
        when(stockKeywordRepository.findByStockId(1L)).thenReturn(List.of(kw1, kw2));

        StockResponse response = stockService.save(request);

        assertNotNull(response);
        assertEquals("테슬라", response.name());
        assertEquals("TSLA", response.ticker());
        assertEquals(2, response.keywords().size());
        assertEquals("일론 머스크", response.keywords().get(0).keyword());
        assertEquals("자율주행", response.keywords().get(1).keyword());
    }

    @Test
    void 종목_저장_시_LLM_키워드_추출에_실패하면_예외가_발생한다() {
        StockSaveRequest request = new StockSaveRequest("TSLA", "테슬라", "NASDAQ");

        when(stockRepository.existsByTickerAndMarket("TSLA", "NASDAQ")).thenReturn(false);
        when(aiService.generateKeywordsForStock(any(Stock.class)))
                .thenThrow(new KeywordGenerationException("테슬라", "LLM 호출 타임아웃"));

        assertThrows(KeywordGenerationException.class, () -> stockService.save(request));
    }

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
