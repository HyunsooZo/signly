package com.signly.template.infrastructure.repository;

import com.signly.template.domain.model.TemplateStatus;
import com.signly.template.infrastructure.entity.TemplateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TemplateJpaRepository extends JpaRepository<TemplateEntity, String> {

    @Query("""
            SELECT t 
             FROM TemplateEntity t 
             WHERE t.ownerId = :ownerId 
             ORDER BY CASE WHEN t.isPreset = true THEN 0 ELSE 1 END, t.createdAt DESC
            """)
    Page<TemplateEntity> findByOwnerId(
            @Param("ownerId") String ownerId,
            Pageable pageable
    );

    @Query("""
           SELECT t 
            FROM TemplateEntity t 
            WHERE t.ownerId = :ownerId AND t.status = :status 
           ORDER BY CASE WHEN t.isPreset = true THEN 0 ELSE 1 END, t.createdAt DESC
            """)
    Page<TemplateEntity> findByOwnerIdAndStatus(
            @Param("ownerId") String ownerId,
            @Param("status") TemplateStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT t 
            FROM TemplateEntity t 
            WHERE t.ownerId = :ownerId AND t.status = 'ACTIVE' 
            ORDER BY CASE WHEN t.isPreset = true THEN 0 ELSE 1 END, t.createdAt DESC
            """)
    List<TemplateEntity> findActiveTemplatesByOwnerId(@Param("ownerId") String ownerId);

    boolean existsByOwnerIdAndTitle(
            String ownerId,
            String title
    );

    long countByOwnerId(String ownerId);

    long countByOwnerIdAndStatus(
            String ownerId,
            TemplateStatus status
    );

    // 프리셋 관련 메서드
    @Query("""
           SELECT t 
            FROM TemplateEntity t 
            WHERE t.isPreset = true AND t.status = 'ACTIVE'
            """)
    List<TemplateEntity> findAllActivePresets();

    Optional<TemplateEntity> findByIsPresetTrueAndPresetId(String presetId);
}