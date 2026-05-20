package com.seo92js.news_alpha_backend.domain.signal.repository;

import com.seo92js.news_alpha_backend.domain.signal.dto.SignalView;
import com.seo92js.news_alpha_backend.domain.signal.Signal;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SignalRepositoryCustom {
    List<Signal> findRecentSignalsByStockId(Long stockId, LocalDateTime since, Pageable pageable);
    List<SignalView> findTopRecentSignalViews();
    SignalView findSignalViewById(Long signalId);
    Set<String> findExistingSignalKeys(Collection<String> signalKeys);
}
