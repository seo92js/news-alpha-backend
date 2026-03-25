package com.seo92js.news_alpha_backend.common;

public class AppConstants {

    private AppConstants() {}

    public static final String SPACE = " ";
    public static final String COMMA = ",";
    public static final String DOT = ".";
    public static final String LOG_DELIMETER = COMMA + SPACE; // ", "

    public static final class Jwt {
        private Jwt() {}
        public static final String PREFIX = "Bearer ";
        public static final int PREFIX_LENGTH = 7;
        public static final String CLAIM_ROLES = "roles";
    }

    public static final class AuthMessage {
        private AuthMessage() {}
        public static final String SIGNUP_SUCCESS = "회원가입이 완료되었습니다.";
        public static final String ADMIN_DASHBOARD = "관리자 대시보드입니다.";
    }
}
