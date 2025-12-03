package com.signly.notification.domain.model;

import com.signly.common.util.UlidGenerator;

public record EmailOutboxId(String value) {

    public EmailOutboxId(String value) {
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
}
