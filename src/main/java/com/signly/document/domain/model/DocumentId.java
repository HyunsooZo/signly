package com.signly.document.domain.model;

import com.signly.common.util.UlidGenerator;

public record DocumentId(String value) {

    public DocumentId {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Document ID cannot be null or empty");
        }
    }

    public static DocumentId generate() {
        return new DocumentId(UlidGenerator.generate());
    }

    public static DocumentId of(String value) {
        return new DocumentId(value);
    }
}