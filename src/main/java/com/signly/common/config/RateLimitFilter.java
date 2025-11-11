package com.signly.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig.RateLimitService rateLimitService;
    private final RateLimitConfig.RateLimitProperties properties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // POST 요청만 rate limiting 적용
        if (!"POST".equals(method) && !shouldApplyRateLimit(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = rateLimitService.getClientIp(request);
        String limiterName = getLimiterName(requestURI);

        if (limiterName == null) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean allowed = rateLimitService.tryAcquire(limiterName, clientIp);

        if (allowed) {
            log.debug("Rate limit check passed - IP: {}, Endpoint: {}, Limiter: {}", 
                     clientIp, requestURI, limiterName);
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded - IP: {}, Endpoint: {}, Limiter: {}, Method: {}, UserAgent: {}", 
                    clientIp, requestURI, limiterName, method, request.getHeader("User-Agent"));

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("X-RateLimit-Retry-After", "60");
            response.getWriter().write("""
                    {
                        "error": "Too Many Requests",
                        "message": "Rate limit exceeded. Please try again later.",
                        "retryAfter": 60
                    }
                    """);
        }
    }

    private boolean shouldApplyRateLimit(String requestURI) {
        return requestURI.startsWith("/api/") ||
                requestURI.equals("/login") ||
                requestURI.equals("/register") ||
                requestURI.equals("/forgot-password");
    }

    private String getLimiterName(String requestURI) {
        if (requestURI.equals("/login") ||
                requestURI.equals("/register") ||
                requestURI.equals("/forgot-password") ||
                requestURI.startsWith("/api/users/login") ||
                requestURI.startsWith("/api/users/register") ||
                requestURI.startsWith("/api/auth/")) {
            return "auth";
        }

        if (requestURI.startsWith("/api/")) {
            return "api";
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/static/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/fonts/") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs");
    }
}