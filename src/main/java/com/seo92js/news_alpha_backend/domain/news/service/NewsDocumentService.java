package com.seo92js.news_alpha_backend.domain.news.service;

import com.seo92js.news_alpha_backend.common.AppConstants;
import com.seo92js.news_alpha_backend.domain.ai.dto.NewsMetadata;
import com.seo92js.news_alpha_backend.domain.ai.service.VectorStoreService;
import com.seo92js.news_alpha_backend.domain.news.News;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsDocumentService {

    private final VectorStoreService vectorStoreService;

    /**
     * 저장된 뉴스 목록을 기사당 단 1개의 대표 청크(제목 + 요약)로 임베딩하여 벡터 스토어에 저장
     */
    public void process(List<News> newsList) {
        if (newsList.isEmpty()) return;

        for (News news : newsList) {
            List<Document> documents = newsToDocument(news);
            if (documents.isEmpty()) continue;

            try {
                vectorStoreService.save(documents);
            } catch (Exception e) {
                log.warn("벡터 변환 및 저장에 실패했습니다. 키워드 : {}", news.getKeyword(), e);
            }
        }
    }

    /**
     * 뉴스 제목(Title)과 요약(Description)만 결합해 단 1개의 대표 청크 Document 생성
     */
    private List<Document> newsToDocument(News news) {
        if (news.getTitle() == null || news.getTitle().isBlank()) return List.of();

        String title = cleanHtml(news.getTitle());
        String description = cleanHtml(news.getDescription());

        // 제목과 요약을 합쳐 단일 대표 청크
        String chunkText = title + " - " + description;

        String uuid = UUID.nameUUIDFromBytes(
                (news.getId() + AppConstants.HYPHEN + "0").getBytes(StandardCharsets.UTF_8)
        ).toString();

        Map<String, Object> metadata = new NewsMetadata(news, 0).toMap();
        Document document = new Document(uuid, chunkText, metadata);

        return List.of(document);
    }

    /**
     * 네이버 API 검색 결과에 들어있는 <b> 등의 HTML 태그 및 &quot; 등 엔티티 문자열 제거 정제
     */
    private String cleanHtml(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "")
                .replace("&quot;", "\"")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .trim();
    }
}
