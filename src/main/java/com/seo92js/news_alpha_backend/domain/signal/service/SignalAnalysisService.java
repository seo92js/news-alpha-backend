package com.seo92js.news_alpha_backend.domain.signal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seo92js.news_alpha_backend.domain.ai.service.AiService;
import com.seo92js.news_alpha_backend.domain.signal.SignalEventType;
import com.seo92js.news_alpha_backend.domain.signal.SignalSentiment;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalAnalysisService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Value("classpath:/prompts/signal-analysis.st")
    private Resource signalAnalysisPromptResource;

    /**
     * 군집 뉴스를 투자자용 시그널 필드로 분석하고, 실패 시 보수 기본값을 반환
     */
    public SignalAnalysisResult analyze(
            Stock stock,
            String keyword,
            String defaultTitle,
            String defaultSummary,
            int clusterSize,
            long burstCount,
            List<SignalAnalysisNews> news
    ) {
        try {
            String response = aiService.chat(buildPrompt(stock, keyword, clusterSize, burstCount, news));
            return parseResponse(response, defaultTitle, defaultSummary);
        } catch (Exception e) {
            log.warn("시그널 LLM 분석에 실패했습니다. stockId={}, keyword={}", stock.getId(), keyword, e);
            return SignalAnalysisResult.defaultResult(defaultTitle, defaultSummary);
        }
    }

    private String buildPrompt(Stock stock, String keyword, int clusterSize, long burstCount, List<SignalAnalysisNews> news) {
        String newsBlock = news.stream()
                .map(item -> "- 제목: %s | 발행시각: %s | 내용: %s"
                        .formatted(
                                item.title(),
                                item.publishedAt().format(DATE_TIME_FORMATTER),
                                item.preview()
                        ))
                .collect(Collectors.joining("\n"));

        PromptTemplate promptTemplate = new PromptTemplate(signalAnalysisPromptResource);
        return promptTemplate.render(Map.of(
                "stockName", stock.getName(),
                "ticker", stock.getTicker(),
                "market", stock.getMarket(),
                "keyword", keyword,
                "clusterSize", Integer.toString(clusterSize),
                "burstCount", Long.toString(burstCount),
                "newsBlock", newsBlock
        ));
    }

    private SignalAnalysisResult parseResponse(String response, String defaultTitle, String defaultSummary) throws Exception {
        JsonNode root = objectMapper.readTree(stripCodeFence(response));
        String title = textOrDefault(root, "title", defaultTitle);
        String summary = textOrDefault(root, "summary", defaultSummary);
        SignalEventType eventType = enumOrDefault(root, "eventType", SignalEventType.class, SignalEventType.ETC);
        SignalSentiment sentiment = enumOrDefault(root, "sentiment", SignalSentiment.class, SignalSentiment.NEUTRAL);
        int confidence = clamp(root.path("confidence").asInt(50), 0, 100);
        String investorSummary = textOrDefault(root, "investorSummary", summary);

        return new SignalAnalysisResult(title, summary, eventType, sentiment, confidence, investorSummary);
    }

    private String stripCodeFence(String response) {
        if (response == null) {
            return "{}";
        }
        return response
                .replaceFirst("(?s)^\\s*```json\\s*", "")
                .replaceFirst("(?s)^\\s*```\\s*", "")
                .replaceFirst("(?s)\\s*```\\s*$", "")
                .trim();
    }

    private String textOrDefault(JsonNode root, String fieldName, String defaultValue) {
        String value = root.path(fieldName).asText(null);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private <T extends Enum<T>> T enumOrDefault(JsonNode root, String fieldName, Class<T> enumClass, T defaultValue) {
        String value = root.path(fieldName).asText(null);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
