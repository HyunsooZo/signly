package com.signly.template.application.mapper;

import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.domain.model.ContractTemplate;
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