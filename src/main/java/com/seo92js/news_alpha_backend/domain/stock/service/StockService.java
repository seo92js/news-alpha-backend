package com.seo92js.news_alpha_backend.domain.stock.service;

import com.seo92js.news_alpha_backend.domain.ai.service.AiService;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalEvidenceRepository;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalRepository;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import com.seo92js.news_alpha_backend.domain.stock.StockReport;
import com.seo92js.news_alpha_backend.domain.stock.dto.*;
import com.seo92js.news_alpha_backend.domain.stock.exception.DuplicateStockException;
import com.seo92js.news_alpha_backend.domain.stock.exception.DuplicateStockKeywordException;
import com.seo92js.news_alpha_backend.domain.stock.exception.StockNotFoundException;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockKeywordRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportSignalRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockKeywordRepository stockKeywordRepository;
    private final StockReportRepository stockReportRepository;
    private final StockReportSignalRepository stockReportSignalRepository;
    private final SignalRepository signalRepository;
    private final SignalEvidenceRepository signalEvidenceRepository;
    private final AiService aiService;
    private final TransactionTemplate transactionTemplate;

    /**
     * 종목 저장
     */
    public StockResponse save(StockSaveRequest request) {
        if (stockRepository.existsByTickerAndMarket(request.ticker(), request.market())) {
            throw new DuplicateStockException(request.ticker(), request.market());
        }

        Stock tempStock = Stock.of(request.ticker(), request.name(), request.market());

        // DB 커넥션 미점
        List<String> autoKeywords = aiService.generateKeywordsForStock(tempStock);

        // 외부 통신 성공 시에만 영속화
        return transactionTemplate.execute(status -> {
            if (stockRepository.existsByTickerAndMarket(request.ticker(), request.market())) {
                throw new DuplicateStockException(request.ticker(), request.market());
            }

            Stock stock = stockRepository.save(tempStock);
            for (String keyword : autoKeywords) {
                stockKeywordRepository.save(StockKeyword.of(stock, keyword));
            }

            return StockResponse.from(stock, findKeywordResponses(stock.getId()));
        });
    }

    /**
     * 종목 목록을 수집 키워드와 함께 조회
     */
    @Transactional(readOnly = true)
    public List<StockResponse> findAll() {
        return stockRepository.findAll().stream()
                .map(stock -> StockResponse.from(stock, findKeywordResponses(stock.getId())))
                .toList();
    }

    /**
     * 특정 종목에 뉴스 수집용 키워드 추가
     */
    @Transactional
    public StockKeywordResponse addKeyword(Long stockId, StockKeywordSaveRequest request) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException(stockId));

        if (stockKeywordRepository.existsByStockIdAndKeyword(stockId, request.keyword())) {
            throw new DuplicateStockKeywordException(stockId, request.keyword());
        }

        StockKeyword stockKeyword = stockKeywordRepository.save(StockKeyword.of(stock, request.keyword()));
        return StockKeywordResponse.from(stockKeyword);
    }

    /**
     * 특정 종목의 뉴스 수집용 키워드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<StockKeywordResponse> findKeywords(Long stockId) {
        if (!stockRepository.existsById(stockId)) {
            throw new StockNotFoundException(stockId);
        }
        return findKeywordResponses(stockId);
    }

    /**
     * 특정 종목의 가장 최근 생성된 리포트 1건 조회
     */
    @Transactional(readOnly = true)
    public StockLatestReportResponse findLatestReport(Long stockId) {
        if (!stockRepository.existsById(stockId)) {
            throw new StockNotFoundException(stockId);
        }

        return stockReportRepository.findTopByStockIdOrderByGeneratedAtDesc(stockId)
                .map(stockReport -> StockLatestReportResponse.from(
                        stockReport,
                        stockReportSignalRepository.findSignalSummariesByStockReportId(stockReport.getId())
                ))
                .orElse(null);
    }

    private List<StockKeywordResponse> findKeywordResponses(Long stockId) {
        return stockKeywordRepository.findByStockId(stockId).stream()
                .map(StockKeywordResponse::from)
                .toList();
    }

    /**
     * 전체 종목의 최신 시그널 레포트 조회
     */
    @Transactional(readOnly = true)
    public List<StockLatestReportResponse> findLatestReports() {
        List<StockReport> latestReports = stockReportRepository.findLatestStockReports();
        if (latestReports.isEmpty()) return List.of();

        List<Long> reportIds = latestReports.stream().map(StockReport::getId).toList();
        Map<Long, List<StockSignalSummaryResponse>> signalsByStockReportId =
                stockReportSignalRepository.findSignalSummariesByStockReportIds(reportIds).stream()
                        .collect(Collectors.groupingBy(StockSignalSummaryResponse::stockReportId));

        return latestReports.stream()
                .map(report -> StockLatestReportResponse.from(
                        report,
                        signalsByStockReportId.getOrDefault(report.getId(), List.of())
                ))
                .toList();
    }

    /**
     * 종목 삭제
     */
    @Transactional
    public void delete(Long stockId) {
        stockReportSignalRepository.deleteByStockReportStockId(stockId);
        signalEvidenceRepository.deleteBySignalStockId(stockId);
        stockReportRepository.deleteByStockId(stockId);
        signalRepository.deleteByStockId(stockId);
        stockKeywordRepository.deleteByStockId(stockId);
        stockRepository.deleteById(stockId);
    }

    /**
     * 키워드 삭제
     */
    @Transactional
    public void deleteKeyword(Long keywordId) {
        stockKeywordRepository.deleteById(keywordId);
    }
}
