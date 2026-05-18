package com.seo92js.news_alpha_backend.news.service;

import com.seo92js.news_alpha_backend.config.security.jwt.JwtTokenProvider;
import com.seo92js.news_alpha_backend.domain.news.News;
import com.seo92js.news_alpha_backend.domain.news.service.NewsPipelineService;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalEvidenceRepository;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalRepository;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import com.seo92js.news_alpha_backend.domain.stock.StockReport;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockSignalSummaryResponse;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockKeywordRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportSignalRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockRepository;
import com.seo92js.news_alpha_backend.domain.stock.service.StockReportService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithMockUser
@SpringBootTest
class NewsServiceTest {
    @Autowired
    NewsPipelineService newsPipelineService;

    @Autowired
    SignalRepository signalRepository;

    @Autowired
    SignalEvidenceRepository signalEvidenceRepository;

    @Autowired
    StockRepository stockRepository;

    @Autowired
    StockKeywordRepository stockKeywordRepository;

    @Autowired
    StockReportRepository stockReportRepository;

    @Autowired
    StockReportSignalRepository stockReportSignalRepository;

    @Autowired
    StockReportService stockReportService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Disabled
    @Test
    void collectEmbedDetectSignalAndGenerateStockReport() {
        Stock stock = stockRepository.findByTickerAndMarket("A000660", "KOSPI")
                .orElseGet(() -> stockRepository.save(Stock.of("A000660", "SK하이닉스", "KOSPI")));
        StockKeyword stockKeyword1 = findOrCreateKeyword(stock, "하이닉스");
        StockKeyword stockKeyword2 = findOrCreateKeyword(stock, "D램");

        long signalCountBefore = signalRepository.count();
        long evidenceCountBefore = signalEvidenceRepository.count();
        long stockReportCountBefore = stockReportRepository.count();

        List<News> discoveredNews1 = newsPipelineService.collectEmbedAndDetect(stockKeyword1);
        List<News> discoveredNews2 = newsPipelineService.collectEmbedAndDetect(stockKeyword2);

        stockReportService.generateLatestReport(stock);

        long signalCountAfter = signalRepository.count();
        long evidenceCountAfter = signalEvidenceRepository.count();
        long stockReportCountAfter = stockReportRepository.count();
        Optional<StockReport> latestStockReport = stockReportRepository.findTopByStockIdOrderByGeneratedAtDesc(stock.getId());

        assertNotNull(discoveredNews1);
        assertNotNull(discoveredNews2);
        assertTrue(latestStockReport.isPresent(), "종목 리포트가 생성되어야 함");

        List<StockSignalSummaryResponse> reportSignals = stockReportSignalRepository.findSignalSummariesByStockReportId(
                latestStockReport.orElseThrow().getId()
        );
        assertTrue(reportSignals.size() <= 5);
        reportSignals.forEach(signal -> {
            assertNotNull(signal.eventType());
            assertNotNull(signal.eventTypeLabel());
            assertNotNull(signal.sentiment());
            assertNotNull(signal.sentimentLabel());
            assertNotNull(signal.confidence());
            assertNotNull(signal.investorSummary());
        });

        System.out.printf(
                "stock pipeline result - stock=%s, keyword1=%s, keyword2=%s, discoveredNews1=%d, discoveredNews2=%d, signals=%d->%d, evidences=%d->%d, stockReports=%d->%d, latestStockReport=%s, reportSignals=%d%n",
                stock.getName(),
                stockKeyword1.getKeyword(),
                stockKeyword2.getKeyword(),
                discoveredNews1.size(),
                discoveredNews2.size(),
                signalCountBefore,
                signalCountAfter,
                evidenceCountBefore,
                evidenceCountAfter,
                stockReportCountBefore,
                stockReportCountAfter,
                latestStockReport.isPresent(),
                reportSignals.size()
        );
    }

    private StockKeyword findOrCreateKeyword(Stock stock, String keyword) {
        return stockKeywordRepository.findByStockIdAndKeyword(stock.getId(), keyword)
                .orElseGet(() -> stockKeywordRepository.save(StockKeyword.of(stock, keyword)));
    }
}
