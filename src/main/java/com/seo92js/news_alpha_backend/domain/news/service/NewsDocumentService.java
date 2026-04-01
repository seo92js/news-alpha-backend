package com.seo92js.news_alpha_backend.domain.news.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seo92js.news_alpha_backend.common.AppConstants;
import com.seo92js.news_alpha_backend.domain.ai.dto.NewsMetadata;
import com.seo92js.news_alpha_backend.domain.ai.service.VectorStoreService;
import com.seo92js.news_alpha_backend.domain.news.News;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class NewsDocumentService {

    private final TokenTextSplitter tokenTextSplitter;
    private final ObjectMapper objectMapper;
    private final VectorStoreService vectorStoreService;

    public void process(List<News> newsList) {

        if (newsList == null || newsList.isEmpty()) return;

        List<Document> chunks = new ArrayList<>();
        List<String> keywordList = new ArrayList<>();

        for (News news : newsList) {

            chunks.addAll(newsToDocument(news));
            keywordList.add(news.getKeyword());
        }

        vectorStoreService.save(chunks, String.join(AppConstants.LOG_DELIMETER, keywordList));
    }

    private List<Document> newsToDocument(News news) {

        if (news.getContent() == null || news.getContent().isBlank()) return List.of();

        List<Document> splitDocs = tokenTextSplitter.apply(List.of(new Document(news.getContent())));

        return IntStream.range(0, splitDocs.size())
                .mapToObj(i -> {

                    String docId = news.getId() + AppConstants.HYPHEN + i;
                    NewsMetadata newsMetadata = new NewsMetadata(news, i);
                    Map<String, Object> metaData = objectMapper.convertValue(newsMetadata, Map.class);

                    return new Document(docId, splitDocs.get(i).getText(), metaData);
                })
                .toList();
    }
}
