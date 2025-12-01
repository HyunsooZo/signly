package com.deally.template.domain.repository;

import com.deally.template.domain.model.ContractTemplate;
import com.deally.template.domain.model.TemplateId;
import com.deally.template.domain.model.TemplateStatus;
import com.deally.user.domain.model.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository {

    ContractTemplate save(ContractTemplate template);

    Optional<ContractTemplate> findById(TemplateId templateId);

    Page<ContractTemplate> findByOwnerId(
            UserId ownerId,
            Pageable pageable
    );

    Page<ContractTemplate> findByOwnerIdAndStatus(
            UserId ownerId,
            TemplateStatus status,
            Pageable pageable
    );

    List<ContractTemplate> findActiveTemplatesByOwnerId(UserId ownerId);

    boolean existsByOwnerIdAndTitle(
            UserId ownerId,
            String title
    );

    void delete(ContractTemplate template);

    long countByOwnerId(UserId ownerId);
}