package com.signly.template.infrastructure.repository.impl;

import com.signly.template.domain.model.ContractTemplate;
import com.signly.template.domain.model.TemplateId;
import com.signly.template.domain.model.TemplateStatus;
import com.signly.template.domain.repository.TemplateRepository;
import com.signly.user.domain.model.UserId;
import com.signly.template.infrastructure.TemplateEntity;
import com.signly.template.infrastructure.TemplateEntityMapper;
import com.signly.template.infrastructure.TemplateJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class TemplateRepositoryImpl implements TemplateRepository {

    private final TemplateJpaRepository templateJpaRepository;
    private final TemplateEntityMapper templateEntityMapper;

    public TemplateRepositoryImpl(TemplateJpaRepository templateJpaRepository, TemplateEntityMapper templateEntityMapper) {
        this.templateJpaRepository = templateJpaRepository;
        this.templateEntityMapper = templateEntityMapper;
    }

    @Override
    public ContractTemplate save(ContractTemplate template) {
        Optional<TemplateEntity> existingEntity = templateJpaRepository.findById(template.getTemplateId().getValue());

        if (existingEntity.isPresent()) {
            TemplateEntity entity = existingEntity.get();
            templateEntityMapper.updateEntity(entity, template);
            TemplateEntity savedEntity = templateJpaRepository.save(entity);
            return templateEntityMapper.toDomain(savedEntity);
        } else {
            TemplateEntity entity = templateEntityMapper.toEntity(template);
            TemplateEntity savedEntity = templateJpaRepository.save(entity);
            return templateEntityMapper.toDomain(savedEntity);
        }
    }

    @Override
    public Optional<ContractTemplate> findById(TemplateId templateId) {
        return templateJpaRepository.findById(templateId.getValue())
                .map(templateEntityMapper::toDomain);
    }

    @Override
    public Page<ContractTemplate> findByOwnerId(UserId ownerId, Pageable pageable) {
        Page<TemplateEntity> entities = templateJpaRepository.findByOwnerId(ownerId.getValue(), pageable);
        return entities.map(templateEntityMapper::toDomain);
    }

    @Override
    public Page<ContractTemplate> findByOwnerIdAndStatus(UserId ownerId, TemplateStatus status, Pageable pageable) {
        Page<TemplateEntity> entities = templateJpaRepository.findByOwnerIdAndStatus(ownerId.getValue(), status, pageable);
        return entities.map(templateEntityMapper::toDomain);
    }

    @Override
    public List<ContractTemplate> findActiveTemplatesByOwnerId(UserId ownerId) {
        List<TemplateEntity> entities = templateJpaRepository.findActiveTemplatesByOwnerId(ownerId.getValue());
        return entities.stream()
                .map(templateEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByOwnerIdAndTitle(UserId ownerId, String title) {
        return templateJpaRepository.existsByOwnerIdAndTitle(ownerId.getValue(), title);
    }

    @Override
    public void delete(ContractTemplate template) {
        templateJpaRepository.deleteById(template.getTemplateId().getValue());
    }

    @Override
    public long countByOwnerId(UserId ownerId) {
        return templateJpaRepository.countByOwnerId(ownerId.getValue());
    }
}