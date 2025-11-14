package com.signly.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final TokenRedisService tokenRedisService;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService userDetailsService,
            TokenRedisService tokenRedisService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenRedisService = tokenRedisService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            log.debug("토큰 발견: {}", token.substring(0, Math.min(20, token.length())) + "...");

            if (!jwtTokenProvider.isTokenValid(token)) {
                log.warn("액세스 토큰이 만료되었거나 유효하지 않습니다. 자동 갱신 시도...");

                // 액세스 토큰 만료 시 리프레시 토큰으로 자동 갱신 시도
                String refreshToken = extractRefreshTokenFromRequest(request);
                if (refreshToken != null && jwtTokenProvider.isRefreshToken(refreshToken)) {
                    try {
                        String newAccessToken = attemptTokenRefresh(refreshToken, request, response);
                        if (newAccessToken != null) {
                            token = newAccessToken; // 갱신된 토큰으로 교체
                            log.info("액세스 토큰 자동 갱신 성공");
                        }
                    } catch (Exception refreshError) {
                        log.warn("액세스 토큰 자동 갱신 실패: {}", refreshError.getMessage());
                        SecurityContextHolder.clearContext();
                        sendUnauthorizedResponse(response, "TOKEN_REFRESH_FAILED");
                        return;
                    }
                } else {
                    log.warn("리프레시 토큰이 없거나 유효하지 않습니다");
                    SecurityContextHolder.clearContext();
                    sendUnauthorizedResponse(response, "NO_REFRESH_TOKEN");
                    return;
                }
            }

            if (!jwtTokenProvider.isAccessToken(token)) {
                log.warn("Access 토큰이 아닙니다 (Refresh 토큰일 수 있음)");
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // 유효한 액세스 토큰 처리
            try {
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                String email = jwtTokenProvider.getEmailFromToken(token);

                // Redis에서 토큰 검증
                if (!tokenRedisService.isAccessTokenValid(userId, token)) {
                    log.warn("Redis에 토큰이 존재하지 않거나 일치하지 않습니다: userId={}", userId);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                request.setAttribute("userId", userId);
                request.setAttribute("userEmail", email);
                request.setAttribute("userType", jwtTokenProvider.getUserTypeFromToken(token));

                log.debug("인증 성공: userId={}, email={}", userId, email);
            } catch (Exception e) {
                log.warn("JWT 토큰 처리 중 오류 발생", e);
                SecurityContextHolder.clearContext();
            }
        } else {
            log.debug("토큰이 없습니다. 요청 URI: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        // Authorization 헤더에서 토큰 추출
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 쿠키에서 토큰 추출
        return extractCookieValue(request, "authToken");
    }

    /**
     * 리프레시 토큰 추출
     */
    private String extractRefreshTokenFromRequest(HttpServletRequest request) {
        return extractCookieValue(request, "refreshToken");
    }

    /**
     * 쿠키에서 특정 이름의 값 추출 (중복 제거를 위한 공통 메서드)
     */
    private String extractCookieValue(
            HttpServletRequest request,
            String cookieName
    ) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 토큰 갱신 시도
     */
    private String attemptTokenRefresh(
            String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            // AuthService를 통해 토큰 갱신
            com.signly.core.auth.dto.RefreshTokenRequest refreshRequest =
                    new com.signly.core.auth.dto.RefreshTokenRequest(refreshToken);

            // AuthService 주입받기 위해 ApplicationContext 사용
            com.signly.core.auth.AuthService authService =
                    org.springframework.web.context.support.WebApplicationContextUtils
                            .getRequiredWebApplicationContext(request.getServletContext())
                            .getBean(com.signly.core.auth.AuthService.class);

            com.signly.core.auth.dto.LoginResponse loginResponse = authService.refreshToken(refreshRequest);

            // 새로운 액세스 토큰을 쿠키에 설정
            Cookie newAuthCookie = new Cookie("authToken", loginResponse.accessToken());
            newAuthCookie.setHttpOnly(true);
            newAuthCookie.setPath("/");
            newAuthCookie.setMaxAge(60 * 60); // 1시간
            response.addCookie(newAuthCookie);

            // Redis에 새 액세스 토큰 저장
            tokenRedisService.saveAccessToken(loginResponse.userId(), loginResponse.accessToken());

            return loginResponse.accessToken();

        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생", e);
            throw e;
        }
    }

    /**
     * 401 응답 전송
     */
    private void sendUnauthorizedResponse(
            HttpServletResponse response,
            String errorCode
    ) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(String.format(
                    "{\"error\":\"%s\",\"message\":\"인증이 필요합니다\",\"timestamp\":\"%s\"}",
                    errorCode,
                    java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            log.error("401 응답 전송 중 오류 발생", e);
        }
    }
}
