package com.seo92js.news_alpha_backend.domain.signal.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SignalSimilarityPolicy {

    private static final Pattern NON_WORD_PATTERN = Pattern.compile("[^0-9a-zA-Z가-힣]+");

    private static final Set<String> STOPWORDS = Set.of(
            "관련", "따른", "대한", "통한", "위한", "있는", "없는", "으로", "에서",
            "하며", "하고", "되는", "보임", "나타남", "것으로", "있습니다", "습니다"
    );

    public boolean hasNewsOverlap(Set<Long> firstNewsIds, Set<Long> secondNewsIds, double threshold) {
        return calculateOverlap(firstNewsIds, secondNewsIds) >= threshold;
    }

    public boolean isTextSimilar(String firstText, String secondText, double threshold) {
        return calculateOverlap(tokenize(firstText), tokenize(secondText)) >= threshold;
    }

    public Set<String> tokenize(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        String normalized = NON_WORD_PATTERN.matcher(value.toLowerCase())
                .replaceAll(" ")
                .trim();
        if (normalized.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(normalized.split("\\s+"))
                .filter(token -> token.length() >= 2)
                .filter(token -> !STOPWORDS.contains(token))
                .collect(Collectors.toSet());
    }

    public <T> double calculateOverlap(Set<T> first, Set<T> second) {
        int smallerSize = Math.min(first.size(), second.size());
        if (smallerSize == 0) {
            return 0d;
        }
        long intersectionCount = first.stream()
                .filter(second::contains)
                .count();
        return (double) intersectionCount / smallerSize;
    }
}
