package com.signly.common.audit.config;

import com.signly.common.audit.aop.AuditContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 감사 로그 컨텍스트 필터
 * 모든 HTTP 요청에 대해 IP 주소와 User-Agent를 ThreadLocal에 저장
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)  // 가장 먼저 실행
public class AuditContextFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    )
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            AuditContextHolder.setIpAddress(ipAddress);
            AuditContextHolder.setUserAgent(userAgent);

            log.debug("감사 컨텍스트 설정: ip={}, userAgent={}", ipAddress, userAgent);

            chain.doFilter(request, response);

        } finally {
            // 요청 처리 완료 후 ThreadLocal 정리 (메모리 누수 방지)
            AuditContextHolder.clear();
            log.debug("감사 컨텍스트 정리 완료");
        }
    }

    /**
     * 클라이언트 IP 주소 추출
     * 프록시/로드밸런서 환경을 고려하여 X-Forwarded-For 헤더 우선 확인
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For는 여러 IP가 콤마로 구분될 수 있음 (client, proxy1, proxy2)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}