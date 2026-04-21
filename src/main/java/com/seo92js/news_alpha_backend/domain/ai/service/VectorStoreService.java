package com.seo92js.news_alpha_backend.domain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;

    /**
     * 청킹된 뉴스 Document 목록을 벡터 스토어에 저장
     */
    public void save(List<Document> documents) {

        vectorStore.accept(documents);
    }

    /**
     * query와 의미적으로 유사한 Document를 벡터 스토어에서 검색
     */
    public List<Document> similaritySearch(String query, int topK, double similarityThreshold) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();
        return vectorStore.similaritySearch(request);
    }
}
