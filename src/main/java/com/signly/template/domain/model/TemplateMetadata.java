package com.signly.template.domain.model;

import java.util.HashMap;
import java.util.Map;

public class TemplateMetadata {

    private final String title;
    private final String description;
    private final String createdBy;
    private final Map<String, TemplateVariable> variables;

    private TemplateMetadata(String title,
                             String description,
                             String createdBy,
                             Map<String, TemplateVariable> variables) {
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.variables = variables != null ? new HashMap<>(variables) : new HashMap<>();
    }

    public static TemplateMetadata of(String title,
                                      String description,
                                      String createdBy,
                                      Map<String, TemplateVariable> variables) {
        return new TemplateMetadata(title, description, createdBy, variables);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedBy() {
        return createdBy;
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
