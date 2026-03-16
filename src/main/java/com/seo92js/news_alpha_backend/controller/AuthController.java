package com.seo92js.news_alpha_backend.controller;

import com.seo92js.news_alpha_backend.dto.LoginRequest;
import com.seo92js.news_alpha_backend.dto.LoginResponse;
import com.seo92js.news_alpha_backend.dto.SignupRequest;
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
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody SignupRequest request) {
        Long memberId = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "회원가입이 완료되었습니다.",
                        "memberId", memberId
                ));
    }

    // 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // 내 정보 조회 (인증 필요)
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyInfo(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "email", authentication.getName(),
                "roles", authentication.getAuthorities()
        ));
    }

    // ADMIN 전용 API 예시
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminDashboard() {
        return ResponseEntity.ok(Map.of("message", "관리자 대시보드입니다."));
    }
}
