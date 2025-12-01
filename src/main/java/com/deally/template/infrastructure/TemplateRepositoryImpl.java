package com.deally.template.infrastructure.repository.impl;

import com.deally.template.domain.model.ContractTemplate;
import com.deally.template.domain.model.TemplateId;
import com.deally.template.domain.model.TemplateStatus;
import com.deally.template.domain.repository.TemplateRepository;
import com.deally.template.infrastructure.entity.TemplateEntity;
import com.deally.template.infrastructure.mapper.TemplateEntityMapper;
import com.deally.template.infrastructure.repository.TemplateJpaRepository;
import com.deally.user.domain.model.UserId;
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

    public TemplateRepositoryImpl(
            TemplateJpaRepository templateJpaRepository,
            TemplateEntityMapper templateEntityMapper
    ) {
        this.templateJpaRepository = templateJpaRepository;
        this.templateEntityMapper = templateEntityMapper;
    }

    @Override
    public ContractTemplate save(ContractTemplate template) {
        Optional<TemplateEntity> existingEntity = templateJpaRepository.findById(template.getTemplateId().value());

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
        return templateJpaRepository.findById(templateId.value())
                .map(templateEntityMapper::toDomain);
    }

    @Override
    public Page<ContractTemplate> findByOwnerId(
            UserId ownerId,
            Pageable pageable
    ) {
        Page<TemplateEntity> entities = templateJpaRepository.findByOwnerId(ownerId.value(), pageable);
        return entities.map(templateEntityMapper::toDomain);
    }

    @Override
    public Page<ContractTemplate> findByOwnerIdAndStatus(
            UserId ownerId,
            TemplateStatus status,
            Pageable pageable
    ) {
        Page<TemplateEntity> entities = templateJpaRepository.findByOwnerIdAndStatus(ownerId.value(), status, pageable);
        return entities.map(templateEntityMapper::toDomain);
    }

    @Override
    public List<ContractTemplate> findActiveTemplatesByOwnerId(UserId ownerId) {
        List<TemplateEntity> entities = templateJpaRepository.findActiveTemplatesByOwnerId(ownerId.value());
        return entities.stream()
                .map(templateEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByOwnerIdAndTitle(
            UserId ownerId,
            String title
    ) {
        return templateJpaRepository.existsByOwnerIdAndTitle(ownerId.value(), title);
    }

    @Override
    public void delete(ContractTemplate template) {
        templateJpaRepository.deleteById(template.getTemplateId().value());
    }

    @Override
    public long countByOwnerId(UserId ownerId) {
        return templateJpaRepository.countByOwnerId(ownerId.value());
    }
}