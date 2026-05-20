package com.seo92js.news_alpha_backend.domain.signal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seo92js.news_alpha_backend.domain.ai.service.AiService;
import com.seo92js.news_alpha_backend.domain.signal.SignalEventType;
import com.seo92js.news_alpha_backend.domain.signal.SignalSentiment;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignalAnalysisServiceTest {

    @Mock
    private AiService aiService;

    private SignalAnalysisService signalAnalysisService;

    @BeforeEach
    void setUp() {
        signalAnalysisService = new SignalAnalysisService(aiService, new ObjectMapper());
        ReflectionTestUtils.setField(
                signalAnalysisService,
                "signalAnalysisPromptResource",
                new ClassPathResource("prompts/signal-analysis.st")
        );
    }

    @Test
    void LLM_JSON_응답을_투자자용_시그널_분석값으로_변환() {
        when(aiService.chat(anyString())).thenReturn("""
                {
                  "title": "테슬라 로보택시 기대감 확대",
                  "summary": "로보택시 관련 보도가 집중되고 있습니다.",
                  "eventType": "PRODUCT",
                  "sentiment": "POSITIVE",
                  "confidence": 82,
                  "investorSummary": "상용화 일정과 규제 승인 여부를 함께 확인해야 합니다."
                }
                """);

        String preview = "로보택시 상용화 기대가 커지고 있습니다.";

        SignalAnalysisResult result = signalAnalysisService.analyze(
                Stock.of("TSLA", "테슬라", "NASDAQ"),
                "테슬라",
                "기본 제목",
                "기본 요약",
                4,
                2,
                List.of(new SignalAnalysisNews("로보택시 뉴스", "https://example.com", LocalDateTime.of(2026, 5, 18, 10, 0), preview))
        );

        assertEquals("테슬라 로보택시 기대감 확대", result.title());
        assertEquals(SignalEventType.PRODUCT, result.eventType());
        assertEquals(SignalSentiment.POSITIVE, result.sentiment());
        assertEquals(82, result.confidence());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiService).chat(promptCaptor.capture());
        assertTrue(promptCaptor.getValue().contains("내용: " + preview));
        assertTrue(promptCaptor.getValue().contains("PRODUCT: 제품, 서비스, 기술, 출시, 사업 확장, 파트너십 관련"));
    }

    @Test
    void LLM_분석_실패시_보수_기본값을_반환() {
        when(aiService.chat(anyString())).thenThrow(new RuntimeException("quota exceeded"));

        SignalAnalysisResult result = signalAnalysisService.analyze(
                Stock.of("TSLA", "테슬라", "NASDAQ"),
                "테슬라",
                "기본 제목",
                "기본 요약",
                4,
                2,
                List.of(new SignalAnalysisNews("로보택시 뉴스", "https://example.com", LocalDateTime.of(2026, 5, 18, 10, 0), "로보택시 상용화 기대가 커지고 있습니다."))
        );

        assertEquals("기본 제목", result.title());
        assertEquals("기본 요약", result.summary());
        assertEquals(SignalEventType.ETC, result.eventType());
        assertEquals(SignalSentiment.NEUTRAL, result.sentiment());
        assertEquals(50, result.confidence());
        assertEquals("기본 요약", result.investorSummary());
    }
}
