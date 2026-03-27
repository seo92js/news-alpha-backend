package com.seo92js.news_alpha_backend.domain.news.scheduler;

import com.seo92js.news_alpha_backend.domain.keyword.Keyword;
import com.seo92js.news_alpha_backend.domain.keyword.repository.KeywordRepository;
import com.seo92js.news_alpha_backend.domain.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final KeywordRepository keywordRepository;
    private final NewsService newsService;

    /**
     * 임시 매 2시간 정각
     * 주석처리 나중에 prod 프로필에서만 돌게
     */
    // @Scheduled(cron = "0 0 0/2 * * *")
    public void fetchAllKeywords() {
        List<Keyword> keywords = keywordRepository.findAll();

        if (keywords.isEmpty()) {
            return;
        }

        int success = 0;
        int failed = 0;

        for (Keyword keyword : keywords) {
            try {
                newsService.fetchNews(keyword.getName());
                success++;

                // IP 차단 방지를 위한 딜레이
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                failed++;
            }
        }
    }
}
