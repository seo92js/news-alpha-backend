package com.seo92js.news_alpha_backend.domain.keyword.exception;

import com.seo92js.news_alpha_backend.common.exception.BusinessException;
import com.seo92js.news_alpha_backend.common.exception.ErrorCode;

public class KeywordNotFoundException extends BusinessException {

    public KeywordNotFoundException(Long id) {
        super(ErrorCode.KEYWORD_NOT_FOUND, String.valueOf(id));
    }
}
