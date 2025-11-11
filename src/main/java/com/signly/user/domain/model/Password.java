package com.signly.user.domain.model;

import com.signly.common.exception.ValidationException;

import java.util.regex.Pattern;

public record Password(String value) {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$");

    public Password(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("비밀번호는 null이거나 빈 값일 수 없습니다");
        }

        if (!PASSWORD_PATTERN.matcher(value).matches()) {
            throw new ValidationException("비밀번호는 8자 이상이며 대소문자, 숫자, 특수문자를 포함해야 합니다");
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