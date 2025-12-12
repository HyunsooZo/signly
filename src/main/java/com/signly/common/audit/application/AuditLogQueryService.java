package com.signly.common.audit.application;

import com.signly.common.audit.application.dto.AuditLogResponse;
import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.EntityType;
import com.signly.common.audit.domain.repository.AuditLogRepository;
import com.signly.user.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 감사 로그 조회 전용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogQueryService {

    private final AuditLogRepository auditLogRepository;

    /**
     * ID로 감사 로그 조회
     */
    public Optional<AuditLogResponse> findById(Long logId) {
        return auditLogRepository.findById(logId)
                .map(AuditLogResponse::from);
    }

    /**
     * 엔티티 타입과 ID로 감사 로그 목록 조회
     */
    public List<AuditLogResponse> findByEntityTypeAndEntityId(
            EntityType entityType,
            String entityId
    ) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }

    /**
     * 엔티티 타입과 작업으로 감사 로그 목록 조회
     */
    public List<AuditLogResponse> findByEntityTypeAndAction(
            EntityType entityType,
            AuditAction action
    ) {
        return auditLogRepository.findByEntityTypeAndAction(entityType, action)
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }

    /**
     * 사용자 ID로 감사 로그 목록 조회
     */
    public List<AuditLogResponse> findByUserId(UserId userId) {
        return auditLogRepository.findByUserId(userId)
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }

    /**
     * 기간 내 감사 로그 목록 조회
     */
    public List<AuditLogResponse> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    ) {
        return auditLogRepository.findByCreatedAtBetween(start, end)
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }

    /**
     * 복합 조건으로 감사 로그 목록 조회
     */
    public List<AuditLogResponse> findByConditions(
            EntityType entityType,
            AuditAction action,
            UserId userId,
            LocalDateTime start,
            LocalDateTime end,
            int limit
    ) {
        return auditLogRepository.findByConditions(entityType, action, userId, start, end, limit)
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }

    /**
     * 전체 감사 로그 수 조회
     */
    public long count() {
        return auditLogRepository.count();
    }

    /**
     * 기간 내 감사 로그 수 조회
     */
    public long countByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    ) {
        return auditLogRepository.countByCreatedAtBetween(start, end);
    }

    /**
     * 최근 감사 로그 목록 조회 (페이지네이션)
     */
    public Page<AuditLogResponse> findRecent(Pageable pageable) {
        // 현재는 전체 조회 후 페이지네이션 (향후 개선 가능)
        List<AuditLogResponse> allLogs = auditLogRepository.findByConditions(
                        null, null, null, null, null, 1000
                ).stream()
                .map(AuditLogResponse::from)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allLogs.size());

        List<AuditLogResponse> pageContent = start < allLogs.size()
                ? allLogs.subList(start, end)
                : List.of();

        return new PageImpl<>(pageContent, pageable, allLogs.size());
    }
}