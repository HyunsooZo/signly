package com.signly.template.domain.model;

import com.signly.common.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TemplateSection {

    private final String sectionId;
    private TemplateSectionType type;
    private int order;
    private String content;
    private final Map<String, Object> metadata;
    private final List<String> variables;

    private TemplateSection(String sectionId,
                            TemplateSectionType type,
                            int order,
                            String content,
                            Map<String, Object> metadata,
                            List<String> variables) {
        this.sectionId = sectionId;
        this.type = type;
        this.order = order;
        this.content = content == null ? "" : content;
        this.metadata = metadata;
        this.variables = variables != null ? new ArrayList<>(variables) : new ArrayList<>();
    }

    public static TemplateSection of(String sectionId,
                                     TemplateSectionType type,
                                     int order,
                                     String content,
                                     Map<String, Object> metadata,
                                     List<String> variables) {
        if (sectionId == null || sectionId.trim().isEmpty()) {
            throw new ValidationException("섹션 ID는 필수입니다");
        }
        if (type == null) {
            throw new ValidationException("섹션 타입은 필수입니다");
        }
        return new TemplateSection(sectionId, type, order, content, metadata, variables);
    }

    public String getSectionId() {
        return sectionId;
    }

    public TemplateSectionType getType() {
        return type;
    }

    public int getOrder() {
        return order;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public List<String> getVariables() {
        return new ArrayList<>(variables);
    }

    public void setType(TemplateSectionType type) {
        this.type = type;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setContent(String content) {
        this.content = content == null ? "" : content;
    }
}
