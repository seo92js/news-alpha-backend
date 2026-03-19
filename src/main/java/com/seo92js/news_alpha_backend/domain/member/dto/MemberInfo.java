package com.seo92js.news_alpha_backend.domain.member.dto;

import com.seo92js.news_alpha_backend.domain.member.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberInfo {

    private String email;
    private Role role;
}
