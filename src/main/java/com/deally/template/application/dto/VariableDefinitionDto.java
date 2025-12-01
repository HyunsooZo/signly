package com.deally.template.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.deally.template.domain.model.VariableCategory;
import com.deally.template.domain.model.VariableType;

/**
 * 템플릿 변수 정의 DTO
 */
public record VariableDefinitionDto(
        Long id,
        @JsonProperty("name")  // JavaScript에서 v.name으로 접근
        String variableName,
        String displayName,
        VariableCategory category,
        @JsonProperty("type")  // JavaScript에서 v.type으로 접근
        VariableType variableType,
        String description,
        String iconClass,
        Integer inputSize,
        Integer maxLength,
        String placeholderExample,
        @JsonProperty("required")  // JavaScript에서 v.required로 접근
        Boolean isRequired,
        String validationRule,
        String validationMessage,
        String defaultValue,
        Integer displayOrder,
        Boolean isActive,
        String htmlInputType
) {

    /**
     * 엔티티를 DTO로 변환
     */
    public static VariableDefinitionDto from(com.deally.template.domain.model.TemplateVariableDefinition entity) {
        return new VariableDefinitionDto(
                entity.getId(),
                entity.getVariableName(),
                entity.getDisplayName(),
                entity.getCategory(),
                entity.getVariableType(),
                entity.getDescription(),
                entity.getIconClass(),
                entity.getInputSize(),
                entity.getMaxLength(),
                entity.getPlaceholderExample(),
                entity.getIsRequired(),
                entity.getValidationRule(),
                entity.getValidationMessage(),
                entity.getDefaultValue(),
                entity.getDisplayOrder(),
                entity.getIsActive(),
                entity.getHtmlInputType()
        );
    }
}