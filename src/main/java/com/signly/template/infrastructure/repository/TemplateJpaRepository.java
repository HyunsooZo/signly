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

    Page<TemplateEntity> findByOwnerId(String ownerId, Pageable pageable);

    Page<TemplateEntity> findByOwnerIdAndStatus(String ownerId, TemplateStatus status, Pageable pageable);

    @Query("SELECT t FROM TemplateEntity t WHERE t.ownerId = :ownerId AND t.status = 'ACTIVE'")
    List<TemplateEntity> findActiveTemplatesByOwnerId(@Param("ownerId") String ownerId);

    boolean existsByOwnerIdAndTitle(String ownerId, String title);

    long countByOwnerId(String ownerId);

    // 프리셋 관련 메서드
    @Query("SELECT t FROM TemplateEntity t WHERE t.isPreset = true AND t.status = 'ACTIVE'")
    List<TemplateEntity> findAllActivePresets();

    Optional<TemplateEntity> findByIsPresetTrueAndPresetId(String presetId);
}