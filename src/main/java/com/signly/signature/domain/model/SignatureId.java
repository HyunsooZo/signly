package com.signly.signature.domain.model;

import com.signly.common.util.UlidGenerator;

import java.util.Objects;

public class SignatureId {
    private final String value;

    private SignatureId(String value) {
        this.value = value;
    }

    public static SignatureId of(String value) {
        return new SignatureId(value);
    }

    public static SignatureId generate() {
        return new SignatureId(UlidGenerator.generate());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignatureId that = (SignatureId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "SignatureId{" + value + '}';
    }
}