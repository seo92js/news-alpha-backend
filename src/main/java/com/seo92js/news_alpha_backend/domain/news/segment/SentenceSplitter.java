package com.seo92js.news_alpha_backend.domain.news.segment;

import com.seo92js.news_alpha_backend.common.AppConstants;
import com.seo92js.news_alpha_backend.domain.news.Sentence;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SentenceSplitter {

    // '.', '!', '?' 기준 문장 단위 split
    public List<Sentence> splitBySentence(String content) {
        List<Sentence> sentences = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        char prevChar = 0;

        for (char c : content.toCharArray()) {
            stringBuilder.append(c);
            if (isSentenceEnd(c, prevChar)) {
                sentences.add(new Sentence(stringBuilder.toString()));
                stringBuilder.setLength(0);
            }
            prevChar = c;
        }

        if (!stringBuilder.isEmpty()) sentences.add(new Sentence(stringBuilder.toString()));
        return sentences;
    }

    // '?', '!' 은 그냥 split, '.' 은 바로 앞글자가 한글일때만(NO. Mr. 1. 이런거 문장으로 판단하지 않도록)
    private boolean isSentenceEnd(char c, char prevChar) {
        if (c == AppConstants.SentenceDelimiter.EXCLAMATION || c == AppConstants.SentenceDelimiter.QUESTION) return true;

        if (c == AppConstants.SentenceDelimiter.PERIOD) {
            return Character.UnicodeScript.of(prevChar) == Character.UnicodeScript.HANGUL;
        }

        return false;
    }
}
