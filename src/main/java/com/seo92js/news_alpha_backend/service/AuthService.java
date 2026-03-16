package com.seo92js.news_alpha_backend.service;

import com.seo92js.news_alpha_backend.dto.LoginRequest;
import com.seo92js.news_alpha_backend.dto.LoginResponse;
import com.seo92js.news_alpha_backend.dto.SignupRequest;
import com.seo92js.news_alpha_backend.entity.Member;
import com.seo92js.news_alpha_backend.entity.Role;
import com.seo92js.news_alpha_backend.jwt.JwtTokenProvider;
import com.seo92js.news_alpha_backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Transactional
    public Long signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(Role.ROLE_USER) // 기본권한 : USER
                .build();

        return memberRepository.save(member).getId();
    }

    // 로그인
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtTokenProvider.generateToken(authentication);

        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return new LoginResponse(
                token,
                member.getEmail(),
                member.getNickname(),
                member.getRole().name()
        );
    }
}
