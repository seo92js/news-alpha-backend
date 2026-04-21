package com.seo92js.news_alpha_backend.domain.stock.controller;

import com.seo92js.news_alpha_backend.domain.stock.dto.StockKeywordResponse;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockKeywordSaveRequest;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockResponse;
import com.seo92js.news_alpha_backend.domain.stock.dto.StockSaveRequest;
import com.seo92js.news_alpha_backend.domain.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    @PostMapping
    public StockResponse save(@RequestBody StockSaveRequest request) {
        return stockService.save(request);
    }

    @GetMapping
    public List<StockResponse> findAll() {
        return stockService.findAll();
    }

    @PostMapping("/{stockId}/keywords")
    public StockKeywordResponse addKeyword(
            @PathVariable Long stockId,
            @RequestBody StockKeywordSaveRequest request
    ) {
        return stockService.addKeyword(stockId, request);
    }

    @GetMapping("/{stockId}/keywords")
    public List<StockKeywordResponse> findKeywords(@PathVariable Long stockId) {
        return stockService.findKeywords(stockId);
    }
}
