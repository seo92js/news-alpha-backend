package com.seo92js.news_alpha_backend.domain.news.service;

import com.seo92js.news_alpha_backend.common.AppConstants;
import com.seo92js.news_alpha_backend.domain.ai.dto.NewsMetadata;
import com.seo92js.news_alpha_backend.domain.ai.service.VectorStoreService;
import com.seo92js.news_alpha_backend.domain.news.News;
import com.seo92js.news_alpha_backend.domain.news.Sentence;
import com.seo92js.news_alpha_backend.domain.news.segment.NewsChunker;
import com.seo92js.news_alpha_backend.domain.news.segment.SentenceSplitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsDocumentService {

    private final SentenceSplitter sentenceSplitter;
    private final NewsChunker newsChunker;
    private final VectorStoreService vectorStoreService;

    private static final List<String> NOISE_START_PATTERNS = List.of("◎", "ⓒ", "저작권자");
    private static final List<String> NOISE_CONTAIN_PATTERNS = List.of("무단전재", "재배포 금지", "@");
    private static final List<String> NOISE_EXACT_PATTERNS = List.of("ADVERTISEMENT");

    public void process(List<News> newsList) {
        if (newsList.isEmpty()) return;

        for (News news : newsList) {

            List<Document> documents = newsToDocument(news);
            if (documents.isEmpty()) continue;

            try {

                vectorStoreService.save(documents);

            }
            catch (Exception e) {

                log.warn("벡터 변환 및 저장에 실패했습니다. 키워드 : {}", news.getKeyword(), e);
            }
        }
    }

    private List<Document> newsToDocument(News news) {
        if (news.getContent() == null || news.getContent().isBlank()) return List.of();

        List<Sentence> sentences = sentenceSplitter.splitBySentence(news.getContent());

        List<Sentence> filteredNoise = sentences.stream()
                .filter(s -> isNotNoise(s.text()))
                .toList();
        if (filteredNoise.isEmpty()) return List.of();

        List<String> chunks = newsChunker.chunk(filteredNoise);

        return IntStream.range(0, chunks.size())
                .mapToObj(i -> {
                    String uuid = UUID.nameUUIDFromBytes(
                            (news.getId() + AppConstants.HYPHEN + i).getBytes(StandardCharsets.UTF_8)
                    ).toString();
                    Map<String, Object> metadata = new NewsMetadata(news, i).toMap();
                    return new Document(uuid, chunks.get(i), metadata);
                })
                .toList();
    }

    private boolean isNotNoise(String sentence) {
        return !NOISE_EXACT_PATTERNS.contains(sentence)
                && NOISE_START_PATTERNS.stream().noneMatch(sentence::startsWith)
                && NOISE_CONTAIN_PATTERNS.stream().noneMatch(sentence::contains);
    }
}
