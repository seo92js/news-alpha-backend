package com.seo92js.news_alpha_backend.domain.stock.exception;

import com.seo92js.news_alpha_backend.common.exception.BusinessException;
import com.seo92js.news_alpha_backend.common.exception.ErrorCode;

public class DuplicateStockKeywordException extends BusinessException {
    public DuplicateStockKeywordException(Long stockId, String keyword) {
        super(ErrorCode.DUPLICATE_STOCK_KEYWORD, String.valueOf(stockId), keyword);
    }
}
