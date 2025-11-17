package com.signly.contract.infrastructure.repository;

import com.signly.contract.infrastructure.entity.SignatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

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
