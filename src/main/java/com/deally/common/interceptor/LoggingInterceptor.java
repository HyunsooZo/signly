package com.deally.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
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
        String uri = request.getRequestURI();
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
}