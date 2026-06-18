package com.seo92js.news_alpha_backend.domain.stock.service;

import com.seo92js.news_alpha_backend.domain.ai.service.AiService;
import com.seo92js.news_alpha_backend.domain.signal.Signal;
import com.seo92js.news_alpha_backend.domain.signal.SignalEventType;
import com.seo92js.news_alpha_backend.domain.signal.SignalSentiment;
import com.seo92js.news_alpha_backend.domain.signal.SignalType;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceRow;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalEvidenceRepository;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalRepository;
import com.seo92js.news_alpha_backend.domain.signal.service.SignalSimilarityPolicy;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockReport;
import com.seo92js.news_alpha_backend.domain.stock.StockReportSignal;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportSignalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockReportServiceTest {

    @Mock
    private AiService aiService;

    @Mock
    private SignalRepository signalRepository;

    @Mock
    private SignalEvidenceRepository signalEvidenceRepository;

    @Mock
    private StockReportRepository stockReportRepository;

    @Mock
    private StockReportSignalRepository stockReportSignalRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    private StockReportService stockReportService;

    @BeforeEach
    void setUp() {
        stockReportService = new StockReportService(
                aiService,
                signalRepository,
                signalEvidenceRepository,
                new SignalSimilarityPolicy(),
                stockReportRepository,
                stockReportSignalRepository,
                transactionTemplate
        );
        ReflectionTestUtils.setField(
                stockReportService,
                "stockReportPromptResource",
                new ByteArrayResource("signals: {signalBlock}".getBytes(StandardCharsets.UTF_8))
        );
    }

    @Test
    void 리포트_생성시_의미상_중복_시그널은_대표_시그널만_선택한다() {
        Stock stock = Stock.of("A000660", "SK하이닉스", "KOSPI");
        ReflectionTestUtils.setField(stock, "id", 1L);

        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        Signal firstMacroSignal = signal(
                1L,
                stock,
                "하이닉스",
                "미·이란 종전 합의에 따른 증시 불안 해소 및 SK하이닉스 주가 급등",
                "미국과 이란의 종전 합의로 지정학적 리스크가 완화되며 코스피와 반도체 대형주가 동반 강세를 보임",
                100d,
                6
        );
        Signal duplicatedMacroSignal = signal(
                2L,
                stock,
                "엔비디아",
                "미-이란 종전 합의에 따른 투자심리 개선 및 반도체주 동반 급등",
                "미국과 이란의 종전 합의가 투자심리를 개선하며 SK하이닉스를 포함한 반도체주 상승세가 나타남",
                98d,
                4
        );
        Signal supplyChainSignal = signal(
                3L,
                stock,
                "엔비디아",
                "엔비디아 핵심 공급망으로서의 SK하이닉스 입지 재확인",
                "엔비디아 AI 반도체 공급망에서 SK하이닉스의 HBM 공급사 지위가 재확인됨",
                92d,
                3
        );

        when(signalRepository.findRecentSignalsByStockId(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(firstMacroSignal, duplicatedMacroSignal, supplyChainSignal));
        when(signalEvidenceRepository.findEvidenceRowsBySignalIds(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(
                        evidence(1L, 11L),
                        evidence(1L, 12L),
                        evidence(2L, 21L),
                        evidence(2L, 22L),
                        evidence(3L, 31L),
                        evidence(3L, 32L)
                ));
        when(aiService.chat(anyString())).thenReturn("리포트");
        when(stockReportRepository.save(any(StockReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockReportSignalRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        stockReportService.generateLatestReport(stock);

        ArgumentCaptor<StockReport> reportCaptor = ArgumentCaptor.forClass(StockReport.class);
        verify(stockReportRepository).save(reportCaptor.capture());
        assertEquals(2, reportCaptor.getValue().getSignalCount());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StockReportSignal>> reportSignalsCaptor = ArgumentCaptor.forClass(List.class);
        verify(stockReportSignalRepository).saveAll(reportSignalsCaptor.capture());
        List<StockReportSignal> reportSignals = reportSignalsCaptor.getValue();

        assertEquals(2, reportSignals.size());
        assertEquals(1L, reportSignals.get(0).getSignal().getId());
        assertEquals(3L, reportSignals.get(1).getSignal().getId());
    }

    private Signal signal(
            Long id,
            Stock stock,
            String keyword,
            String title,
            String summary,
            double score,
            int relatedNewsCount
    ) {
        Signal signal = Signal.of(
                "signal-" + id,
                stock,
                SignalType.EMERGING_CLUSTER,
                keyword,
                title,
                summary,
                SignalEventType.MARKET,
                SignalSentiment.POSITIVE,
                90,
                summary,
                score,
                relatedNewsCount,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(signal, "id", id);
        return signal;
    }

    private SignalEvidenceRow evidence(Long signalId, Long newsId) {
        return new SignalEvidenceRow(signalId, newsId, 1, "근거 기사", "https://example.com/" + newsId, LocalDateTime.now());
    }
}
