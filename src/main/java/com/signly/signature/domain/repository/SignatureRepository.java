package com.signly.signature.domain.repository;

import com.signly.contract.domain.model.ContractId;
import com.signly.signature.domain.model.ContractSignature;
import com.signly.signature.domain.model.SignatureId;

import java.util.List;
import java.util.Optional;

public interface SignatureRepository {
    void save(ContractSignature signature);

    Optional<ContractSignature> findById(SignatureId signatureId);

    List<ContractSignature> findByContractId(ContractId contractId);

    Optional<ContractSignature> findByContractIdAndSignerEmail(ContractId contractId, String signerEmail);

    boolean existsByContractIdAndSignerEmail(ContractId contractId, String signerEmail);

    void delete(SignatureId signatureId);
}