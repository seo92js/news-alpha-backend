package com.seo92js.news_alpha_backend.domain.signal.repository;

import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceDetailRow;
import com.seo92js.news_alpha_backend.domain.signal.dto.SignalEvidenceRow;

import java.util.List;

public interface SignalEvidenceRepositoryCustom {
    List<SignalEvidenceRow> findEvidenceRowsBySignalIds(List<Long> signalIds);
    List<SignalEvidenceDetailRow> findEvidenceDetailRowsBySignalId(Long signalId);
}
