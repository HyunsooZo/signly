package com.deally.signature.domain.model;

import com.deally.common.util.UlidGenerator;

public record SignatureId(String value) {
    public static SignatureId of(String value) {
        return new SignatureId(value);
    }

    public static SignatureId generate() {
        return new SignatureId(UlidGenerator.generate());
    }
}