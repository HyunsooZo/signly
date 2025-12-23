package com.signly.user.infrastructure.persistence.repository;

import com.signly.user.infrastructure.persistence.entity.PasswordHistoryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PasswordHistoryJpaRepository extends JpaRepository<PasswordHistoryEntity, Long> {

    @Query("""
            SELECT ph FROM PasswordHistoryEntity ph 
                       WHERE ph.userId = :userId 
                       AND ph.changedAt > :cutoffDate 
                       ORDER BY ph.changedAt DESC
            """)
    List<PasswordHistoryEntity> findRecentByUserIdWithin90Days(
            @Param("userId") String userId,
            @Param("cutoffDate") LocalDateTime cutoffDate,
            Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM PasswordHistoryEntity ph WHERE ph.changedAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
