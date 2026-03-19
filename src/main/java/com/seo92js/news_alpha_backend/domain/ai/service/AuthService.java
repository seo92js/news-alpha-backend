package com.seo92js.news_alpha_backend.domain.ai.service;

import com.seo92js.news_alpha_backend.common.AppConstants;
import com.seo92js.news_alpha_backend.domain.ai.exception.DuplicateEmailException;
import com.seo92js.news_alpha_backend.domain.ai.exception.MemberNotFoundException;
import com.seo92js.news_alpha_backend.domain.member.Member;
import com.seo92js.news_alpha_backend.domain.member.Role;
import com.seo92js.news_alpha_backend.config.security.jwt.JwtTokenProvider;
import com.seo92js.news_alpha_backend.domain.member.dto.*;
import com.seo92js.news_alpha_backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
    public SignupResponse signup(SignupRequest request) {

        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(Role.ROLE_USER) // 기본권한 : USER
                .build();

        Member saved = memberRepository.save(member);

        return new SignupResponse(saved.getId(), AppConstants.AuthMessage.SIGNUP_SUCCESS);
    }

    // 로그인
    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtTokenProvider.generateToken(authentication);

        // authenticate() 통과하면 존재, loadUserByUsername
        Member member = memberRepository.findByEmail(userDetails.getUsername()).get();

        return new LoginResponse(token, member.getEmail(), member.getNickname(), member.getRole().name());
    }

    public MemberInfo getMyInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
        return new MemberInfo(member.getEmail(), member.getRole());
    }
}
