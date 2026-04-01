package com.seo92js.news_alpha_backend.domain.ai.service;

import com.seo92js.news_alpha_backend.domain.ai.exception.EmbeddingFailureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;

    public void save(List<Document> documents, String keywords) {

        if (documents == null || documents.isEmpty()) return;

        try {

            vectorStore.accept(documents);
        }
        catch (Exception e) {
            throw new EmbeddingFailureException(keywords);
        }
    }
}
