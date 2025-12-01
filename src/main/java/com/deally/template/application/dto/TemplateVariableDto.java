package com.deally.template.application.dto;

import com.deally.template.domain.model.TemplateVariable;
import com.deally.template.domain.model.TemplateVariableType;
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
                variable.label(),
                variable.type(),
                variable.required(),
                variable.defaultValue()
        );
    }
}
