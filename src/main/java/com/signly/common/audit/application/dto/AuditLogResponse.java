package com.signly.common.audit.application.dto;

import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.AuditDetails;
import com.signly.common.audit.domain.model.EntityType;
import com.signly.user.domain.model.UserId;

import java.time.LocalDateTime;

/**
 * 감사 로그 응답 DTO
 */
public record AuditLogResponse(
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
    public static AuditLogResponse from(com.signly.common.audit.domain.model.AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getLogId(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getAction(),
                auditLog.getUserId(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getDetails(),
                auditLog.getCreatedAt()
        );
    }
}