package com.deally.contract.domain.repository;

import com.deally.contract.domain.model.ContractId;
import com.deally.contract.domain.model.Signature;

import java.util.List;
import java.util.Optional;

public interface SignatureRepository {
    void save(ContractId contractId, Signature signature);

    Optional<Signature> findById(String signatureId);

    List<Signature> findByContractId(ContractId contractId);

    Optional<Signature> findByContractIdAndSignerEmail(
            ContractId contractId,
            String signerEmail
    );

    boolean existsByContractIdAndSignerEmail(
            ContractId contractId,
            String signerEmail
    );

    void delete(String signatureId);
}
