package com.seo92js.news_alpha_backend.controller;

import com.seo92js.news_alpha_backend.common.AppConstants;
import com.seo92js.news_alpha_backend.dto.*;
import com.seo92js.news_alpha_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {

        return authService.signup(request);
    }

    // 로그인
    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }

    // 내 정보 조회 (인증 필요)
    @GetMapping("/me")
    public MemberInfo getMyInfo(Authentication authentication) {
        return authService.getMyInfo(authentication.getName());
    }

    // ADMIN 전용 API 예시
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse adminDashboard() {
        return new AdminDashboardResponse(AppConstants.AuthMessage.ADMIN_DASHBOARD);
    }
}
