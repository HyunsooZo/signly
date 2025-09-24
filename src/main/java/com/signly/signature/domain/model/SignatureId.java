package com.signly.signature.domain.model;

import com.signly.common.exception.ValidationException;

public class SignatureId {
    private final String value;

    private SignatureId(String value) {
        this.value = value;
    }

    public static SignatureId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("서명 ID는 필수입니다");
        }
        return new SignatureId(value);
    }

    public static SignatureId generate() {
        return new SignatureId(java.util.UUID.randomUUID().toString());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignatureId that = (SignatureId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}