package com.signly.document.domain.model;

public enum DocumentType {
    CONTRACT_PDF("계약서 PDF"),
    SIGNATURE_IMAGE("서명 이미지"),
    ATTACHMENT("첨부파일");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}