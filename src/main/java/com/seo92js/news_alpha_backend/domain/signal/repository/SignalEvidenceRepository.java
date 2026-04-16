package com.seo92js.news_alpha_backend.domain.signal.repository;

import com.seo92js.news_alpha_backend.domain.signal.SignalEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignalEvidenceRepository extends JpaRepository<SignalEvidence, Long> {
    List<SignalEvidence> findBySignalIdOrderByRankOrderAsc(Long signalId);
}
