package com.signly.template.infrastructure.repository.jpa;

import com.signly.template.domain.model.TemplateStatus;
import com.signly.template.infrastructure.TemplateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemplateJpaRepository extends JpaRepository<TemplateEntity, String> {

    Page<TemplateEntity> findByOwnerId(String ownerId, Pageable pageable);

    Page<TemplateEntity> findByOwnerIdAndStatus(String ownerId, TemplateStatus status, Pageable pageable);

    @Query("SELECT t FROM TemplateEntity t WHERE t.ownerId = :ownerId AND t.status = 'ACTIVE'")
    List<TemplateEntity> findActiveTemplatesByOwnerId(@Param("ownerId") String ownerId);

    boolean existsByOwnerIdAndTitle(String ownerId, String title);

    long countByOwnerId(String ownerId);
}