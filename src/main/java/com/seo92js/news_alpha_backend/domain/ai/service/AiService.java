package com.seo92js.news_alpha_backend.domain.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seo92js.news_alpha_backend.domain.news.service.NewsService;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import com.seo92js.news_alpha_backend.domain.stock.exception.KeywordGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final NewsService newsService;

    public String chat(String message) {
        return chatModel.call(message);
    }

    /**
     * 종목 정보에 대응하는 네이버 뉴스 검색용 핵심 연관 명사 키워드 2개를 추출
     */
    public List<String> generateKeywordsForStock(Stock stock) {
        List<String> titles = newsService.fetchRecentNewsTitles(stock.getName());
        String prompt;

        if (titles.isEmpty()) {
            // 뉴스 검색 결과가 없는 경우
            prompt = """
                주식 종목명: %s (티커: %s, 시장: %s)
                위 종목의 주가 변동, 기업 가치, 혹은 주요 비즈니스 이슈에 직간접적으로 가장 강력한 영향을 미치는 핵심 연관 수집 키워드를 정확히 2개만 한글로 추출해 주십시오. (예: 테슬라 -> ["일론 머스크", "자율주행"])
                반드시 한글로 작성하며, 결과는 부연 설명이나 공백 없이 오직 JSON 배열 포맷 `["키워드1", "키워드2"]`로만 출력해 주십시오.
                """.formatted(stock.getName(), stock.getTicker(), stock.getMarket());
        } else {
            String recentTitlesBlock = titles.stream()
                    .map(title -> "- " + title)
                    .collect(Collectors.joining("\n"));

            prompt = """
                주식 종목명: %s (티커: %s, 시장: %s)

                아래는 최근 이 종목과 관련하여 포착된 실제 언론사 뉴스 제목 목록입니다:
                %s

                위 최신 이슈들을 최우선적으로 반영하여, 현재 시장에서 이 종목의 기업 가치 및 주가 변동성에 가장 지배적이고 강력한 영향을 미치는 핵심 연관 수집 키워드를 정확히 2개만 한글로 추출해 주십시오. (예: ["일론 머스크", "자율주행"])
                결과는 부연 설명이나 공백 없이 오직 JSON 배열 포맷 `["키워드1", "키워드2"]`로만 출력해 주십시오.
                """.formatted(stock.getName(), stock.getTicker(), stock.getMarket(), recentTitlesBlock);
        }

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
