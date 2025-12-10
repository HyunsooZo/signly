package com.signly.core.auth;

import com.signly.common.security.JwtTokenProvider;
import com.signly.common.security.SecurityUser;
import com.signly.common.security.TokenRedisService;
import com.signly.user.domain.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 핸들러
 * - JWT 토큰 생성 및 Redis 저장
 * - 프론트엔드로 토큰 전달 (쿼리 파라미터 또는 쿠키)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User user = securityUser.getUser();

        log.info("OAuth2 login success for user: {}", user.getEmail().value());

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId().value(),
                user.getEmail().value(),
                user.getUserType().name()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId().value());

        // Redis에 토큰 저장
        tokenRedisService.saveAccessToken(user.getUserId().value(), accessToken);
        tokenRedisService.saveRefreshToken(user.getUserId().value(), refreshToken);

        // 리다이렉트 URL 생성 (토큰을 쿼리 파라미터로 전달)
        String targetUrl = UriComponentsBuilder.fromUriString("/home")
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshToken)
                .build().toUriString();

        log.info("Redirecting OAuth2 user to: {}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
