package com.signly.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Token Provider 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secretKey = "testSecretKeyForJWT12345678901234567890123456789012345678901234567890";
    private final long accessTokenValidity = 3600000L; // 1시간
    private final long refreshTokenValidity = 86400000L; // 24시간

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secretKey, accessTokenValidity, refreshTokenValidity);
    }

    @Test
    @DisplayName("액세스 토큰 생성 성공")
    void shouldCreateAccessTokenSuccessfully() {
        // Given
        String userId = "test-user-id";
        String email = "test@example.com";
        String userType = "INDIVIDUAL";

        // When
        String token = jwtTokenProvider.createAccessToken(userId, email, userType);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // 토큰에서 정보 추출 검증
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo(email);
        assertThat(jwtTokenProvider.getUserTypeFromToken(token)).isEqualTo(userType);
        assertThat(jwtTokenProvider.isAccessToken(token)).isTrue();
        assertThat(jwtTokenProvider.isRefreshToken(token)).isFalse();
    }

    @Test
    @DisplayName("리프레시 토큰 생성 성공")
    void shouldCreateRefreshTokenSuccessfully() {
        // Given
        String userId = "test-user-id";

        // When
        String token = jwtTokenProvider.createRefreshToken(userId);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // 토큰에서 정보 추출 검증
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.isRefreshToken(token)).isTrue();
        assertThat(jwtTokenProvider.isAccessToken(token)).isFalse();
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void shouldValidateValidToken() {
        // Given
        String token = jwtTokenProvider.createAccessToken("user-id", "test@example.com", "INDIVIDUAL");

        // When & Then
        assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void shouldFailValidationForExpiredToken() {
        // Given - 만료 시간이 매우 짧은 토큰 프로바이더 생성
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secretKey, 1L, refreshTokenValidity);
        String token = shortLivedProvider.createAccessToken("user-id", "test@example.com", "INDIVIDUAL");

        // When - 토큰 만료 대기
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        assertThat(shortLivedProvider.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("잘못된 토큰 검증 실패")
    void shouldFailValidationForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThat(jwtTokenProvider.isTokenValid(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("빈 토큰 검증 실패")
    void shouldFailValidationForEmptyToken() {
        // Given
        String emptyToken = "";

        // When & Then
        assertThat(jwtTokenProvider.isTokenValid(emptyToken)).isFalse();
        assertThat(jwtTokenProvider.isTokenValid(null)).isFalse();
    }

    @Test
    @DisplayName("토큰에서 사용자 정보 추출 성공")
    void shouldExtractUserInfoFromToken() {
        // Given
        String userId = "test-user-id";
        String email = "test@example.com";
        String userType = "CORPORATE";
        String token = jwtTokenProvider.createAccessToken(userId, email, userType);

        // When & Then
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo(email);
        assertThat(jwtTokenProvider.getUserTypeFromToken(token)).isEqualTo(userType);
    }

    @Test
    @DisplayName("토큰 만료 시간 추출 성공")
    void shouldExtractExpirationFromToken() {
        // Given
        String token = jwtTokenProvider.createAccessToken("user-id", "test@example.com", "INDIVIDUAL");
        LocalDateTime now = LocalDateTime.now();

        // When
        LocalDateTime expiration = jwtTokenProvider.getExpirationFromToken(token);

        // Then
        assertThat(expiration).isAfter(now);
        assertThat(expiration).isBefore(now.plusSeconds(accessTokenValidity / 1000 + 1));
    }

    @Test
    @DisplayName("잘못된 서명의 토큰 처리 시 예외 발생")
    void shouldThrowExceptionForTokenWithWrongSignature() {
        // Given
        String token = jwtTokenProvider.createAccessToken("user-id", "test@example.com", "INDIVIDUAL");
        
        // 다른 시크릿 키로 생성한 토큰처럼 조작
        String tamperedToken = token.substring(0, token.length() - 10) + "tampered";

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(tamperedToken))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Invalid JWT token");
    }

    @Test
    @DisplayName("토큰 유효성 시간 확인")
    void shouldReturnCorrectValidityPeriods() {
        // When & Then
        assertThat(jwtTokenProvider.getAccessTokenValidityInMs()).isEqualTo(accessTokenValidity);
        assertThat(jwtTokenProvider.getRefreshTokenValidityInMs()).isEqualTo(refreshTokenValidity);
    }

    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰 타입 구분")
    void shouldDistinguishAccessTokenAndRefreshToken() {
        // Given
        String userId = "test-user-id";
        String accessToken = jwtTokenProvider.createAccessToken(userId, "test@example.com", "INDIVIDUAL");
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        // When & Then
        assertThat(jwtTokenProvider.isAccessToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.isRefreshToken(accessToken)).isFalse();
        
        assertThat(jwtTokenProvider.isRefreshToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.isAccessToken(refreshToken)).isFalse();
    }
}