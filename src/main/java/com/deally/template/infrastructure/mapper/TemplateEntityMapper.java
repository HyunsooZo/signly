package com.deally.template.infrastructure.mapper;

import com.deally.template.domain.model.ContractTemplate;
import com.deally.template.domain.model.TemplateContent;
import com.deally.template.domain.model.TemplateId;
import com.deally.template.infrastructure.entity.TemplateEntity;
import com.deally.user.domain.model.UserId;
import org.springframework.stereotype.Component;

@Component
public class TemplateEntityMapper {

    public TemplateEntity toEntity(ContractTemplate template) {
        return new TemplateEntity(
                template.getTemplateId().value(),
                template.getOwnerId().value(),
                template.getTitle(),
                template.getContent().jsonContent(),
                template.getVersion(),
                template.getStatus(),
                false, // isPreset - 사용자 템플릿은 프리셋이 아님
                null,  // presetId
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    public ContractTemplate toDomain(TemplateEntity entity) {
        // 프리셋은 도메인 객체로 변환하지 않음 (프리셋은 읽기 전용)
        if (entity.isPreset()) {
            throw new IllegalStateException("Preset templates cannot be converted to domain objects");
        }

        return ContractTemplate.restore(
                TemplateId.of(entity.getTemplateId()),
                UserId.of(entity.getOwnerId()),
                entity.getTitle(),
                TemplateContent.fromJson(entity.getContent()),
                entity.getVersion(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void updateEntity(
            TemplateEntity entity,
            ContractTemplate template
    ) {
        entity.setTitle(template.getTitle());
        entity.setContent(template.getContent().jsonContent());
        entity.setVersion(template.getVersion());
        entity.setStatus(template.getStatus());
        entity.setUpdatedAt(template.getUpdatedAt());
    }
}