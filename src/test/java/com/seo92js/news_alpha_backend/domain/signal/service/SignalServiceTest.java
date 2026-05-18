package com.seo92js.news_alpha_backend.domain.signal.service;

import com.seo92js.news_alpha_backend.domain.signal.SignalEventType;
import com.seo92js.news_alpha_backend.domain.signal.SignalSentiment;
import com.seo92js.news_alpha_backend.domain.signal.SignalType;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalDetailResponse;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceDetailRow;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceRow;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalResponse;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalView;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalEvidenceRepository;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignalServiceTest {

    @Mock
    private SignalRepository signalRepository;

    @Mock
    private SignalEvidenceRepository signalEvidenceRepository;

    @InjectMocks
    private SignalService signalService;

    @Test
    void 최근_시그널_목록_조회시_signalId별로_근거뉴스_반환() {
        SignalView first = new SignalView(
                1L, "테슬라", SignalType.EMERGING_CLUSTER, "로보택시 기대감 재점화", "최근 기사 급증",
                SignalEventType.PRODUCT, SignalSentiment.POSITIVE, 78, "로보택시 기대가 투자 심리에 영향을 줄 수 있습니다.",
                88.2, 6,
                LocalDateTime.of(2026, 5, 7, 7, 0),
                LocalDateTime.of(2026, 5, 7, 9, 0),
                LocalDateTime.of(2026, 5, 7, 9, 10)
        );
        SignalView second = new SignalView(
                2L, "테슬라", SignalType.EMERGING_CLUSTER, "일론 머스크 발언 영향", "변동성 확대",
                SignalEventType.MANAGEMENT, SignalSentiment.MIXED, 70, "CEO 발언에 따른 변동성 확대 여부를 확인해야 합니다.",
                81.5, 4,
                LocalDateTime.of(2026, 5, 7, 6, 30),
                LocalDateTime.of(2026, 5, 7, 8, 30),
                LocalDateTime.of(2026, 5, 7, 8, 40)
        );

        when(signalRepository.findTopRecentSignalViews()).thenReturn(List.of(first, second));
        when(signalEvidenceRepository.findEvidenceRowsBySignalIds(List.of(1L, 2L))).thenReturn(List.of(
                new SignalEvidenceRow(1L, 101L, 1, "근거 기사 1", "https://a.com", LocalDateTime.of(2026, 5, 7, 9, 0)),
                new SignalEvidenceRow(1L, 102L, 2, "근거 기사 2", "https://b.com", LocalDateTime.of(2026, 5, 7, 8, 50)),
                new SignalEvidenceRow(2L, 201L, 1, "근거 기사 3", "https://c.com", LocalDateTime.of(2026, 5, 7, 8, 20))
        ));

        List<SignalResponse> responses = signalService.findRecentSignals();

        assertEquals(2, responses.size());
        assertEquals(2, responses.get(0).evidences().size());
        assertEquals(1, responses.get(1).evidences().size());
        assertEquals(101L, responses.get(0).evidences().get(0).newsId());
        assertEquals(201L, responses.get(1).evidences().get(0).newsId());
        assertEquals(SignalEventType.PRODUCT, responses.get(0).eventType());
        assertEquals("제품/사업", responses.get(0).eventTypeLabel());
        assertEquals(SignalSentiment.POSITIVE, responses.get(0).sentiment());
        assertEquals("긍정", responses.get(0).sentimentLabel());
        assertEquals(78, responses.get(0).confidence());
    }

    @Test
    void 시그널_상세_조회시_근거뉴스_반환() {
        Long signalId = 1L;
        SignalView signal = new SignalView(
                signalId, "테슬라", SignalType.EMERGING_CLUSTER, "로보택시 기대감 재점화", "최근 기사 급증",
                SignalEventType.PRODUCT, SignalSentiment.POSITIVE, 78, "로보택시 기대가 투자 심리에 영향을 줄 수 있습니다.",
                88.2, 6,
                LocalDateTime.of(2026, 5, 7, 7, 0),
                LocalDateTime.of(2026, 5, 7, 9, 0),
                LocalDateTime.of(2026, 5, 7, 9, 10)
        );
        String content = "테슬라 로보택시 기대감이 시장에서 다시 부각되며 관련 뉴스가 빠르게 증가하고 있다. "
                + "여러 매체가 같은 이슈를 반복 보도하면서 투자자 관심이 커지고 있다.";

        when(signalRepository.findSignalViewById(signalId)).thenReturn(signal);
        when(signalEvidenceRepository.findEvidenceDetailRowsBySignalId(signalId)).thenReturn(List.of(
                new SignalEvidenceDetailRow(
                        101L,
                        1,
                        "근거 기사 1",
                        "https://a.com",
                        LocalDateTime.of(2026, 5, 7, 9, 0),
                        "짧은 설명",
                        content
                )
        ));

        SignalDetailResponse response = signalService.findSignalDetail(signalId);

        assertNotNull(response);
        assertEquals(signalId, response.id());
        assertEquals(1, response.evidences().size());
        assertNotNull(response.evidences().get(0).preview());
        assertEquals(content, response.evidences().get(0).preview());
        assertEquals(SignalEventType.PRODUCT, response.eventType());
        assertEquals("제품/사업", response.eventTypeLabel());
        assertEquals(SignalSentiment.POSITIVE, response.sentiment());
        assertEquals("긍정", response.sentimentLabel());
        assertEquals("로보택시 기대가 투자 심리에 영향을 줄 수 있습니다.", response.investorSummary());
    }

    @Test
    void 없는_시그널_상세_조회시_null반환() {
        when(signalRepository.findSignalViewById(999L)).thenReturn(null);

        SignalDetailResponse response = signalService.findSignalDetail(999L);

        assertNull(response);
    }
}
