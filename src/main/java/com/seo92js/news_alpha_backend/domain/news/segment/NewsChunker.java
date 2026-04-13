package com.seo92js.news_alpha_backend.domain.news.segment;

import com.seo92js.news_alpha_backend.common.AppConstants;
import com.seo92js.news_alpha_backend.domain.news.Sentence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NewsChunker {

    private final int maxChunkSize;
    private final int minChunkSize;
    private final int overlapSentences;

    public NewsChunker(
            @Value("${naver.news.splitter.max-chunk-size}") int maxChunkSize,
            @Value("${naver.news.splitter.min-chunk-size}") int minChunkSize,
            @Value("${naver.news.splitter.overlap-sentences}") int overlapSentences) {
        this.maxChunkSize = maxChunkSize;
        this.minChunkSize = minChunkSize;
        this.overlapSentences = overlapSentences;
    }

    public List<String> chunk(List<Sentence> sentences) {
        List<String> chunks = new ArrayList<>();
        Deque<Sentence> currentChunk = new ArrayDeque<>();

        makeChunks(chunks, currentChunk, sentences);
        appendLastChunk(chunks, currentChunk);

        return chunks;
    }

    private void makeChunks(List<String> chunks, Deque<Sentence> deque, List<Sentence> sentences) {
        int chunkLength = 0;

        for (Sentence sentence : sentences) {

            if (chunkLength + sentence.length() > maxChunkSize) {
                chunks.add(joinSentences(deque));

                while (deque.size() > overlapSentences) {
                    chunkLength -= deque.pollFirst().length() + 1;
                }
            }

            deque.addLast(sentence);
            chunkLength += sentence.length() + 1;
        }
    }

    private void appendLastChunk(List<String> chunks, Deque<Sentence> currentChunk) {
        String lastChunk = joinSentences(currentChunk);
        if (!chunks.isEmpty() && lastChunk.length() < minChunkSize) {

            chunks.add(chunks.remove(chunks.size() - 1) + AppConstants.SPACE + lastChunk);
        }
        else {

            chunks.add(lastChunk);
        }
    }

    private String joinSentences(Deque<Sentence> sentences) {

        return sentences.stream()
                .map(Sentence::text)
                .collect(Collectors.joining(AppConstants.SPACE));
    }
}
