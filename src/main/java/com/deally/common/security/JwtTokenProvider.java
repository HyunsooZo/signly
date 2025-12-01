package com.deally.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    @Getter
    private final long accessTokenValidityInMs;
    @Getter
    private final long refreshTokenValidityInMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:mySecretKeyForJWT123456789012345678901234567890}") String secretKeyString,
            @Value("${app.jwt.access-token-validity-in-ms:3600000}") long accessTokenValidityInMs,
            @Value("${app.jwt.refresh-token-validity-in-ms:86400000}") long refreshTokenValidityInMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        this.accessTokenValidityInMs = accessTokenValidityInMs;
        this.refreshTokenValidityInMs = refreshTokenValidityInMs;
    }

    public String createAccessToken(
            String userId,
            String email,
            String userType
    ) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMs);

        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .claim("userType", userType)
                .claim("tokenType", "ACCESS")
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String createRefreshToken(String userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMs);

        return Jwts.builder()
                .setSubject(userId)
                .claim("tokenType", "REFRESH")
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new SecurityException("Invalid JWT token", e);
        }
    }

    public String getUserIdFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email", String.class);
    }

    public String getUserTypeFromToken(String token) {
        return getClaimsFromToken(token).get("userType", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return "ACCESS".equals(claims.get("tokenType", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return "REFRESH".equals(claims.get("tokenType", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public LocalDateTime getExpirationFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}