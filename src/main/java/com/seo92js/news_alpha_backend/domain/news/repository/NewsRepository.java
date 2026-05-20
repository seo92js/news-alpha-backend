package com.seo92js.news_alpha_backend.domain.news.repository;

import com.seo92js.news_alpha_backend.domain.news.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findAllByOriginalLinkIn(Collection<String> originalLinks);
}
