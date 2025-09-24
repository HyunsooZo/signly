package com.signly.document.infrastructure.persistence.repository;

import com.signly.document.infrastructure.persistence.entity.DocumentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentJpaRepository extends JpaRepository<DocumentJpaEntity, String> {

    List<DocumentJpaEntity> findByContractIdOrderByCreatedAtDesc(String contractId);

    List<DocumentJpaEntity> findByUploadedByOrderByCreatedAtDesc(String uploadedBy);

    @Query("SELECT d FROM DocumentJpaEntity d WHERE d.contractId = :contractId AND d.type = 'CONTRACT_PDF'")
    DocumentJpaEntity findContractDocument(@Param("contractId") String contractId);

    @Query("SELECT d FROM DocumentJpaEntity d WHERE d.contractId = :contractId AND d.type = 'SIGNATURE_IMAGE'")
    List<DocumentJpaEntity> findSignatureDocuments(@Param("contractId") String contractId);
}