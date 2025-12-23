package com.signly.user.domain.model;

import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * 비밀번호 이력 Domain Model
 * - 비밀번호 변경 이력을 추적
 * - 90일 이내 최근 3개 비밀번호 재사용 방지
 */
@Getter
public class PasswordHistory {

    private final Long id;
    private final UserId userId;
    private final EncodedPassword passwordHash;
    private final LocalDateTime changedAt;
    private final String ipAddress;
    private final String userAgent;
    private final LocalDateTime createdAt;

    private PasswordHistory(
            Long id,
            UserId userId,
            EncodedPassword passwordHash,
            LocalDateTime changedAt,
            String ipAddress,
            String userAgent,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.changedAt = changedAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    /**
     * 새 비밀번호 이력 생성
     */
    public static PasswordHistory create(
            UserId userId,
            EncodedPassword passwordHash,
            String ipAddress,
            String userAgent
    ) {
        return new PasswordHistory(
                null,
                userId,
                passwordHash,
                LocalDateTime.now(),
                ipAddress,
                userAgent,
                LocalDateTime.now()
        );
    }

    /**
     * 영속성 계층에서 복원
     */
    public static PasswordHistory restore(
            Long id,
            UserId userId,
            String passwordHash,
            LocalDateTime changedAt,
            String ipAddress,
            String userAgent,
            LocalDateTime createdAt
    ) {
        return new PasswordHistory(
                id,
                userId,
                EncodedPassword.of(passwordHash),
                changedAt,
                ipAddress,
                userAgent,
                createdAt
        );
    }

    /**
     * 비밀번호 일치 여부 확인
     */
    public boolean matches(Password password, PasswordEncoder encoder) {
        return passwordHash.matches(password, encoder);
    }

    /**
     * 90일 이내 이력인지 확인
     */
    public boolean isWithin90Days() {
        return changedAt.isAfter(LocalDateTime.now().minusDays(90));
    }

    /**
     * 비밀번호 해시 값 반환 (영속성 계층 전용)
     */
    public String getPasswordHashValue() {
        return passwordHash.value();
    }
}
