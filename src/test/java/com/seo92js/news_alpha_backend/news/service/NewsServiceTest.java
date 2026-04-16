package com.seo92js.news_alpha_backend.news.service;

import com.seo92js.news_alpha_backend.config.security.jwt.JwtTokenProvider;
import com.seo92js.news_alpha_backend.domain.news.News;
import com.seo92js.news_alpha_backend.domain.news.service.NewsPipelineService;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalEvidenceRepository;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalRepository;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockKeywordRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockRepository;
import com.seo92js.news_alpha_backend.domain.stock.service.StockReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    StockReportService stockReportService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Test
    void collectEmbedDetectSignalAndGenerateStockReport() {
        Stock stock = stockRepository.findByTickerAndMarket("TSLA", "NASDAQ")
                .orElseGet(() -> stockRepository.save(Stock.of("TSLA", "테슬라", "NASDAQ")));

        if (!stockKeywordRepository.existsByStockIdAndKeyword(stock.getId(), "테슬라")) {
            stockKeywordRepository.save(StockKeyword.of(stock, "테슬라"));
        }
        if (!stockKeywordRepository.existsByStockIdAndKeyword(stock.getId(), "일론머스크")) {
            stockKeywordRepository.save(StockKeyword.of(stock, "일론머스크"));
        }

        List<StockKeyword> enabledKeywords = stockKeywordRepository.findEnabledWithStock();
        StockKeyword stockKeyword1 = enabledKeywords.stream()
                .filter(sk -> sk.getKeyword().equals("테슬라"))
                .findFirst().orElseThrow();
        StockKeyword stockKeyword2 = enabledKeywords.stream()
                .filter(sk -> sk.getKeyword().equals("일론머스크"))
                .findFirst().orElseThrow();

        long signalCountBefore = signalRepository.count();
        long evidenceCountBefore = signalEvidenceRepository.count();
        long stockReportCountBefore = stockReportRepository.count();

        List<News> savedNews1 = newsPipelineService.collectEmbedAndDetect(stockKeyword1);
        List<News> savedNews2 = newsPipelineService.collectEmbedAndDetect(stockKeyword2);

        stockReportService.generateLatestReport(stock);

        long signalCountAfter = signalRepository.count();
        long evidenceCountAfter = signalEvidenceRepository.count();
        long stockReportCountAfter = stockReportRepository.count();
        Optional<?> latestStockReport = stockReportRepository.findTopByStockIdOrderByGeneratedAtDesc(stock.getId());

        assertNotNull(savedNews1);
        assertNotNull(savedNews2);
        System.out.printf(
                "stock pipeline result - stock=%s, keyword1=%s, keyword2=%s, savedNews1=%d, savedNews2=%d, signals=%d->%d, evidences=%d->%d, stockReports=%d->%d, latestStockReport=%s%n",
                stock.getName(),
                stockKeyword1.getKeyword(),
                stockKeyword2.getKeyword(),
                savedNews1.size(),
                savedNews2.size(),
                signalCountBefore,
                signalCountAfter,
                evidenceCountBefore,
                evidenceCountAfter,
                stockReportCountBefore,
                stockReportCountAfter,
                latestStockReport.isPresent()
        );
    }
}
