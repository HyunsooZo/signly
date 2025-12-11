package com.signly.core.auth;

import com.signly.common.security.JwtTokenProvider;
import com.signly.common.security.SecurityUser;
import com.signly.common.security.TokenRedisService;
import com.signly.user.domain.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 핸들러
 * - JWT 토큰 생성 및 Redis 저장
 * - 프론트엔드로 토큰 전달 (쿠키)
 */
@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;
    private final boolean secureCookie;

    public OAuth2AuthenticationSuccessHandler(
            JwtTokenProvider jwtTokenProvider,
            TokenRedisService tokenRedisService,
            @Value("${app.security.cookie.secure:false}") boolean secureCookie
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenRedisService = tokenRedisService;
        this.secureCookie = secureCookie;
    }

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

        log.info("OAuth2 tokens saved to Redis for user: {}", user.getEmail().value());

        // 쿠키 설정
        if (secureCookie) {
            response.addHeader("Set-Cookie", String.format(
                    "authToken=%s; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=%d",
                    accessToken, 60 * 60
            ));
            response.addHeader("Set-Cookie", String.format(
                    "refreshToken=%s; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=%d",
                    refreshToken, 30 * 24 * 60 * 60
            ));
        } else {
            Cookie authCookie = new Cookie("authToken", accessToken);
            authCookie.setHttpOnly(true);
            authCookie.setPath("/");
            authCookie.setMaxAge(60 * 60);
            response.addCookie(authCookie);

            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(30 * 24 * 60 * 60);
            response.addCookie(refreshCookie);
        }

        log.info("OAuth2 tokens stored in cookies for user: {}", user.getEmail().value());

        // 쿠키로 토큰 전달하므로 쿼리 파라미터 불필요
        String targetUrl = "/home";

        log.info("Redirecting OAuth2 user to: {}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
