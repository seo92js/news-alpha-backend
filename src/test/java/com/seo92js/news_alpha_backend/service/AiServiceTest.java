package com.seo92js.news_alpha_backend.service;

import com.seo92js.news_alpha_backend.ai.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiServiceTest {
    @Autowired
    private AiService aiService;

    @Test
    void chat() {
        String message = "현재 날씨 알려줘";
        String response = aiService.chat(message);
        System.out.println("AI Response: " + response);
    }
}