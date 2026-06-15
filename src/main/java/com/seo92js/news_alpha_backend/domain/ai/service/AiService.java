package com.seo92js.news_alpha_backend.domain.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.exception.KeywordGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public String chat(String message) {
        return chatModel.call(message);
    }

    /**
     * 종목 정보에 대응하는 네이버 뉴스 검색용 핵심 연관 명사 키워드 2개를 추출
     */
    public List<String> generateKeywordsForStock(Stock stock) {
        String prompt = """
            주식 종목명: %s (티커: %s, 시장: %s)
            위 종목의 주가 변동, 기업 가치, 혹은 주요 비즈니스 이슈에 직간접적으로 가장 강력한 영향을 미치는 핵심 연관 수집 키워드를 정확히 2개만 한글로 추출해 주십시오. (예: 테슬라 -> ["일론 머스크", "자율주행"])
            반드시 한글로 작성하며, 결과는 부연 설명이나 공백 없이 오직 JSON 배열 포맷 `["키워드1", "키워드2"]`로만 출력해 주십시오.
            """.formatted(stock.getName(), stock.getTicker(), stock.getMarket());

        try {
            String response = chat(prompt);
            String cleanedResponse = cleanJsonString(response);
            List<String> keywords = objectMapper.readValue(cleanedResponse, new TypeReference<List<String>>() {});

            if (keywords == null || keywords.size() != 2) {
                throw new KeywordGenerationException(stock.getName(), "추출된 키워드 개수가 2개가 아닙니다. 수신 데이터: " + response);
            }

            log.info("종목 연관 키워드 생성 성공. stock={}, keywords={}", stock.getName(), keywords);
            return keywords;
        } catch (Exception e) {
            log.error("종목 연관 키워드 추출 실패. stock={}", stock.getName(), e);
            throw new KeywordGenerationException(stock.getName(), e.getMessage());
        }
    }

    private String cleanJsonString(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            // ```json ... ``` 형태의 마크다운 블록 정제
            cleaned = cleaned.replaceAll("^```[a-zA-Z]*\\s*|\\s*```$", "").trim();
        }
        return cleaned;
    }
}
