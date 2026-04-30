package com.seo92js.news_alpha_backend.common.exception;

public class ApiFetchException extends BusinessException{

    public ApiFetchException(ErrorCode errorCode, String... logArgs) {
        super(errorCode, logArgs);
    }
}
