package com.signly.contract.domain.model;

import com.signly.common.exception.ValidationException;

import java.util.Objects;

public class ContractContent {
    private final String content;

    private ContractContent(String content) {
        this.content = content;
    }

    public static ContractContent of(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("계약서 내용은 필수입니다");
        }
        if (content.length() > 50000) {
            throw new ValidationException("계약서 내용은 50,000자를 초과할 수 없습니다");
        }
        return new ContractContent(content.trim());
    }

    public String getValue() {
        return content;
    }

    public int getLength() {
        return content.length();
    }

    public boolean isEmpty() {
        return content.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractContent that = (ContractContent) o;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public String toString() {
        return content;
    }
}