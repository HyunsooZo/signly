package com.signly.contract.infrastructure.repository;

import com.signly.contract.domain.model.ContractId;
import com.signly.contract.domain.model.Signature;
import com.signly.contract.domain.repository.SignatureRepository;
import com.signly.contract.infrastructure.entity.ContractJpaEntity;
import com.signly.contract.infrastructure.entity.SignatureEntity;
import com.signly.contract.infrastructure.mapper.SignatureEntityMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SignatureRepositoryImpl implements SignatureRepository {

    private final SignatureJpaRepository jpaRepository;
    private final ContractJpaRepository contractJpaRepository;
    private final SignatureEntityMapper mapper;

    public SignatureRepositoryImpl(
            SignatureJpaRepository jpaRepository,
            ContractJpaRepository contractJpaRepository,
            SignatureEntityMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.contractJpaRepository = contractJpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(ContractId contractId, Signature signature) {
        SignatureEntity entity = mapper.toEntity(signature);
        ContractJpaEntity contract = contractJpaRepository.findById(contractId.value())
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId.value()));
        entity.setContract(contract);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Signature> findById(String signatureId) {
        return jpaRepository.findById(signatureId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Signature> findByContractId(ContractId contractId) {
        return jpaRepository.findByContractId(contractId.value())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Signature> findByContractIdAndSignerEmail(
            ContractId contractId,
            String signerEmail
    ) {
        return jpaRepository
                .findTopByContractIdAndSignerEmailOrderBySignedAtDesc(contractId.value(), signerEmail)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByContractIdAndSignerEmail(
            ContractId contractId,
            String signerEmail
    ) {
        return jpaRepository.existsByContractIdAndSignerEmail(contractId.value(), signerEmail);
    }

    @Override
    public void delete(String signatureId) {
        jpaRepository.deleteById(signatureId);
    }
}
