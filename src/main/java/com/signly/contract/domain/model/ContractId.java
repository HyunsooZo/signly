package com.signly.contract.domain.model;

import com.signly.common.exception.ValidationException;
import com.signly.common.util.UlidGenerator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

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