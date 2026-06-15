package com.seo92js.news_alpha_backend.domain.stock.exception;

import com.seo92js.news_alpha_backend.common.exception.BusinessException;
import com.seo92js.news_alpha_backend.common.exception.ErrorCode;

public class KeywordGenerationException extends BusinessException {
    public KeywordGenerationException(String stockName, String reason) {
        super(ErrorCode.KEYWORD_GENERATION_FAILED, stockName, reason);
    }
}
