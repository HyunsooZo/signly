package com.signly.domain.template.model;

import com.signly.common.exception.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

public class TemplateContent {

    private final String jsonContent;

    private TemplateContent(String jsonContent) {
        if (jsonContent == null || jsonContent.trim().isEmpty()) {
            throw new ValidationException("템플릿 내용은 null이거나 빈 값일 수 없습니다");
        }

        validateJsonFormat(jsonContent);
        this.jsonContent = jsonContent.trim();
    }

    public static TemplateContent of(String jsonContent) {
        return new TemplateContent(jsonContent);
    }

    private void validateJsonFormat(String content) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(content);
        } catch (JsonProcessingException e) {
            throw new ValidationException("유효하지 않은 JSON 형식입니다");
        }
    }

    public String getJsonContent() {
        return jsonContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateContent that = (TemplateContent) o;
        return Objects.equals(jsonContent, that.jsonContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonContent);
    }

    @Override
    public String toString() {
        return jsonContent;
    }
}