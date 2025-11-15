package com.signly.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis를 이용한 JWT 토큰 관리 서비스
 * 액세스 토큰을 Redis에 저장하여 토큰 무효화 및 관리
 */
@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 액세스 토큰을 Redis에 저장
     */
    public void saveAccessToken(
            String userId,
            String token
    ) {
        String key = ACCESS_TOKEN_PREFIX + userId;
        long ttl = jwtTokenProvider.getAccessTokenValidityInMs();
        redisTemplate.opsForValue().set(key, token, ttl, TimeUnit.MILLISECONDS);
    }

    /**
     * 리프레시 토큰을 Redis에 저장
     */
    public void saveRefreshToken(
            String userId,
            String token
    ) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        long ttl = jwtTokenProvider.getRefreshTokenValidityInMs();
        redisTemplate.opsForValue().set(key, token, ttl, TimeUnit.MILLISECONDS);
    }

    /**
     * Redis에서 액세스 토큰 조회
     */
    public String getAccessToken(String userId) {
        String key = ACCESS_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Redis에서 리프레시 토큰 조회
     */
    public String getRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 액세스 토큰이 Redis에 존재하는지 확인
     */
    public boolean isAccessTokenValid(
            String userId,
            String token
    ) {
        String storedToken = getAccessToken(userId);
        return token.equals(storedToken);
    }

    /**
     * 리프레시 토큰이 Redis에 존재하는지 확인
     */
    public boolean isRefreshTokenValid(
            String userId,
            String token
    ) {
        String storedToken = getRefreshToken(userId);
        return token.equals(storedToken);
    }

    /**
     * 액세스 토큰 삭제 (로그아웃)
     */
    public void deleteAccessToken(String userId) {
        String key = ACCESS_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }

    /**
     * 리프레시 토큰 삭제
     */
    public void deleteRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }

    /**
     * 모든 토큰 삭제 (로그아웃)
     */
    public void deleteAllTokens(String userId) {
        deleteAccessToken(userId);
        deleteRefreshToken(userId);
    }

    /**
     * 토큰 갱신 (기존 액세스 토큰 삭제 후 새로운 토큰 저장)
     */
    public void refreshAccessToken(
            String userId,
            String newToken
    ) {
        deleteAccessToken(userId);
        saveAccessToken(userId, newToken);
    }
}
