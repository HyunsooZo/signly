package com.signly.notification.domain.model;

import com.signly.common.util.UlidGenerator;

import java.util.Objects;

public class EmailOutboxId {
    private final String value;

    private EmailOutboxId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("EmailOutboxId cannot be null or blank");
        }
        this.value = value;
    }

    public static EmailOutboxId generate() {
        return new EmailOutboxId(UlidGenerator.generate());
    }

    public static EmailOutboxId of(String value) {
        return new EmailOutboxId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailOutboxId that = (EmailOutboxId) o;
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
