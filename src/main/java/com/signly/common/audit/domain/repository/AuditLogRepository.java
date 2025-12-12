package com.signly.common.audit.domain.repository;

import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.AuditLog;
import com.signly.common.audit.domain.model.EntityType;
import com.signly.user.domain.model.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 감사 로그 리포지토리 인터페이스
 */
public interface AuditLogRepository {

    /**
     * 감사 로그 저장
     */
    AuditLog save(AuditLog auditLog);

    /**
     * ID로 감사 로그 조회
     */
    Optional<AuditLog> findById(Long logId);

    /**
     * 엔티티 타입과 ID로 감사 로그 목록 조회
     */
    List<AuditLog> findByEntityTypeAndEntityId(
            EntityType entityType,
            String entityId
    );

    /**
     * 엔티티 타입과 작업으로 감사 로그 목록 조회
     */
    List<AuditLog> findByEntityTypeAndAction(
            EntityType entityType,
            AuditAction action
    );

    /**
     * 사용자 ID로 감사 로그 목록 조회
     */
    List<AuditLog> findByUserId(UserId userId);

    /**
     * 기간 내 감사 로그 목록 조회
     */
    List<AuditLog> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * 복합 조건으로 감사 로그 목록 조회
     */
    List<AuditLog> findByConditions(
            EntityType entityType,
            AuditAction action,
            UserId userId,
            LocalDateTime start,
            LocalDateTime end,
            int limit
    );

    /**
     * 전체 감사 로그 수 조회
     */
    long count();

    /**
     * 기간 내 감사 로그 수 조회
     */
    long countByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}