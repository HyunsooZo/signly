package com.signly.user.domain.model;

import com.signly.common.exception.ValidationException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 이메일 인증 토큰 Value Object
 * - 토큰 값과 만료 시간을 캡슐화
 * - 불변 객체
 */
@Getter
public class VerificationToken {

    private static final int EXPIRY_HOURS = 24;  // 24시간 유효

    private final String value;
    private final LocalDateTime expiryTime;

    private VerificationToken(
            String value,
            LocalDateTime expiryTime
    ) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("인증 토큰 값은 필수입니다");
        }
        if (expiryTime == null) {
            throw new ValidationException("만료 시간은 필수입니다");
        }

        this.value = value;
        this.expiryTime = expiryTime;
    }

    /**
     * 새로운 인증 토큰 생성
     */
    public static VerificationToken generate() {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(EXPIRY_HOURS);
        return new VerificationToken(token, expiry);
    }

    /**
     * 기존 토큰 복원 (DB에서 조회 시)
     */
    public static VerificationToken of(
            String value,
            LocalDateTime expiryTime
    ) {
        return new VerificationToken(value, expiryTime);
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * 토큰 일치 여부 확인
     */
    public boolean matches(String token) {
        if (token == null) {
            return false;
        }
        return this.value.equals(token);
    }

    /**
     * 토큰 유효성 검증 (만료 + 일치)
     */
    public void validate(String token) {
        if (!matches(token)) {
            throw new ValidationException("유효하지 않은 인증 토큰입니다");
        }
        if (isExpired()) {
            throw new ValidationException("만료된 인증 토큰입니다");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VerificationToken that = (VerificationToken) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "VerificationToken{" +
                "value='" + value.substring(0, 8) + "...', " +
                "expiryTime=" + expiryTime +
                '}';
    }
}
