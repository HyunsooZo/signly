package com.signly.contract.infrastructure.repository;

import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.infrastructure.entity.ContractJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ContractJpaRepository extends JpaRepository<ContractJpaEntity, String> {

    @Query("""
            SELECT c 
            FROM ContractJpaEntity c 
            WHERE c.creatorId = :creatorId 
            ORDER BY CASE WHEN c.presetType = 'LABOR_STANDARD' THEN 0 ELSE 1 END, c.createdAt DESC
            """)
    Page<ContractJpaEntity> findByCreatorId(
            @Param("creatorId") String creatorId,
            Pageable pageable
    );

    @Query("""
            SELECT c 
            FROM ContractJpaEntity c 
            WHERE c.creatorId = :creatorId AND c.status = :status 
            ORDER BY CASE WHEN c.presetType = 'LABOR_STANDARD' THEN 0 ELSE 1 END, c.createdAt DESC
            """)
    Page<ContractJpaEntity> findByCreatorIdAndStatus(
            @Param("creatorId") String creatorId,
            @Param("status") ContractStatus status,
            Pageable pageable
    );

    // 이메일 해시로 계약 조회 (Blind Index 사용)
    @Query("""
            SELECT c 
            FROM ContractJpaEntity c 
            WHERE c.firstPartyEmailHash = :emailHash OR c.secondPartyEmailHash = :emailHash 
            ORDER BY CASE WHEN c.presetType = 'LABOR_STANDARD' THEN 0 ELSE 1 END, c.createdAt DESC
            """)
    Page<ContractJpaEntity> findByPartyEmailHash(
            @Param("emailHash") String emailHash,
            Pageable pageable
    );

    @Query("""
            SELECT c 
            FROM ContractJpaEntity c 
            WHERE (c.firstPartyEmailHash = :emailHash OR c.secondPartyEmailHash = :emailHash) AND c.status = :status 
            ORDER BY CASE WHEN c.presetType = 'LABOR_STANDARD' THEN 0 ELSE 1 END, c.createdAt DESC
            """)
    Page<ContractJpaEntity> findByPartyEmailHashAndStatus(
            @Param("emailHash") String emailHash,
            @Param("status") ContractStatus status,
            Pageable pageable
    );

    List<ContractJpaEntity> findByTemplateId(String templateId);

    @Query("""
            SELECT c 
            FROM ContractJpaEntity c 
            LEFT JOIN FETCH c.signatures
            WHERE c.status IN (:statuses) AND c.expiresAt < :currentTime
            """)
    List<ContractJpaEntity> findExpiredContracts(
            @Param("statuses") List<ContractStatus> statuses,
            @Param("currentTime") LocalDateTime currentTime
    );

    List<ContractJpaEntity> findByStatusAndExpiresAtBefore(
            ContractStatus status,
            LocalDateTime dateTime
    );

    boolean existsByCreatorIdAndTitle(
            String creatorId,
            String title
    );

    long countByCreatorIdAndStatus(
            String creatorId,
            ContractStatus status
    );

    long countByTemplateId(String templateId);

    @Query("""
            SELECT c 
            FROM ContractJpaEntity c 
            LEFT JOIN FETCH c.signatures
            WHERE c.signToken = :signToken""")
    ContractJpaEntity findBySignToken(@Param("signToken") String signToken);
}