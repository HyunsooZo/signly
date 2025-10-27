package com.signly.template.domain.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class TemplateMetadata {

    @Getter
    private final String title;
    @Getter
    private final String description;
    @Getter
    private final String createdBy;
    private final Map<String, TemplateVariable> variables;

    private TemplateMetadata(
            String title,
                             String description,
                             String createdBy,
            Map<String, TemplateVariable> variables
    ) {
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.variables = variables != null ? new HashMap<>(variables) : new HashMap<>();
    }

    public static TemplateMetadata of(
            String title,
            String description,
            String createdBy,
            Map<String, TemplateVariable> variables
    ) {
        return new TemplateMetadata(title, description, createdBy, variables);
    }

    public Map<String, TemplateVariable> getVariables() {
        return new HashMap<>(variables);
    }

    public boolean hasVariable(String variableName) {
        return variables.containsKey(variableName);
    }

    public TemplateVariable getVariable(String variableName) {
        return variables.get(variableName);
    }
}
