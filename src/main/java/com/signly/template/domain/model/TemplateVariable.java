package com.signly.template.domain.model;

import com.signly.common.exception.ValidationException;

public record TemplateVariable(
        String label,
        TemplateVariableType type,
        boolean required,
        String defaultValue
) {

    public static TemplateVariable of(
            String label,
            TemplateVariableType type,
            boolean required,
            String defaultValue
    ) {
        if (label == null || label.trim().isEmpty()) {
            throw new ValidationException("변수 레이블은 필수입니다");
        }
        if (type == null) {
            throw new ValidationException("변수 타입은 필수입니다");
        }
        return new TemplateVariable(label, type, required, defaultValue);
    }

    public void validateValue(String value) {
        if (required && (value == null || value.trim().isEmpty())) {
            throw new ValidationException(label + "은(는) 필수 항목입니다");
        }

        if (value != null && !value.trim().isEmpty()) {
            switch (type) {
                case NUMBER -> {
                    try {
                        Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        throw new ValidationException(label + "은(는) 숫자여야 합니다");
                    }
                }
                case EMAIL -> {
                    if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                        throw new ValidationException(label + "은(는) 유효한 이메일 형식이어야 합니다");
                    }
                }
                case DATE -> {
                    if (!value.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                        throw new ValidationException(label + "은(는) YYYY-MM-DD 형식이어야 합니다");
                    }
                }
            }
        }
    }
}
