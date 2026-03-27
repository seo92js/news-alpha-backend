package com.seo92js.news_alpha_backend.domain.keyword.exception;

import com.seo92js.news_alpha_backend.common.exception.BusinessException;
import com.seo92js.news_alpha_backend.common.exception.ErrorCode;

public class DuplicateKeywordException extends BusinessException {

    public DuplicateKeywordException(String name) {
        super(ErrorCode.DUPLICATE_KEYWORD, name);
    }
}
