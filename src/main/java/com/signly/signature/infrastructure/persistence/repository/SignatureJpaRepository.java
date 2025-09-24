package com.signly.signature.infrastructure.persistence.repository;

import com.signly.signature.infrastructure.persistence.entity.SignatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SignatureJpaRepository extends JpaRepository<SignatureEntity, String> {

    List<SignatureEntity> findByContractId(String contractId);

    Optional<SignatureEntity> findByContractIdAndSignerEmail(String contractId, String signerEmail);

    boolean existsByContractIdAndSignerEmail(String contractId, String signerEmail);
}