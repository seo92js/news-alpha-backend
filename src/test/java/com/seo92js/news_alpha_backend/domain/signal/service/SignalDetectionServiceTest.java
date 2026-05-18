package com.seo92js.news_alpha_backend.domain.signal.service;

import com.seo92js.news_alpha_backend.domain.ai.dto.NewsMetadata;
import com.seo92js.news_alpha_backend.domain.ai.service.VectorStoreService;
import com.seo92js.news_alpha_backend.domain.news.News;
import com.seo92js.news_alpha_backend.domain.news.repository.NewsRepository;
import com.seo92js.news_alpha_backend.domain.signal.Signal;
import com.seo92js.news_alpha_backend.domain.signal.SignalEventType;
import com.seo92js.news_alpha_backend.domain.signal.SignalSentiment;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalEvidenceRepository;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalRepository;
import com.seo92js.news_alpha_backend.domain.stock.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SignalDetectionServiceTest {

    @Mock
    private VectorStoreService vectorStoreService;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private SignalRepository signalRepository;

    @Mock
    private SignalEvidenceRepository signalEvidenceRepository;

    @Mock
    private SignalAnalysisService signalAnalysisService;

    @Mock
    private TransactionTemplate transactionTemplate;

    private SignalDetectionService signalDetectionService;

    @BeforeEach
    void setUp() {
        signalDetectionService = new SignalDetectionService(
                vectorStoreService,
                newsRepository,
                signalRepository,
                signalEvidenceRepository,
                signalAnalysisService,
                transactionTemplate
        );

        doAnswer(invocation -> {
            Consumer<TransactionStatus> action = invocation.getArgument(0);
            action.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    void 키워드당_시그널_LLM_분석과_저장은_최대_3개까지만() {
        Stock stock = stock();
        List<News> discoveredNews = LongStream.rangeClosed(1, 12)
                .mapToObj(id -> news(id, id <= 4))
                .toList();

        when(signalRepository.findExistingSignalKeys(anyCollection())).thenReturn(Set.of());
        when(vectorStoreService.similaritySearch(anyString(), anyInt(), anyDouble()))
                .thenReturn(List.of(document(5), document(6)))
                .thenReturn(List.of(document(7), document(8)))
                .thenReturn(List.of(document(9), document(10)))
                .thenReturn(List.of(document(11), document(12)));
        when(signalAnalysisService.analyze(any(), anyString(), anyString(), anyString(), anyInt(), anyLong(), any()))
                .thenReturn(analysis());
        when(signalRepository.save(any(Signal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(newsRepository.findAllById(any())).thenReturn(discoveredNews);
        when(signalEvidenceRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        signalDetectionService.detect(stock, "테슬라", discoveredNews);

        verify(signalAnalysisService, times(3))
                .analyze(any(), anyString(), anyString(), anyString(), anyInt(), anyLong(), any());
        verify(signalRepository, times(3)).save(any(Signal.class));
        verify(signalEvidenceRepository, times(3)).saveAll(any());
    }

    @Test
    void 근거_뉴스가_50퍼센트_이상_겹치는_후보는_중복으로_제거() {
        Stock stock = stock();
        List<News> discoveredNews = LongStream.rangeClosed(1, 4)
                .mapToObj(id -> news(id, id <= 2))
                .toList();

        when(signalRepository.findExistingSignalKeys(anyCollection())).thenReturn(Set.of());
        when(vectorStoreService.similaritySearch(anyString(), anyInt(), anyDouble()))
                .thenReturn(List.of(document(3), document(4)))
                .thenReturn(List.of(document(3), document(4)));
        when(signalAnalysisService.analyze(any(), anyString(), anyString(), anyString(), anyInt(), anyLong(), any()))
                .thenReturn(analysis());
        when(signalRepository.save(any(Signal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(newsRepository.findAllById(any())).thenReturn(discoveredNews);
        when(signalEvidenceRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        signalDetectionService.detect(stock, "테슬라", discoveredNews);

        verify(signalAnalysisService, times(1))
                .analyze(any(), anyString(), anyString(), anyString(), anyInt(), anyLong(), any());
        verify(signalRepository, times(1)).save(any(Signal.class));
        verify(signalEvidenceRepository, times(1)).saveAll(any());
    }

    private Stock stock() {
        Stock stock = Stock.of("TSLA", "테슬라", "NASDAQ");
        ReflectionTestUtils.setField(stock, "id", 1L);
        return stock;
    }

    private News news(long id, boolean seed) {
        News news = News.of(
                "테슬라",
                "뉴스 제목 " + id,
                "https://origin.example.com/" + id,
                "https://news.example.com/" + id,
                "뉴스 설명 " + id,
                seed ? "뉴스 본문 " + id : "",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(news, "id", id);
        return news;
    }

    private Document document(long newsId) {
        return new Document(
                "doc-" + newsId,
                "뉴스 본문 " + newsId,
                Map.of(
                        NewsMetadata.Keys.ID, newsId,
                        NewsMetadata.Keys.KEYWORD, "테슬라",
                        NewsMetadata.Keys.TITLE, "뉴스 제목 " + newsId,
                        NewsMetadata.Keys.URL, "https://news.example.com/" + newsId,
                        NewsMetadata.Keys.PUBLISHED_AT, LocalDateTime.now().toString()
                )
        );
    }

    private SignalAnalysisResult analysis() {
        return new SignalAnalysisResult(
                "분석 제목",
                "분석 요약",
                SignalEventType.PRODUCT,
                SignalSentiment.POSITIVE,
                80,
                "투자자 관점 요약"
        );
    }
}
