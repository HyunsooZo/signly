package com.signly.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final TokenRedisService tokenRedisService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (shouldSkipAuthentication(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromRequest(request);
        String refreshToken = extractRefreshTokenFromRequest(request);

        // 액세스 토큰이 없거나 만료된 경우, 리프레시 토큰으로 갱신 시도
        if (!StringUtils.hasText(token) || !jwtTokenProvider.isTokenValid(token)) {
            if (StringUtils.hasText(refreshToken) && jwtTokenProvider.isRefreshToken(refreshToken) && jwtTokenProvider.isTokenValid(refreshToken)) {
                log.info("액세스 토큰이 {}. 리프레시 토큰으로 자동 갱신 시도...",
                        !StringUtils.hasText(token) ? "없음" : "만료됨");

                try {
                    String newAccessToken = attemptTokenRefresh(refreshToken, request, response);
                    if (newAccessToken != null) {
                        token = newAccessToken; // 갱신된 토큰으로 교체
                        log.info("액세스 토큰 자동 갱신 성공");
                    }
                } catch (Exception refreshError) {
                    log.warn("액세스 토큰 자동 갱신 실패: {}", refreshError.getMessage());
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
            } else {
                // 리프레시 토큰도 없거나 유효하지 않으면 인증 없이 진행 (필요시 AuthenticationEntryPoint 처리)
                log.debug("유효한 토큰이 없습니다. 요청 URI: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 유효한 액세스 토큰 처리
        if (StringUtils.hasText(token) && jwtTokenProvider.isAccessToken(token)) {
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
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipAuthentication(HttpServletRequest request) {
        String path = request.getServletPath();
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return path.startsWith("/.well-known")
                || path.equals("/favicon.ico")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/fonts/");
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        // Authorization 헤더에서 토큰 추출
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 쿠키에서 토큰 추출
        String cookieToken = extractCookieValue(request, "authToken");
        if (StringUtils.hasText(cookieToken)) {
            return cookieToken;
        }

        return null;
    }

    /**
     * 리프레시 토큰 추출
     */
    private String extractRefreshTokenFromRequest(HttpServletRequest request) {
        // 쿠키에서 토큰 추출
        String cookieToken = extractCookieValue(request, "refreshToken");
        if (StringUtils.hasText(cookieToken)) {
            return cookieToken;
        }

        return null;
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
            var refreshRequest = new com.signly.core.auth.dto.RefreshTokenRequest(refreshToken);

            var context = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());

            var authService = context.getBean(com.signly.core.auth.AuthService.class);
            var environment = context.getBean(org.springframework.core.env.Environment.class);

            var loginResponse = authService.refreshToken(refreshRequest);

            // 환경별 보안 설정
            boolean isProduction = java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");

            // 새로운 액세스 토큰을 쿠키에 설정
            Cookie newAuthCookie = new Cookie("authToken", loginResponse.accessToken());
            newAuthCookie.setHttpOnly(true);
            newAuthCookie.setSecure(isProduction);
            newAuthCookie.setPath("/");
            newAuthCookie.setMaxAge(60 * 60); // 1시간
            response.addCookie(newAuthCookie);

            // 새로운 리프레시 토큰을 쿠키에 설정 (Token Rotation)
            Cookie newRefreshCookie = new Cookie("refreshToken", loginResponse.refreshToken());
            newRefreshCookie.setHttpOnly(true);
            newRefreshCookie.setSecure(isProduction);
            newRefreshCookie.setPath("/");
            newRefreshCookie.setMaxAge(30 * 24 * 60 * 60); // 30일
            response.addCookie(newRefreshCookie);

            // Redis에 새 액세스 토큰 저장 (리프레시 토큰은 AuthService.refreshToken()에서 이미 저장됨)
            tokenRedisService.saveAccessToken(loginResponse.userId(), loginResponse.accessToken());

            log.info("토큰 자동 갱신 완료: userId={}", loginResponse.userId());

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
