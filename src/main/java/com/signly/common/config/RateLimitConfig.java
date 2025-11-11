package com.signly.common.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Slf4j
@Configuration
@EnableConfigurationProperties(RateLimitConfig.RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry(RateLimitProperties properties) {
        RateLimiterConfig authConfig = RateLimiterConfig.custom()
                .limitForPeriod(properties.getAuth().getLogin().getLimit())
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        RateLimiterConfig apiConfig = RateLimiterConfig.custom()
                .limitForPeriod(properties.getApi().getDefault().getLimit())
                .limitRefreshPeriod(Duration.ofHours(1))
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(authConfig);
        registry.rateLimiter("auth", authConfig);
        registry.rateLimiter("api", apiConfig);

        return registry;
    }

    @Bean
    public RateLimitService rateLimitService(RateLimiterRegistry rateLimiterRegistry, RateLimitProperties properties) {
        return new RateLimitService(rateLimiterRegistry, properties);
    }

    @ConfigurationProperties(prefix = "app.rate-limit")
    public static class RateLimitProperties {
        private boolean enabled = true;
        private AuthProperties auth = new AuthProperties();
        private ApiProperties api = new ApiProperties();

        public boolean isEnabled() {return enabled;}

        public void setEnabled(boolean enabled) {this.enabled = enabled;}

        public AuthProperties getAuth() {return auth;}

        public void setAuth(AuthProperties auth) {this.auth = auth;}

        public ApiProperties getApi() {return api;}

        public void setApi(ApiProperties api) {this.api = api;}

        public static class AuthProperties {
            private EndpointProperties login = new EndpointProperties(5, "1m");
            private EndpointProperties register = new EndpointProperties(3, "5m");
            private EndpointProperties forgotPassword = new EndpointProperties(3, "15m");

            public EndpointProperties getLogin() {return login;}

            public void setLogin(EndpointProperties login) {this.login = login;}

            public EndpointProperties getRegister() {return register;}

            public void setRegister(EndpointProperties register) {this.register = register;}

            public EndpointProperties getForgotPassword() {return forgotPassword;}

            public void setForgotPassword(EndpointProperties forgotPassword) {this.forgotPassword = forgotPassword;}
        }

        public static class ApiProperties {
            private EndpointProperties default_ = new EndpointProperties(100, "1h");
            private EndpointProperties upload = new EndpointProperties(10, "1h");
            private EndpointProperties pdfGeneration = new EndpointProperties(20, "1h");

            public EndpointProperties getDefault() {return default_;}

            public void setDefault(EndpointProperties default_) {this.default_ = default_;}

            public EndpointProperties getUpload() {return upload;}

            public void setUpload(EndpointProperties upload) {this.upload = upload;}

            public EndpointProperties getPdfGeneration() {return pdfGeneration;}

            public void setPdfGeneration(EndpointProperties pdfGeneration) {this.pdfGeneration = pdfGeneration;}
        }

        public static class EndpointProperties {
            private int limit;
            private String window;

            public EndpointProperties() {}

            public EndpointProperties(
                    int limit,
                    String window
            ) {
                this.limit = limit;
                this.window = window;
            }

            public int getLimit() {return limit;}

            public void setLimit(int limit) {this.limit = limit;}

            public String getWindow() {return window;}

            public void setWindow(String window) {this.window = window;}

            public Duration getWindowDuration() {
                if (window.endsWith("m")) {
                    return Duration.ofMinutes(Long.parseLong(window.substring(0, window.length() - 1)));
                } else if (window.endsWith("h")) {
                    return Duration.ofHours(Long.parseLong(window.substring(0, window.length() - 1)));
                } else if (window.endsWith("s")) {
                    return Duration.ofSeconds(Long.parseLong(window.substring(0, window.length() - 1)));
                }
                return Duration.ofMinutes(1);
            }
        }
    }

    public static class RateLimitService {
        private final RateLimiterRegistry rateLimiterRegistry;
        private final RateLimitProperties properties;
        private final Cache<String, RateLimiter> ipLimiters;

        public RateLimitService(
                RateLimiterRegistry rateLimiterRegistry,
                RateLimitProperties properties
        ) {
            this.rateLimiterRegistry = rateLimiterRegistry;
            this.properties = properties;
            this.ipLimiters = Caffeine.newBuilder()
                    .maximumSize(10_000)  // 최대 10,000개 IP 캐시
                    .expireAfterAccess(Duration.ofHours(1))  // 1시간 미사용 시 제거
                    .build();
        }

        public boolean tryAcquire(
                String limiterName,
                String clientIp
        ) {
            String key = limiterName + ":" + clientIp;
            RateLimiter limiter = ipLimiters.get(key, k -> {
                RateLimiterConfig config;
                if ("auth".equals(limiterName)) {
                    config = RateLimiterConfig.custom()
                            .limitForPeriod(properties.getAuth().getLogin().getLimit())
                            .limitRefreshPeriod(Duration.ofMinutes(1))
                            .timeoutDuration(Duration.ofSeconds(0))
                            .build();
                } else {
                    config = RateLimiterConfig.custom()
                            .limitForPeriod(properties.getApi().getDefault().getLimit())
                            .limitRefreshPeriod(Duration.ofHours(1))
                            .timeoutDuration(Duration.ofSeconds(0))
                            .build();
                }
                return RateLimiter.of(key, config);
            });

            return limiter.acquirePermission();
        }

        public String getClientIp(HttpServletRequest request) {
            // 신뢰할 수 있는 프록시 목록 (Nginx, CloudFlare, AWS ALB 등)
            String remoteAddr = request.getRemoteAddr();
            
            // 프록시를 통하지 않은 직접 연결인 경우
            if (!isTrustedProxy(remoteAddr)) {
                return remoteAddr;
            }
            
            // 신뢰할 수 있는 프록시를 통한 경우에만 X-Forwarded-For 사용
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isEmpty()) {
                // 첫 번째 IP (실제 클라이언트 IP)
                String clientIp = xfHeader.split(",")[0].trim();
                // IP 형식 검증
                if (isValidIp(clientIp)) {
                    return clientIp;
                }
            }
            
            return remoteAddr;
        }
        
        private boolean isTrustedProxy(String ip) {
            // 로컬 환경 또는 신뢰할 수 있는 프록시 IP 대역
            return ip.equals("127.0.0.1") || 
                   ip.equals("::1") ||
                   ip.startsWith("10.") ||      // Private network
                   ip.startsWith("172.16.") ||  // Private network
                   ip.startsWith("192.168.");   // Private network
        }
        
        private boolean isValidIp(String ip) {
            // IPv4 형식 검증 (간단한 정규식)
            return ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
        }
    }
}