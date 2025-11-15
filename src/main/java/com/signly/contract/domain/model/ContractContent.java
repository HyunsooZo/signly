package com.signly.contract.domain.model;

import com.signly.common.exception.ValidationException;

public record ContractContent(String content) {

    public ContractContent {
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("계약서 내용은 필수입니다");
        }
        if (content.length() > 50000) {
            throw new ValidationException("계약서 내용은 50,000자를 초과할 수 없습니다");
        }
    }

    public static ContractContent of(String content) {
        if (content == null) {
            throw new ValidationException("계약서 내용은 필수입니다");
        }
        return new ContractContent(content.trim());
    }

    public int getLength() {
        return content.length();
    }

    public boolean isEmpty() {
        return content.trim().isEmpty();
    }

    public String getValue() {
        return content;
    }

    @Override
    public String toString() {
        return content;
    }
}