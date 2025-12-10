package com.signly.common.security;

import com.signly.common.config.RateLimitConfig;
import com.signly.common.config.RateLimitFilter;
import com.signly.core.auth.CustomOAuth2UserService;
import com.signly.core.auth.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final RateLimitConfig.RateLimitService rateLimitService;
    private final RateLimitConfig.RateLimitProperties rateLimitProperties;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionFixation(sessionFixation -> sessionFixation.none())
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(authz -> authz
                        // 서명 관련 페이지는 완전히 public으로 처리
                        .requestMatchers("/sign/**").permitAll()
                        // JSP 뷰 파일들도 허용 (Forward 시 필요)
                        .requestMatchers("/WEB-INF/views/sign/**").permitAll()
                        // 정적 리소스는 공개 허용
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        // API 인증 엔드포인트는 공개 허용
                        .requestMatchers("/api/auth/**").permitAll()
                        // 사용자 회원가입/로그인은 공개 허용
                        .requestMatchers("/api/users/register", "/api/users/login", "/api/users/check-email").permitAll()
                        // 템플릿 API는 임시로 공개 허용 (개발/테스트용)
                        .requestMatchers("/api/templates/**").permitAll()
                        // 프리셋 템플릿은 공개 허용
                        .requestMatchers("/templates/presets/**").permitAll()
                        // 서명 API는 공개 허용 (토큰 검증은 별도 로직에서)
                        .requestMatchers("/api/sign/**").permitAll()
                        // 인증 관련 페이지는 공개 허용
                        .requestMatchers("/", "/login", "/register", "/forgot-password").permitAll()
                        .requestMatchers("/verify-email", "/email-verified", "/email-verify-error", "/registration-pending", "/resend-verification").permitAll()
                        .requestMatchers("/WEB-INF/views/auth/**").permitAll()
                        // OAuth2 로그인 엔드포인트 허용
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        // 개발 도구는 공개 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // 에러 페이지는 공개 허용 (인증 실패 시 리다이렉트 방지)
                        .requestMatchers("/error").permitAll()
                        // JSP Forward를 위한 뷰 경로는 permitAll (실제 접근은 컨트롤러에서 제어)
                        .requestMatchers("/WEB-INF/views/**").permitAll()
                        // 프로필 관리 페이지는 인증 필요
                        .requestMatchers("/profile/**").authenticated()
                        // 홈, 템플릿(프리셋 제외), 계약서 페이지는 인증 필요
                        .requestMatchers("/home", "/templates", "/contracts/**").authenticated()
                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .addFilterBefore(rateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 환경변수에서 허용된 origins 가져오기
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // 보안을 위해 필요한 HTTP 메서드만 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 보안을 위해 필요한 헤더만 허용
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter(rateLimitService, rateLimitProperties);
    }
}
