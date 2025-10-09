package com.signly.user.domain.model;

import com.signly.common.util.UlidGenerator;

import java.util.Objects;

public class UserId {

    private final String value;

    private UserId(String value) {
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

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
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