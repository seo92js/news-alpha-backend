package com.seo92js.news_alpha_backend.common.exception;

import com.seo92js.news_alpha_backend.common.AppConstants;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String[] logArgs;

    public BusinessException(ErrorCode errorCode, String... logArgs) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.logArgs = logArgs;
    }

    public String getLogMessage() {

        if (logArgs == null || logArgs.length == 0) {

            return String.format("[%s]", this.getMessage());
        }
        else {

            String context = String.join(AppConstants.LOG_DELIMETER, logArgs);
            return String.format("[%s] | context : %s", this.getMessage(), context);
        }
    }
}
