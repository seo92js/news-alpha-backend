package com.seo92js.news_alpha_backend.domain.signal.controller;

import com.seo92js.news_alpha_backend.domain.signal.dto.SignalResponse;
import com.seo92js.news_alpha_backend.domain.signal.service.SignalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/signals")
public class SignalController {

    private final SignalService signalService;

    @GetMapping
    public List<SignalResponse> findRecentSignals() {
        return signalService.findRecentSignals();
    }
}
