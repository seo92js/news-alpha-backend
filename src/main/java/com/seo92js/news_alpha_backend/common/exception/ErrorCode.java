package com.seo92js.news_alpha_backend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Member
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),

    // Stock
    STOCK_NOT_FOUND("STOCK_NOT_FOUND", "종목을 찾을 수 없습니다."),
    DUPLICATE_STOCK("DUPLICATE_STOCK", "이미 등록된 종목입니다."),
    DUPLICATE_STOCK_KEYWORD("DUPLICATE_STOCK_KEYWORD", "이미 등록된 종목 키워드입니다."),

    // Common
    INVALID_INPUT("INVALID_INPUT", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final String message;
}
