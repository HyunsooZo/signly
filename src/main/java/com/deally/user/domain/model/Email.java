package com.deally.user.domain.model;

import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 null이거나 빈 값일 수 없습니다");
        }

        String trimmedValue = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmedValue).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다");
        }

        this.value = trimmedValue;
    }

    public static Email of(String value) {
        return new Email(value);
    }

}