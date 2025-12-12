package com.signly.common.audit.domain.model;

import com.signly.common.domain.BaseEntity;
import com.signly.user.domain.model.UserId;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 감사 로그 Aggregate Root
 * 법적 효력을 갖는 모든 중요 작업을 기록
 */
@Getter
public class AuditLog extends BaseEntity {
    private final Long logId;
    private final EntityType entityType;
    private final String entityId;  // ULID 문자열
    private final AuditAction action;
    private final UserId userId;  // nullable (시스템 작업)
    private final String ipAddress;  // nullable
    private final String userAgent;  // nullable
    private final AuditDetails details;  // JSON
    private final LocalDateTime createdAt;

    private AuditLog(
            Long logId,
            EntityType entityType,
            String entityId,
            AuditAction action,
            UserId userId,
            String ipAddress,
            String userAgent,
            AuditDetails details,
            LocalDateTime createdAt
    ) {
        super(createdAt, createdAt);  // audit log는 불변
        this.logId = logId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.details = details;
        this.createdAt = createdAt;
    }

    public static AuditLog create(
            EntityType entityType,
            String entityId,
            AuditAction action,
            UserId userId,
            String ipAddress,
            String userAgent,
            AuditDetails details
    ) {
        validateEntityId(entityId);

        return new AuditLog(
                null,  // DB auto-increment
                entityType,
                entityId,
                action,
                userId,
                ipAddress,
                userAgent,
                details != null ? details : AuditDetails.empty(),
                LocalDateTime.now()
        );
    }

    private static void validateEntityId(String entityId) {
        if (entityId == null || entityId.trim().isEmpty()) {
            throw new IllegalArgumentException("Entity ID는 필수입니다");
        }
    }

    /**
     * 영속성 계층에서 사용하는 정적 팩토리 메서드
     */
    public static AuditLog restore(
            Long logId,
            EntityType entityType,
            String entityId,
            AuditAction action,
            UserId userId,
            String ipAddress,
            String userAgent,
            AuditDetails details,
            LocalDateTime createdAt
    ) {
        return new AuditLog(
                logId,
                entityType,
                entityId,
                action,
                userId,
                ipAddress,
                userAgent,
                details,
                createdAt
        );
    }

    /**
     * 시스템 작업 여부 확인 (userId가 null)
     */
    public boolean isSystemAction() {
        return userId == null;
    }

    /**
     * 사용자 작업 여부 확인
     */
    public boolean isUserAction() {
        return userId != null;
    }
}