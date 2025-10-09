package com.signly.contract.domain.model;

import com.signly.common.exception.ValidationException;
import com.signly.common.util.UlidGenerator;

import java.util.Objects;

public class ContractId {
    private final String value;

    private ContractId(String value) {
        this.value = value;
    }

    public static ContractId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("계약서 ID는 필수입니다");
        }
        return new ContractId(value);
    }

    public static ContractId generate() {
        return new ContractId(UlidGenerator.generate());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractId that = (ContractId) o;
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