package com.seo92js.news_alpha_backend.domain.member.exception;

import com.seo92js.news_alpha_backend.common.exception.BusinessException;
import com.seo92js.news_alpha_backend.common.exception.ErrorCode;

public class MemberNotFoundException extends BusinessException {

    public MemberNotFoundException(String email) {
        super(ErrorCode.MEMBER_NOT_FOUND, email);
    }
}
