package com.seo92js.news_alpha_backend.domain.news;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Component
public class NaverArticleCrawler {
    private final NaverNewsProperties naverNewsProperties;

    /**
     * 네이버 뉴스 페이지가 일반 브라우저 요청처럼 응답하도록 사용하는 User-Agent
     */
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";

    /**
     * 기사 본문 크롤링 요청의 최대 대기 시간
     */
    private static final int TIMEOUT_MS = 10_000;

    /**
     * 네이버 뉴스 본문 영역 후보 selector 목록
     * 언론사/네이버 UI 차이에 따라 본문 id가 달라질 수 있어 순서대로 탐색
     */
    private static final List<String> BODY_SELECTORS = List.of(
            "#dic_area", "#articleBodyContents", "#newsct_article", "#articeBody"
    );

    /**
     * 본문에서 제거할 광고, 저작권, 기자 정보 등 노이즈 selector 목록
     */
    private static final String GARBAGE_SELECTORS =
            "script, style, iframe, .ad, .copyright, .reporter_area, .img_desc, .b_text, .byline, .media_end_head_info";

    /**
     * 네이버 뉴스 기사 본문 크롤링
     */
    public String crawl(String url) {
        if (!naverNewsProperties.isSupportedUrl(url)) {
            return null;
        }

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .get();

            for (String selector : BODY_SELECTORS) {
                Element body = doc.selectFirst(selector);
                if (body != null) {
                    body.select(GARBAGE_SELECTORS).remove();
                    String text = body.text().trim();
                    if (!text.isBlank()) return text;
                }
            }
            return null;
        } catch (Exception e) {
            // 크롤링 실패 시 null 처리
            return null;
        }
    }
}
