package com.seo92js.news_alpha_backend.domain.signal.service;

import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceRow;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceResponse;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalDetailResponse;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceDetailRow;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceDetailResponse;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalResponse;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalView;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalEvidenceRepository;
import com.seo92js.news_alpha_backend.domain.signal.repository.SignalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignalService {

    private final SignalRepository signalRepository;
    private final SignalEvidenceRepository signalEvidenceRepository;

    /**
     * 최근 시그널 목록을 근거 뉴스와 함께 조회
     */
    @Transactional(readOnly = true)
    public List<SignalResponse> findRecentSignals() {
        List<SignalView> signals = signalRepository.findTopRecentSignalViews();
        List<SignalEvidenceRow> evidenceRows = signalEvidenceRepository.findEvidenceRowsBySignalIds(
                signals.stream().map(SignalView::id).toList()
        );

        return signals.stream()
                .map(signal -> SignalResponse.from(
                        signal,
                        buildEvidenceResponses(signal.id(), evidenceRows)
                ))
                .toList();
    }

    /**
     * 특정 시그널의 요약 정보와 근거 뉴스 preview 목록 조회
     */
    @Transactional(readOnly = true)
    public SignalDetailResponse findSignalDetail(Long signalId) {
        SignalView signal = signalRepository.findSignalViewById(signalId);
        if (signal == null) {
            return null;
        }

        return SignalDetailResponse.from(
                signal,
                signalEvidenceRepository.findEvidenceDetailRowsBySignalId(signalId).stream()
                        .map(SignalEvidenceDetailResponse::from)
                        .toList()
        );
    }

    private List<SignalEvidenceResponse> buildEvidenceResponses(Long signalId, List<SignalEvidenceRow> evidenceRows) {
        return evidenceRows.stream()
                .filter(row -> row.signalId().equals(signalId))
                .map(SignalEvidenceResponse::from)
                .toList();
    }
}
