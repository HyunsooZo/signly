package com.deally.document.infrastructure.persistence.repository;

import com.deally.document.domain.model.DocumentType;
import com.deally.document.infrastructure.persistence.entity.DocumentJpaEntity;
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

    // N+1 쿼리 문제 해결을 위한 최적화된 쿼리들
    @Query("SELECT d FROM DocumentJpaEntity d WHERE d.contractId = :contractId AND d.type = :type ORDER BY d.createdAt DESC")
    List<DocumentJpaEntity> findByContractIdAndTypeOrderByCreatedAtDesc(
            @Param("contractId") String contractId,
            @Param("type") DocumentType type
    );

    @Query("SELECT COUNT(d) FROM DocumentJpaEntity d WHERE d.contractId = :contractId")
    long countByContractId(@Param("contractId") String contractId);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DocumentJpaEntity d WHERE d.contractId = :contractId AND d.type = :type")
    boolean existsByContractIdAndType(
            @Param("contractId") String contractId,
            @Param("type") DocumentType type
    );

    @Query("SELECT d FROM DocumentJpaEntity d WHERE d.contractId = :contractId ORDER BY d.createdAt DESC")
    List<DocumentJpaEntity> findAllByContractIdOrderByCreatedAtDesc(@Param("contractId") String contractId);
}