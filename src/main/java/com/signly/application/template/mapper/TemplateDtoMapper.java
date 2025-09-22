package com.signly.application.template.mapper;

import com.signly.application.template.dto.TemplateResponse;
import com.signly.domain.template.model.ContractTemplate;
import org.springframework.stereotype.Component;

@Component
public class TemplateDtoMapper {

    public TemplateResponse toResponse(ContractTemplate template) {
        return new TemplateResponse(
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
}