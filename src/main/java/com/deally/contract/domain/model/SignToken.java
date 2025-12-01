package com.deally.contract.domain.model;

import com.deally.common.exception.ValidationException;
import com.deally.common.util.UlidGenerator;

public record SignToken(String value) {

    public static SignToken of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("서명 토큰은 필수입니다");
        }
        return new SignToken(value.trim());
    }

    public static SignToken generate() {
        return new SignToken(UlidGenerator.generate());
    }

}
