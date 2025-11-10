package com.signly.contract.infrastructure.repository;

import com.signly.contract.domain.model.ContractId;
import com.signly.contract.domain.model.Signature;
import com.signly.contract.domain.repository.SignatureRepository;
import com.signly.contract.infrastructure.entity.SignatureEntity;
import com.signly.contract.infrastructure.mapper.SignatureEntityMapper;
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
    public void save(Signature signature) {
        SignatureEntity entity = mapper.toEntity(signature);
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
    public Optional<Signature> findByContractIdAndSignerEmail(ContractId contractId, String signerEmail) {
        return jpaRepository.findByContractIdAndSignerEmail(contractId.value(), signerEmail)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByContractIdAndSignerEmail(ContractId contractId, String signerEmail) {
        return jpaRepository.existsByContractIdAndSignerEmail(contractId.value(), signerEmail);
    }

    @Override
    public void delete(String signatureId) {
        jpaRepository.deleteById(signatureId);
    }
}