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

    Page<ContractJpaEntity> findByCreatorId(String creatorId, Pageable pageable);

    Page<ContractJpaEntity> findByCreatorIdAndStatus(String creatorId, ContractStatus status, Pageable pageable);

    @Query("SELECT c FROM ContractJpaEntity c WHERE c.firstPartyEmail = :email OR c.secondPartyEmail = :email")
    Page<ContractJpaEntity> findByPartyEmail(@Param("email") String email, Pageable pageable);

    @Query("SELECT c FROM ContractJpaEntity c WHERE (c.firstPartyEmail = :email OR c.secondPartyEmail = :email) AND c.status = :status")
    Page<ContractJpaEntity> findByPartyEmailAndStatus(@Param("email") String email, @Param("status") ContractStatus status, Pageable pageable);

    List<ContractJpaEntity> findByTemplateId(String templateId);

    @Query("SELECT c FROM ContractJpaEntity c WHERE c.status IN ('PENDING', 'SIGNED') AND c.expiresAt < :currentTime")
    List<ContractJpaEntity> findExpiredContracts(@Param("currentTime") LocalDateTime currentTime);

    List<ContractJpaEntity> findByStatusAndExpiresAtBefore(ContractStatus status, LocalDateTime dateTime);

    boolean existsByCreatorIdAndTitle(String creatorId, String title);

    long countByCreatorIdAndStatus(String creatorId, ContractStatus status);

    long countByTemplateId(String templateId);

    @Query("SELECT c FROM ContractJpaEntity c WHERE c.signToken = :signToken")
    ContractJpaEntity findBySignToken(@Param("signToken") String signToken);
}