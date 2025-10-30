package com.signly.contract.infrastructure.repository;

import com.signly.contract.domain.model.Contract;
import com.signly.contract.domain.model.ContractId;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.domain.model.SignToken;
import com.signly.contract.domain.repository.ContractRepository;
import com.signly.template.domain.model.TemplateId;
import com.signly.user.domain.model.UserId;
import com.signly.contract.infrastructure.entity.ContractJpaEntity;
import com.signly.contract.infrastructure.mapper.ContractEntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ContractRepositoryImpl implements ContractRepository {

    private static final Logger logger = LoggerFactory.getLogger(ContractRepositoryImpl.class);

    private final ContractJpaRepository jpaRepository;
    private final ContractEntityMapper entityMapper;

    public ContractRepositoryImpl(ContractJpaRepository jpaRepository, ContractEntityMapper entityMapper) {
        this.jpaRepository = jpaRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    public Contract save(Contract contract) {
        ContractJpaEntity entity = entityMapper.toEntity(contract);
        ContractJpaEntity savedEntity = jpaRepository.save(entity);
        return entityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Contract> findById(ContractId contractId) {
        return jpaRepository.findById(contractId.getValue())
                .map(entityMapper::toDomain);
    }

    @Override
    public void delete(Contract contract) {
        jpaRepository.deleteById(contract.getId().getValue());
    }

    @Override
    public Page<Contract> findByCreatorId(UserId creatorId, Pageable pageable) {
        Page<ContractJpaEntity> entities = jpaRepository.findByCreatorId(creatorId.getValue(), pageable);
        return entities.map(entityMapper::toDomain);
    }

    @Override
    public Page<Contract> findByCreatorIdAndStatus(UserId creatorId, ContractStatus status, Pageable pageable) {
        Page<ContractJpaEntity> entities = jpaRepository.findByCreatorIdAndStatus(
                creatorId.getValue(), status, pageable);
        return entities.map(entityMapper::toDomain);
    }

    @Override
    public Page<Contract> findByPartyEmail(String email, Pageable pageable) {
        Page<ContractJpaEntity> entities = jpaRepository.findByPartyEmail(email, pageable);
        return entities.map(entityMapper::toDomain);
    }

    @Override
    public Page<Contract> findByPartyEmailAndStatus(String email, ContractStatus status, Pageable pageable) {
        Page<ContractJpaEntity> entities = jpaRepository.findByPartyEmailAndStatus(email, status, pageable);
        return entities.map(entityMapper::toDomain);
    }

    @Override
    public List<Contract> findByTemplateId(TemplateId templateId) {
        List<ContractJpaEntity> entities = jpaRepository.findByTemplateId(templateId.getValue());
        return entities.stream()
                .map(entityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findExpiredContracts(LocalDateTime currentTime) {
        // SIGNED 상태는 제외 - 양측 서명이 완료된 계약은 만료 처리하지 않음
        List<ContractStatus> statuses = List.of(ContractStatus.PENDING);
        List<ContractJpaEntity> entities = jpaRepository.findExpiredContracts(statuses, currentTime);
        return entities.stream()
                .map(entityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findByStatusAndExpiresAtBefore(ContractStatus status, LocalDateTime dateTime) {
        List<ContractJpaEntity> entities = jpaRepository.findByStatusAndExpiresAtBefore(status, dateTime);
        return entities.stream()
                .map(entityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCreatorIdAndTitle(UserId creatorId, String title) {
        return jpaRepository.existsByCreatorIdAndTitle(creatorId.getValue(), title);
    }

    @Override
    public long countByCreatorIdAndStatus(UserId creatorId, ContractStatus status) {
        return jpaRepository.countByCreatorIdAndStatus(creatorId.getValue(), status);
    }

    @Override
    public long countByTemplateId(TemplateId templateId) {
        return jpaRepository.countByTemplateId(templateId.getValue());
    }

    @Override
    public Optional<Contract> findBySignToken(SignToken signToken) {
        logger.info("DB 쿼리: findBySignToken - signToken={}", signToken.value());
        ContractJpaEntity entity = jpaRepository.findBySignToken(signToken.value());
        logger.info("DB 쿼리 결과: entity found={}", entity != null);
        if (entity != null) {
            logger.info("Entity details: id={}, signToken={}, status={}",
                entity.getId(), entity.getSignToken(), entity.getStatus());
        }
        return entity != null ? Optional.of(entityMapper.toDomain(entity)) : Optional.empty();
    }
}