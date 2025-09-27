package com.signly.template.application.dto;

import com.signly.template.domain.model.TemplateSection;
import com.signly.template.domain.model.TemplateSectionType;

public record TemplateSectionDto(
        String sectionId,
        TemplateSectionType type,
        int order,
        String content
) {
    public static TemplateSectionDto from(TemplateSection section) {
        return new TemplateSectionDto(
                section.getSectionId(),
                section.getType(),
                section.getOrder(),
                section.getContent()
        );
    }
}
