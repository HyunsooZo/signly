package com.signly.user.domain.model;

import com.signly.common.exception.ValidationException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 계정 해제 토큰 Value Object
 * - 계정 잠금 해제를 위한 토큰 값과 만료 시간을 캡슐화
 * - 불변 객체
 * - 이메일 링크를 통해 계정 잠금 해제 시 사용
 */
@Getter
public class AccountUnlockToken {

    private static final int EXPIRY_HOURS = 24;  // 24시간 유효

    private final String value;
    private final LocalDateTime expiryTime;

    private AccountUnlockToken(
            String value,
            LocalDateTime expiryTime
    ) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("계정 해제 토큰 값은 필수입니다");
        }
        if (expiryTime == null) {
            throw new ValidationException("만료 시간은 필수입니다");
        }

        this.value = value;
        this.expiryTime = expiryTime;
    }

    /**
     * 새로운 계정 해제 토큰 생성
     */
    public static AccountUnlockToken generate() {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(EXPIRY_HOURS);
        return new AccountUnlockToken(token, expiry);
    }

    /**
     * 기존 토큰 복원 (DB에서 조회 시)
     */
    public static AccountUnlockToken of(
            String value,
            LocalDateTime expiryTime
    ) {
        return new AccountUnlockToken(value, expiryTime);
    }

    /**
     * 문자열에서 AccountUnlockToken 생성 (임시 객체)
     */
    public static AccountUnlockToken from(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new ValidationException("계정 해제 토큰은 필수입니다");
        }
        // 임시 객체로 생성 (유효성 검증은 서비스에서 수행)
        return new AccountUnlockToken(token, LocalDateTime.now().plusHours(24));
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
            throw new ValidationException("유효하지 않은 계정 해제 링크입니다");
        }
        if (isExpired()) {
            throw new ValidationException("만료된 계정 해제 링크입니다. 고객지원에 문의해주세요.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountUnlockToken that = (AccountUnlockToken) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "AccountUnlockToken{" +
                "value='" + value.substring(0, 8) + "...', " +
                "expiryTime=" + expiryTime +
                '}';
    }
}
