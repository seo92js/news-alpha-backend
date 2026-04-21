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
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_signal_evidence_signal_news", columnNames = {"signal_id", "evidence_news_id"})
        }
)
public class SignalEvidence extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signal_id", nullable = false)
    private Signal signal;

    @Column(name = "evidence_news_id", nullable = false)
    private Long newsId;

    @Column(nullable = false)
    private int rankOrder;

    @Column(nullable = false)
    private String title;

    private String url;

    private LocalDateTime publishedAt;

    public static SignalEvidence of(Signal signal, Long newsId, String title, String url, LocalDateTime publishedAt, int rankOrder) {
        SignalEvidence evidence = new SignalEvidence();
        evidence.signal = signal;
        evidence.newsId = newsId;
        evidence.rankOrder = rankOrder;
        evidence.title = title;
        evidence.url = url;
        evidence.publishedAt = publishedAt;
        return evidence;
    }
}
