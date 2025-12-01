package com.deally.template.domain.model;

import java.util.HashMap;
import java.util.Map;

public record TemplateMetadata(
        String title,
        String description,
        String createdBy,
        Map<String, TemplateVariable> variables
) {

    public TemplateMetadata(
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
