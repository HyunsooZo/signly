package com.signly.common.audit.application.dto;

import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.AuditDetails;
import com.signly.common.audit.domain.model.EntityType;
import com.signly.user.domain.model.UserId;

/**
 * 감사 로그 생성 커맨드
 */
public record CreateAuditLogCommand(
        EntityType entityType,
        String entityId,
        AuditAction action,
        UserId userId,
        String ipAddress,
        String userAgent,
        AuditDetails details
) {
    public CreateAuditLogCommand {
        if (entityType == null) {
            throw new IllegalArgumentException("Entity type은 필수입니다");
        }
        if (entityId == null || entityId.trim().isEmpty()) {
            throw new IllegalArgumentException("Entity ID는 필수입니다");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action은 필수입니다");
        }
    }
}