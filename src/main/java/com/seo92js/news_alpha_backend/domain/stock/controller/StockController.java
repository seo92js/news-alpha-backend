package com.seo92js.news_alpha_backend.domain.stock.controller;

import com.seo92js.news_alpha_backend.domain.stock.dto.*;
import com.seo92js.news_alpha_backend.domain.stock.service.KrxStockService;
import com.seo92js.news_alpha_backend.domain.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;
    private final KrxStockService krxStockService;

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

    @GetMapping("/meta")
    public List<StockMetaResponse> findAllMeta() {
        return krxStockService.findAll();
    }
}
