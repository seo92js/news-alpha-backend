package com.seo92js.news_alpha_backend.domain.news.service;

import com.seo92js.news_alpha_backend.common.AppConstants;
import com.seo92js.news_alpha_backend.domain.ai.dto.NewsMetadata;
import com.seo92js.news_alpha_backend.domain.ai.service.VectorStoreService;
import com.seo92js.news_alpha_backend.domain.news.News;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsDocumentService {

    private final TokenTextSplitter tokenTextSplitter;
    private final VectorStoreService vectorStoreService;

    public void process(List<News> newsList) {

        if (newsList == null || newsList.isEmpty()) return;

        for (News news : newsList) {

            List<Document> chunks = newsToDocument(news);

            try {

                vectorStoreService.save(chunks);
            }
            catch (Exception e) {
                log.warn("벡터 변환 및 저장에 실패했습니다. 키워드 : {}", news.getKeyword(), e);
            }
        }

    }

    private List<Document> newsToDocument(News news) {

        if (news.getContent() == null || news.getContent().isBlank()) return List.of();

        List<Document> splitDocs = tokenTextSplitter.apply(List.of(new Document(news.getContent())));

        return IntStream.range(0, splitDocs.size())
                .mapToObj(i -> {

                    String vectorId = news.getId() + AppConstants.HYPHEN + i;
                    String uuid = UUID.nameUUIDFromBytes(vectorId.getBytes(StandardCharsets.UTF_8)).toString();

                    NewsMetadata newsMetadata = new NewsMetadata(news, i);
                    Map<String, Object> metaData = newsMetadata.toMap();

                    return new Document(uuid, splitDocs.get(i).getText(), metaData);
                })
                .toList();
    }
}
