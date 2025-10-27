package com.signly.template.application.mapper;

import com.signly.template.application.dto.TemplateResponse;
import com.signly.template.application.dto.TemplateSectionDto;
import com.signly.template.application.dto.TemplateVariableDto;
import com.signly.template.domain.model.ContractTemplate;
import com.signly.template.domain.model.TemplateContent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TemplateDtoMapper {

    public TemplateResponse toResponse(ContractTemplate template) {
        TemplateContent content = template.getContent();
        List<TemplateSectionDto> sections = content.getSections().stream()
                .map(TemplateSectionDto::from)
                .collect(Collectors.toList());

        Map<String, TemplateVariableDto> variables = content.getMetadata()
                .getVariables()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> TemplateVariableDto.from(entry.getKey(), entry.getValue())
                ));

        return new TemplateResponse(
                template.getTemplateId().getValue(),
                template.getOwnerId().getValue(),
                template.getTitle(),
                content.getJsonContent(),
                template.getVersion(),
                template.getStatus(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                sections,
                content.renderHtml(),
                content.toPlainText(),
                variables
        );
    }
}
