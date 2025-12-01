package com.deally.contract.domain.model;

import com.deally.common.exception.ValidationException;
import com.deally.common.util.UlidGenerator;

public record ContractId(String value) {

    public static ContractId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("계약서 ID는 필수입니다");
        }
        return new ContractId(value);
    }

    public static ContractId generate() {
        return new ContractId(UlidGenerator.generate());
    }
}