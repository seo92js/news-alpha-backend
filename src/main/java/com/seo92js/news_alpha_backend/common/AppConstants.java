package com.seo92js.news_alpha_backend.common;

public class AppConstants {

    private AppConstants() {}

    public static final class Jwt {
        public static final String PREFIX = "Bearer ";
        public static final int PREFIX_LENGTH = 7;
        public static final String CLAIM_ROLES = "roles";
    }
}
