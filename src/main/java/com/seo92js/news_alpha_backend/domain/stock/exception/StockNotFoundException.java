package com.seo92js.news_alpha_backend.domain.stock.exception;

import com.seo92js.news_alpha_backend.common.exception.BusinessException;
import com.seo92js.news_alpha_backend.common.exception.ErrorCode;

public class StockNotFoundException extends BusinessException {
    public StockNotFoundException(Long stockId) {
        super(ErrorCode.STOCK_NOT_FOUND, String.valueOf(stockId));
    }
}
