package com.signly.template.application.dto;

import com.signly.template.domain.model.TemplateVariable;
import com.signly.template.domain.model.TemplateVariableType;
import lombok.Getter;

@Getter
public class TemplateVariableDto {
    private final String name;
    private final String label;
    private final TemplateVariableType type;
    private final boolean required;
    private final String defaultValue;

    public TemplateVariableDto(
        String name,
        String label,
        TemplateVariableType type,
        boolean required,
        String defaultValue
    ) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public static TemplateVariableDto from(
        String name,
        TemplateVariable variable
    ) {
        return new TemplateVariableDto(
            name,
            variable.getLabel(),
            variable.getType(),
            variable.isRequired(),
            variable.getDefaultValue()
        );
    }
}
