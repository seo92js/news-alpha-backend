package com.seo92js.news_alpha_backend.domain.news;

public record Sentence(
        String text,
        int length
)
{
    public Sentence {
        text = text.trim();
        length = text.length();
    }

    public Sentence(String text) {
        this(text, text.length());
    }
}
