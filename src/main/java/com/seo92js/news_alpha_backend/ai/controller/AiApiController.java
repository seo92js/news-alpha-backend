package com.seo92js.news_alpha_backend.ai.controller;

import com.seo92js.news_alpha_backend.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiApiController {

    private final AiService aiService;

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message", defaultValue = "현재 날씨 알려줘") String message) {
        return aiService.chat(message);
    }
}
