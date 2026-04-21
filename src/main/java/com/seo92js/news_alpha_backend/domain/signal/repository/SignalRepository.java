package com.seo92js.news_alpha_backend.domain.signal.repository;

import com.seo92js.news_alpha_backend.domain.signal.Signal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignalRepository extends JpaRepository<Signal, Long>, SignalRepositoryCustom {
    boolean existsBySignalKey(String signalKey);
    List<Signal> findTop20ByOrderByScoreDescDetectedAtDesc();
}
