package com.signly.signature.infrastructure.persistence.repository;

import com.signly.contract.domain.model.ContractId;
import com.signly.signature.domain.model.ContractSignature;
import com.signly.signature.domain.model.SignatureId;
import com.signly.signature.domain.repository.SignatureRepository;
import com.signly.signature.infrastructure.persistence.entity.SignatureEntity;
import com.signly.signature.infrastructure.persistence.mapper.SignatureEntityMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SignatureRepositoryImpl implements SignatureRepository {

    private final SignatureJpaRepository jpaRepository;
    private final SignatureEntityMapper mapper;

    public SignatureRepositoryImpl(SignatureJpaRepository jpaRepository, SignatureEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(ContractSignature signature) {
        SignatureEntity entity = mapper.toEntity(signature);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<ContractSignature> findById(SignatureId signatureId) {
        return jpaRepository.findById(signatureId.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<ContractSignature> findByContractId(ContractId contractId) {
        return jpaRepository.findByContractId(contractId.value())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ContractSignature> findByContractIdAndSignerEmail(ContractId contractId, String signerEmail) {
        return jpaRepository.findByContractIdAndSignerEmail(contractId.value(), signerEmail)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByContractIdAndSignerEmail(ContractId contractId, String signerEmail) {
        return jpaRepository.existsByContractIdAndSignerEmail(contractId.value(), signerEmail);
    }

    @Override
    public void delete(SignatureId signatureId) {
        jpaRepository.deleteById(signatureId.value());
    }
}