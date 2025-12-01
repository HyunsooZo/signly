package com.deally.template.application.mapper;

import com.deally.template.application.dto.TemplateResponse;
import com.deally.template.application.dto.TemplateSectionDto;
import com.deally.template.application.dto.TemplateVariableDto;
import com.deally.template.domain.model.ContractTemplate;
import com.deally.template.domain.model.TemplateContent;
import com.deally.template.domain.service.UnifiedTemplateRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TemplateDtoMapper {

    private final UnifiedTemplateRenderer unifiedTemplateRenderer;

    public TemplateResponse toResponse(ContractTemplate template) {
        TemplateContent content = template.getContent();
        List<TemplateSectionDto> sections = content.sections().stream()
                .map(TemplateSectionDto::from)
                .collect(Collectors.toList());

        Map<String, TemplateVariableDto> variables = content.metadata()
                .variables()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> TemplateVariableDto.from(entry.getKey(), entry.getValue())
                ));

        return new TemplateResponse(
                template.getTemplateId().value(),
                template.getOwnerId().value(),
                template.getTitle(),
                content.jsonContent(),
                template.getVersion(),
                template.getStatus(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                sections,
                unifiedTemplateRenderer.renderPreview(content),
                unifiedTemplateRenderer.renderPlainText(content),
                variables
        );
    }
}
