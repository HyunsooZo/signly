package com.signly.contract.domain.model;

import com.signly.common.exception.ValidationException;
import com.signly.common.util.UlidGenerator;

public class SignToken {
    private final String value;

    private SignToken(String value) {
        this.value = value;
    }

    public static SignToken of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("서명 토큰은 필수입니다");
        }
        return new SignToken(value.trim());
    }

    public static SignToken generate() {
        return new SignToken(UlidGenerator.generate());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignToken signToken = (SignToken) o;
        return value.equals(signToken.value);
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