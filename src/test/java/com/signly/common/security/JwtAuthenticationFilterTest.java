package com.signly.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Filter 테스트")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private TokenRedisService tokenRedisService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private final String validToken = "valid.access.token";
    private final String refreshToken = "valid.refresh.token";
    private final String userId = "test-user-id";
    private final String email = "test@example.com";
    private final String userType = "OWNER";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 Authorization 헤더 토큰으로 인증 성공")
    void shouldAuthenticateWithValidAuthorizationHeader() throws Exception {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        
        when(jwtTokenProvider.isTokenValid(validToken)).thenReturn(true);
        when(jwtTokenProvider.isAccessToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn(email);
        when(jwtTokenProvider.getUserTypeFromToken(validToken)).thenReturn(userType);
        when(tokenRedisService.isAccessTokenValid(userId, validToken)).thenReturn(true);
        
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        
        verify(filterChain).doFilter(request, response);
        verify(tokenRedisService).isAccessTokenValid(userId, validToken);
        verify(userDetailsService).loadUserByUsername(email);
    }

    @Test
    @DisplayName("쿠키의 유효한 토큰으로 인증 성공")
    void shouldAuthenticateWithValidCookieToken() throws Exception {
        // Given
        Cookie authCookie = new Cookie("authToken", validToken);
        request.setCookies(authCookie);
        
        when(jwtTokenProvider.isTokenValid(validToken)).thenReturn(true);
        when(jwtTokenProvider.isAccessToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn(email);
        when(jwtTokenProvider.getUserTypeFromToken(validToken)).thenReturn(userType);
        when(tokenRedisService.isAccessTokenValid(userId, validToken)).thenReturn(true);
        
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰이 없을 때 인증 건너뛰기")
    void shouldSkipAuthenticationWhenNoToken() throws Exception {
        // Given - 토큰 없음

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).isTokenValid(anyString());
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 인증 실패 및 다음 필터 진행")
    void shouldFailAuthenticationWithInvalidToken() throws Exception {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtTokenProvider.isTokenValid(validToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        assertThat(response.getStatus()).isEqualTo(200);
        
        // filterChain이 호출되어야 함 (인증 없이 진행)
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Redis에서 토큰이 유효하지 않을 때 인증 실패")
    void shouldFailAuthenticationWhenTokenNotValidInRedis() throws Exception {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtTokenProvider.isTokenValid(validToken)).thenReturn(true);
        when(jwtTokenProvider.isAccessToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);
        when(tokenRedisService.isAccessTokenValid(userId, validToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("리프레시 토큰이 없을 때 인증 없이 진행")
    void shouldSkipAuthenticationWhenNoRefreshToken() throws Exception {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtTokenProvider.isTokenValid(validToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("리프레시 토큰이 액세스 토큰으로 사용될 때 건너뛰기")
    void shouldSkipWhenRefreshTokenUsedAsAccessToken() throws Exception {
        // Given
        request.addHeader("Authorization", "Bearer " + refreshToken);
        when(jwtTokenProvider.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.isAccessToken(refreshToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("요청 속성에 사용자 정보 설정")
    void shouldSetUserAttributesInRequest() throws Exception {
        // Given
        request.addHeader("Authorization", "Bearer " + validToken);
        
        when(jwtTokenProvider.isTokenValid(validToken)).thenReturn(true);
        when(jwtTokenProvider.isAccessToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn(email);
        when(jwtTokenProvider.getUserTypeFromToken(validToken)).thenReturn(userType);
        when(tokenRedisService.isAccessTokenValid(userId, validToken)).thenReturn(true);
        
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(request.getAttribute("userId")).isEqualTo(userId);
        assertThat(request.getAttribute("userEmail")).isEqualTo(email);
        assertThat(request.getAttribute("userType")).isEqualTo(userType);
    }
}