package com.signly.domain.contract.model;

public enum ContractStatus {
    DRAFT("초안"),
    PENDING("서명대기"),
    SIGNED("서명완료"),
    COMPLETED("완료"),
    CANCELLED("취소됨"),
    EXPIRED("만료됨");

    private final String description;

    ContractStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canUpdate() {
        return this == DRAFT;
    }

    public boolean canCancel() {
        return this == DRAFT || this == PENDING;
    }

    public boolean canSign() {
        return this == PENDING;
    }

    public boolean canComplete() {
        return this == SIGNED;
    }

    public boolean isActive() {
        return this == PENDING || this == SIGNED;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED || this == EXPIRED;
    }
}