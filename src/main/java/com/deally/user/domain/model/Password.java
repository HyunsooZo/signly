package com.deally.user.domain.model;

import com.deally.common.exception.ValidationException;
import com.deally.common.util.PasswordValidator;

public record Password(String value) {

    public Password(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("비밀번호는 null이거나 빈 값일 수 없습니다");
        }

        if (!PasswordValidator.isValid(value)) {
            throw new ValidationException(PasswordValidator.PASSWORD_REQUIREMENT_MESSAGE);
        }

        this.value = value;
    }

    public static Password of(String value) {
        return new Password(value);
    }

    @Override
    public String toString() {
        return "****";
    }

}