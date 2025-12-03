package com.signly.notification.infrastructure.persistence.repository;

import com.signly.notification.domain.model.EmailOutboxStatus;
import com.signly.notification.infrastructure.persistence.entity.EmailOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailOutboxJpaRepository extends JpaRepository<EmailOutboxEntity, String> {

    @Query("SELECT e FROM EmailOutboxEntity e WHERE e.status = :status " +
            "AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now) " +
            "ORDER BY e.createdAt ASC")
    List<EmailOutboxEntity> findPendingEmails(
            @Param("status") EmailOutboxStatus status,
            @Param("now") LocalDateTime now,
            @Param("limit") int limit
    );
}
