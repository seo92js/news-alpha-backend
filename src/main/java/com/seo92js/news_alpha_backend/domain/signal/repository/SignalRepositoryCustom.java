package com.seo92js.news_alpha_backend.domain.signal.repository;

import com.seo92js.news_alpha_backend.domain.signal.Signal;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface SignalRepositoryCustom {
    List<Signal> findRecentSignalsByStockId(Long stockId, LocalDateTime since, Pageable pageable);
}
