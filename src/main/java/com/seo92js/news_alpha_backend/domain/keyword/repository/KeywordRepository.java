package com.seo92js.news_alpha_backend.domain.keyword.repository;

import com.seo92js.news_alpha_backend.domain.keyword.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    boolean existsByName(String name);
}
