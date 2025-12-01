package com.deally.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        String requestUri = request.getRequestURI();

        // API 요청인 경우 401 JSON 응답
        if (requestUri.startsWith("/api/")) {
            log.debug("API 요청 인증 실패: {}", requestUri);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다.\"}");
            return;
        }

        // 웹 페이지 요청인 경우 로그인 페이지로 리다이렉트
        log.debug("웹 페이지 인증 실패, 로그인 페이지로 리다이렉트: {}", requestUri);
        String redirectUrl = "/login";

        // 원래 요청 URL을 returnUrl 파라미터로 전달
        if (!requestUri.equals("/")) {
            redirectUrl += "?returnUrl=" + requestUri;
        }

        response.sendRedirect(redirectUrl);
    }
}
