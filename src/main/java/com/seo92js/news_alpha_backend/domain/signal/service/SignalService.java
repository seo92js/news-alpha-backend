package com.seo92js.news_alpha_backend.domain.signal.service;

import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceResponse;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalResponse;
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
        return signalRepository.findTop20ByOrderByScoreDescDetectedAtDesc().stream()
                .map(signal -> SignalResponse.from(
                        signal,
                        signalEvidenceRepository.findBySignalIdOrderByRankOrderAsc(signal.getId()).stream()
                                .map(SignalEvidenceResponse::from)
                                .toList()
                ))
                .toList();
    }
}
