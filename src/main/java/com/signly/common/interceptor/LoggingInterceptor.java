package com.signly.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        String method = request.getMethod();
        String uri = sanitizePath(request.getRequestURI());
        String queryString = sanitizeQueryString(request.getQueryString());
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        logger.info("📥 [REQUEST] {} {} {} | IP: {} | UA: {}",
                method, uri,
                queryString != null ? "?" + queryString : "",
                remoteAddr,
                userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) + "..." : "N/A");

        // 헤더 로깅 (X-User-Id 같은 중요한 헤더)
        String userId = request.getHeader("X-User-Id");
        if (userId != null) {
            logger.debug("📋 [HEADER] X-User-Id: {}", userId);
        }

        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) throws Exception {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        String method = request.getMethod();
        String uri = sanitizePath(request.getRequestURI());
        int status = response.getStatus();

        String statusEmoji = getStatusEmoji(status);

        logger.info("📤 [RESPONSE] {} {} -> {} {} | {}ms",
                method, uri, statusEmoji, status, duration);

        if (ex != null) {
            logger.error("❌ [ERROR] Exception during request processing: {}", ex.getMessage(), ex);
        }
    }

    private String getStatusEmoji(int status) {
        if (status >= 200 && status < 300) return "✅";
        else if (status >= 300 && status < 400) return "🔄";
        else if (status >= 400 && status < 500) return "⚠️";
        else if (status >= 500) return "❌";
        else return "❓";
    }

    private static final Set<String> SENSITIVE_QUERY_KEYS = new HashSet<>(Arrays.asList(
            "access_token",
            "refresh_token",
            "token"
    ));

    private String sanitizePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return rawPath;
        }

        // 서명 링크 토큰이 경로에 포함되므로 마스킹
        // 예: /sign/{token}, /sign/{token}/verify, /sign/{token}/sign
        if (rawPath.startsWith("/sign/")) {
            String[] parts = rawPath.split("/", -1);
            if (parts.length >= 3 && parts[2] != null && !parts[2].isBlank()) {
                parts[2] = "{token}";
                return String.join("/", parts);
            }
        }

        return rawPath;
    }

    private String sanitizeQueryString(String rawQueryString) {
        if (rawQueryString == null || rawQueryString.isBlank()) {
            return rawQueryString;
        }

        StringBuilder sanitized = new StringBuilder();
        String[] params = rawQueryString.split("&");
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            int equalsIndex = param.indexOf('=');
            String key = equalsIndex >= 0 ? param.substring(0, equalsIndex) : param;
            String value = equalsIndex >= 0 ? param.substring(equalsIndex + 1) : "";

            if (SENSITIVE_QUERY_KEYS.contains(key)) {
                value = "***";
            }

            if (i > 0) {
                sanitized.append('&');
            }
            sanitized.append(key);
            if (equalsIndex >= 0) {
                sanitized.append('=').append(value);
            }
        }

        return sanitized.toString();
    }
}
