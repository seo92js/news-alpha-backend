package com.seo92js.news_alpha_backend.domain.signal.controller;

import com.seo92js.news_alpha_backend.domain.signal.dto.SignalDetailResponse;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalResponse;
import com.seo92js.news_alpha_backend.domain.signal.service.SignalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{signalId}")
    public SignalDetailResponse findSignalDetail(@PathVariable Long signalId) {
        return signalService.findSignalDetail(signalId);
    }
}
