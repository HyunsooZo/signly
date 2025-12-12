package com.signly.common.audit.infrastructure.repository;

import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.EntityType;
import com.signly.common.audit.infrastructure.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 감사 로그 JPA Repository
 */
@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {

    /**
     * 엔티티 타입과 ID로 감사 로그 목록 조회
     */
    List<AuditLogEntity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            EntityType entityType,
            String entityId
    );

    /**
     * 엔티티 타입과 작업으로 감사 로그 목록 조회
     */
    List<AuditLogEntity> findByEntityTypeAndActionOrderByCreatedAtDesc(
            EntityType entityType,
            AuditAction action
    );

    /**
     * 사용자 ID로 감사 로그 목록 조회
     */
    List<AuditLogEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 기간 내 감사 로그 목록 조회
     */
    List<AuditLogEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * 복합 조건으로 감사 로그 목록 조회
     */
    @Query("""
            SELECT a FROM AuditLogEntity a
            WHERE (:entityType IS NULL OR a.entityType = :entityType)
            AND (:action IS NULL OR a.action = :action)
            AND (:userId IS NULL OR a.userId = :userId)
            AND (:start IS NULL OR a.createdAt >= :start)
            AND (:end IS NULL OR a.createdAt <= :end)
            ORDER BY a.createdAt DESC
            """)
    List<AuditLogEntity> findByConditions(
            @Param("entityType") EntityType entityType,
            @Param("action") AuditAction action,
            @Param("userId") String userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 기간 내 감사 로그 수 조회
     */
    @Query("SELECT COUNT(a) FROM AuditLogEntity a WHERE a.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 최근 감사 로그 목록 조회 (limit 적용)
     */
    @Query("""
            SELECT a FROM AuditLogEntity a
            WHERE (:entityType IS NULL OR a.entityType = :entityType)
            AND (:action IS NULL OR a.action = :action)
            AND (:userId IS NULL OR a.userId = :userId)
            ORDER BY a.createdAt DESC
            LIMIT :limit
            """)
    List<AuditLogEntity> findRecentByConditions(
            @Param("entityType") EntityType entityType,
            @Param("action") AuditAction action,
            @Param("userId") String userId,
            @Param("limit") int limit
    );
}