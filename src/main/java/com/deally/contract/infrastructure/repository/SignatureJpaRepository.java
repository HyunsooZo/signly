package com.deally.contract.infrastructure.repository;

import com.deally.contract.infrastructure.entity.SignatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignatureJpaRepository extends JpaRepository<SignatureEntity, String> {

    List<SignatureEntity> findByContractId(String contractId);

    List<SignatureEntity> findAllByContractIdAndSignerEmailOrderBySignedAtDesc(
            String contractId,
            String signerEmail
    );

    boolean existsByContractIdAndSignerEmail(
            String contractId,
            String signerEmail
    );
}
