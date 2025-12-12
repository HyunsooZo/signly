package com.signly.common.audit.infrastructure.entity;

import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.AuditDetails;
import com.signly.common.audit.domain.model.AuditLog;
import com.signly.common.audit.domain.model.EntityType;
import com.signly.user.domain.model.UserId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 감사 로그 JPA Entity
 * 순수 데이터 홀더 역할, 비즈니스 로직 없음
 */
@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false, length = 26)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "user_id", length = 26)
    private String userId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "details", columnDefinition = "JSON")
    private String detailsJson;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime createdAt;

    @Builder
    private AuditLogEntity(
            Long logId,
            EntityType entityType,
            String entityId,
            AuditAction action,
            String userId,
            String ipAddress,
            String userAgent,
            String detailsJson,
            LocalDateTime createdAt
    ) {
        this.logId = logId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.detailsJson = detailsJson;
        this.createdAt = createdAt;
    }

    /**
     * 도메인 모델을 Entity로 변환
     */
    public static AuditLogEntity fromDomain(AuditLog auditLog) {
        return AuditLogEntity.builder()
                .logId(auditLog.getLogId())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .action(auditLog.getAction())
                .userId(auditLog.getUserId() != null ? auditLog.getUserId().value() : null)
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .detailsJson(auditLog.getDetails().toJson())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    /**
     * Entity를 도메인 모델로 변환
     */
    public AuditLog toDomain() {
        UserId userIdValue = this.userId != null ? UserId.of(this.userId) : null;
        AuditDetails details = AuditDetails.fromJson(this.detailsJson);

        return AuditLog.restore(
                this.logId,
                this.entityType,
                this.entityId,
                this.action,
                userIdValue,
                this.ipAddress,
                this.userAgent,
                details,
                this.createdAt
        );
    }
}