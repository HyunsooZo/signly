package com.signly.common.audit.infrastructure.repository;

import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.AuditLog;
import com.signly.common.audit.domain.model.EntityType;
import com.signly.common.audit.domain.repository.AuditLogRepository;
import com.signly.user.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 감사 로그 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogJpaRepository jpaRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        var entity = com.signly.common.audit.infrastructure.entity.AuditLogEntity.fromDomain(auditLog);
        var savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<AuditLog> findById(Long logId) {
        return jpaRepository.findById(logId)
                .map(com.signly.common.audit.infrastructure.entity.AuditLogEntity::toDomain);
    }

    @Override
    public List<AuditLog> findByEntityTypeAndEntityId(
            EntityType entityType,
            String entityId
    ) {
        return jpaRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(com.signly.common.audit.infrastructure.entity.AuditLogEntity::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByEntityTypeAndAction(
            EntityType entityType,
            AuditAction action
    ) {
        return jpaRepository.findByEntityTypeAndActionOrderByCreatedAtDesc(entityType, action)
                .stream()
                .map(com.signly.common.audit.infrastructure.entity.AuditLogEntity::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByUserId(UserId userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId.value())
                .stream()
                .map(com.signly.common.audit.infrastructure.entity.AuditLogEntity::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    ) {
        return jpaRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end)
                .stream()
                .map(com.signly.common.audit.infrastructure.entity.AuditLogEntity::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByConditions(
            EntityType entityType,
            AuditAction action,
            UserId userId,
            LocalDateTime start,
            LocalDateTime end,
            int limit
    ) {
        String userIdValue = userId != null ? userId.value() : null;

        return jpaRepository.findRecentByConditions(entityType, action, userIdValue, limit)
                .stream()
                .map(com.signly.common.audit.infrastructure.entity.AuditLogEntity::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    ) {
        return jpaRepository.countByCreatedAtBetween(start, end);
    }
}