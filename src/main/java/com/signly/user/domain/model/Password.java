package com.signly.user.domain.model;

import com.signly.common.exception.ValidationException;

import java.util.Objects;
import java.util.regex.Pattern;

public class Password {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$"
    );

    private final String value;

    private Password(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("비밀번호는 null이거나 빈 값일 수 없습니다");
        }

        if (!PASSWORD_PATTERN.matcher(value).matches()) {
            throw new ValidationException("비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 포함해야 합니다");
        }

        this.value = value;
    }

    public static Password of(String value) {
        return new Password(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(value, password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "****";
    }
}