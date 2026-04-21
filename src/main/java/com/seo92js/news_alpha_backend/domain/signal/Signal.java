package com.seo92js.news_alpha_backend.domain.signal;

import com.seo92js.news_alpha_backend.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Signal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String signalKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignalType type;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private int relatedNewsCount;

    private LocalDateTime firstPublishedAt;

    private LocalDateTime lastPublishedAt;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    public static Signal of(
            String signalKey,
            SignalType type,
            String keyword,
            String title,
            String summary,
            double score,
            int relatedNewsCount,
            LocalDateTime firstPublishedAt,
            LocalDateTime lastPublishedAt,
            LocalDateTime detectedAt
    ) {
        Signal signal = new Signal();
        signal.signalKey = signalKey;
        signal.type = type;
        signal.keyword = keyword;
        signal.title = title;
        signal.summary = summary;
        signal.score = score;
        signal.relatedNewsCount = relatedNewsCount;
        signal.firstPublishedAt = firstPublishedAt;
        signal.lastPublishedAt = lastPublishedAt;
        signal.detectedAt = detectedAt;
        return signal;
    }
}
