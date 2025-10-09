package com.signly.template.domain.model;

import com.signly.common.util.UlidGenerator;

import java.util.Objects;

public class TemplateId {

    private final String value;

    private TemplateId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("템플릿 ID는 null이거나 빈 값일 수 없습니다");
        }
        if (!UlidGenerator.isValid(value)) {
            throw new IllegalArgumentException("유효하지 않은 템플릿 ID 형식입니다");
        }
        this.value = value;
    }

    public static TemplateId of(String value) {
        return new TemplateId(value);
    }

    public static TemplateId generate() {
        return new TemplateId(UlidGenerator.generate());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateId that = (TemplateId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}