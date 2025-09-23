package com.signly.template.infrastructure.mapper;

import com.signly.template.domain.model.*;
import com.signly.user.domain.model.UserId;
import com.signly.template.infrastructure.TemplateEntity;
import org.springframework.stereotype.Component;

@Component
public class TemplateEntityMapper {

    public TemplateEntity toEntity(ContractTemplate template) {
        return new TemplateEntity(
                template.getTemplateId().getValue(),
                template.getOwnerId().getValue(),
                template.getTitle(),
                template.getContent().getJsonContent(),
                template.getVersion(),
                template.getStatus(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    public ContractTemplate toDomain(TemplateEntity entity) {
        return ContractTemplate.restore(
                TemplateId.of(entity.getTemplateId()),
                UserId.of(entity.getOwnerId()),
                entity.getTitle(),
                TemplateContent.of(entity.getContent()),
                entity.getVersion(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void updateEntity(TemplateEntity entity, ContractTemplate template) {
        entity.setTitle(template.getTitle());
        entity.setContent(template.getContent().getJsonContent());
        entity.setVersion(template.getVersion());
        entity.setStatus(template.getStatus());
        entity.setUpdatedAt(template.getUpdatedAt());
    }
}