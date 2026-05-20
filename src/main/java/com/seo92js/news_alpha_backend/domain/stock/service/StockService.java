package com.seo92js.news_alpha_backend.domain.stock.service;

import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.StockKeyword;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockKeywordResponse;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockKeywordSaveRequest;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockLatestReportResponse;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockResponse;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockSaveRequest;
import com.seo92js.news_alpha_backend.domain.stock.exception.DuplicateStockException;
import com.seo92js.news_alpha_backend.domain.stock.exception.DuplicateStockKeywordException;
import com.seo92js.news_alpha_backend.domain.stock.exception.StockNotFoundException;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalEvidenceRepository;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockKeywordRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockReportSignalRepository;
import com.seo92js.news_alpha_backend.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockKeywordRepository stockKeywordRepository;
    private final StockReportRepository stockReportRepository;
    private final StockReportSignalRepository stockReportSignalRepository;
    private final SignalRepository signalRepository;
    private final SignalEvidenceRepository signalEvidenceRepository;

    /**
     * 종목 저장
     */
    @Transactional
    public StockResponse save(StockSaveRequest request) {
        if (stockRepository.existsByTickerAndMarket(request.ticker(), request.market())) {
            throw new DuplicateStockException(request.ticker(), request.market());
        }

        Stock stock = stockRepository.save(Stock.of(request.ticker(), request.name(), request.market()));
        return StockResponse.from(stock, List.of());
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
