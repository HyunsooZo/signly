package com.signly.user.domain.model;

import com.signly.common.util.UlidGenerator;

public record UserId(String value) {

    public UserId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 ID는 null이거나 빈 값일 수 없습니다");
        }
        if (!UlidGenerator.isValid(value)) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID 형식입니다");
        }
        this.value = value;
    }

    public static UserId of(String value) {
        return new UserId(value);
    }

    public static UserId generate() {
        return new UserId(UlidGenerator.generate());
    }

    @Override
    public String toString() {
        return value;
    }

}