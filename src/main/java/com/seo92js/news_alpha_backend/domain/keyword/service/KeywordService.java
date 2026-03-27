package com.seo92js.news_alpha_backend.domain.keyword.service;

import com.seo92js.news_alpha_backend.common.exception.BusinessException;
import com.seo92js.news_alpha_backend.common.exception.ErrorCode;
import com.seo92js.news_alpha_backend.domain.keyword.Keyword;
import com.seo92js.news_alpha_backend.domain.keyword.dto.KeywordResponse;
import com.seo92js.news_alpha_backend.domain.keyword.dto.KeywordSaveRequest;
import com.seo92js.news_alpha_backend.domain.keyword.exception.DuplicateKeywordException;
import com.seo92js.news_alpha_backend.domain.keyword.exception.KeywordNotFoundException;
import com.seo92js.news_alpha_backend.domain.keyword.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;

    /**
     * 키워드 저장
     */
    @Transactional
    public KeywordResponse save(KeywordSaveRequest request) {
        if (keywordRepository.existsByName(request.name())) {
            throw new DuplicateKeywordException(request.name());
        }
        Keyword keyword = keywordRepository.save(Keyword.of(request.name()));
        return KeywordResponse.from(keyword);
    }

    /**
     * 키워드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<KeywordResponse> findAll() {
        return keywordRepository.findAll().stream()
                .map(KeywordResponse::from)
                .toList();
    }

    /**
     * 키워드 삭제
     */
    @Transactional
    public void delete(Long id) {
        Keyword keyword = keywordRepository.findById(id)
                .orElseThrow(() -> new KeywordNotFoundException(id));
        keywordRepository.delete(keyword);
    }
}
