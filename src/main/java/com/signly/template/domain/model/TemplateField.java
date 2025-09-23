package com.signly.template.domain.model;

import com.signly.common.exception.ValidationException;

import java.util.Objects;

public class TemplateField {

    private final String fieldName;
    private final String fieldType;
    private final boolean required;
    private final String placeholder;

    private TemplateField(String fieldName, String fieldType, boolean required, String placeholder) {
        validateFieldName(fieldName);
        validateFieldType(fieldType);

        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.required = required;
        this.placeholder = placeholder;
    }

    public static TemplateField of(String fieldName, String fieldType, boolean required, String placeholder) {
        return new TemplateField(fieldName, fieldType, required, placeholder);
    }

    private void validateFieldName(String fieldName) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            throw new ValidationException("필드 이름은 필수입니다");
        }
        if (fieldName.length() > 100) {
            throw new ValidationException("필드 이름은 100자를 초과할 수 없습니다");
        }
    }

    private void validateFieldType(String fieldType) {
        if (fieldType == null || fieldType.trim().isEmpty()) {
            throw new ValidationException("필드 타입은 필수입니다");
        }

        String[] validTypes = {"text", "number", "date", "email", "signature"};
        boolean isValidType = false;
        for (String validType : validTypes) {
            if (validType.equals(fieldType.toLowerCase())) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            throw new ValidationException("지원되지 않는 필드 타입입니다: " + fieldType);
        }
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public boolean isRequired() {
        return required;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateField that = (TemplateField) o;
        return required == that.required &&
               Objects.equals(fieldName, that.fieldName) &&
               Objects.equals(fieldType, that.fieldType) &&
               Objects.equals(placeholder, that.placeholder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, fieldType, required, placeholder);
    }
}