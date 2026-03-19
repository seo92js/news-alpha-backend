package com.seo92js.news_alpha_backend.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String email;
    private String nickname;
    private String role;
}
