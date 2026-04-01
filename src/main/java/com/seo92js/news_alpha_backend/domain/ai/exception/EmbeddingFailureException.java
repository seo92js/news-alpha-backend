package com.seo92js.news_alpha_backend.domain.ai.exception;

import com.seo92js.news_alpha_backend.common.exception.BusinessException;
import com.seo92js.news_alpha_backend.common.exception.ErrorCode;

public class EmbeddingFailureException extends BusinessException {

    public EmbeddingFailureException(String keyword) {
        super(ErrorCode.EMBEDDING_FAILURE, keyword);
    }
}
