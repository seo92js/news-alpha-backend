package com.seo92js.news_alpha_backend.scheduler;

import com.seo92js.news_alpha_backend.domain.ai.service.AiService;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockKeywordRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyKeywordRefreshSchedulerTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockKeywordRepository stockKeywordRepository;

    @Mock
    private AiService aiService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private DailyKeywordRefreshScheduler dailyKeywordRefreshScheduler;

    @Test
    void 매일_새벽_스케줄러가_돌면_등록된_모든_종목의_기존_키워드를_지우고_새_키워드로_교체한다() {
        Stock stock = Stock.of("NVDA", "엔비디아", "NASDAQ");
        ReflectionTestUtils.setField(stock, "id", 1L);

        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        when(stockRepository.findAll()).thenReturn(List.of(stock));
        when(aiService.generateKeywordsForStock(stock)).thenReturn(List.of("AI 반도체", "젠슨 황"));

        dailyKeywordRefreshScheduler.refreshAllStockKeywords();

        verify(stockKeywordRepository).deleteByStockId(1L);
        verify(stockKeywordRepository, times(2)).save(any(StockKeyword.class));
    }

    @Test
    void 키워드_갱신_중_에러가_발생해도_다른_종목의_갱신에_영향을_주지_않는다() {
        Stock stock1 = Stock.of("NVDA", "엔비디아", "NASDAQ");
        ReflectionTestUtils.setField(stock1, "id", 1L);
        Stock stock2 = Stock.of("TSLA", "테슬라", "NASDAQ");
        ReflectionTestUtils.setField(stock2, "id", 2L);

        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        when(stockRepository.findAll()).thenReturn(List.of(stock1, stock2));
        when(aiService.generateKeywordsForStock(stock1)).thenThrow(new RuntimeException("LLM 에러"));
        when(aiService.generateKeywordsForStock(stock2)).thenReturn(List.of("일론 머스크", "자율주행"));

        dailyKeywordRefreshScheduler.refreshAllStockKeywords();

        verify(stockKeywordRepository, never()).deleteByStockId(1L);

        verify(stockKeywordRepository).deleteByStockId(2L);
        verify(stockKeywordRepository, times(2)).save(any(StockKeyword.class));
    }
}
