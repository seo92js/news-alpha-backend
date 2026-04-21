package com.seo92js.news_alpha_backend.domain.stock.exception;

import com.seo92js.news_alpha_backend.common.exception.BusinessException;
import com.seo92js.news_alpha_backend.common.exception.ErrorCode;

public class DuplicateStockException extends BusinessException {
    public DuplicateStockException(String ticker, String market) {
        super(ErrorCode.DUPLICATE_STOCK, ticker, market);
    }
}
