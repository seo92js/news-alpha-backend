package com.seo92js.news_alpha_backend.domain.keyword.controller;

import com.seo92js.news_alpha_backend.domain.keyword.dto.KeywordResponse;
import com.seo92js.news_alpha_backend.domain.keyword.dto.KeywordSaveRequest;
import com.seo92js.news_alpha_backend.domain.keyword.service.KeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keywords")
public class KeywordController {

    private final KeywordService keywordService;

    @PostMapping
    public KeywordResponse save(@RequestBody KeywordSaveRequest request) {
        return keywordService.save(request);
    }

    @GetMapping
    public List<KeywordResponse> findAll() {
        return keywordService.findAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        keywordService.delete(id);
    }
}
