package com.signly.contract.domain.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 계약서 PDF 생성에 필요한 데이터를 담는 Value Object
 * DDD 원칙: 불변 객체, 관련 데이터 응집
 * SRP: PDF 생성에 필요한 데이터만 포함
 */
public class ContractPdfData {
    private final ContractId contractId;
    private final String title;
    private final String htmlContent;
    private final String firstPartySignatureImage;
    private final String secondPartySignatureImage;
    private final PresetType presetType;
    private final Map<String, Object> additionalData;

    private ContractPdfData(Builder builder) {
        this.contractId = Objects.requireNonNull(builder.contractId, "계약서 ID는 필수입니다");
        this.title = Objects.requireNonNull(builder.title, "제목은 필수입니다");
        this.htmlContent = Objects.requireNonNull(builder.htmlContent, "HTML 내용은 필수입니다");
        this.firstPartySignatureImage = builder.firstPartySignatureImage;
        this.secondPartySignatureImage = builder.secondPartySignatureImage;
        this.presetType = builder.presetType;
        this.additionalData = builder.additionalData != null
            ? new HashMap<>(builder.additionalData)
            : new HashMap<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public ContractId getContractId() {
        return contractId;
    }

    public String getTitle() {
        return title;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public String getFirstPartySignatureImage() {
        return firstPartySignatureImage;
    }

    public String getSecondPartySignatureImage() {
        return secondPartySignatureImage;
    }

    public PresetType getPresetType() {
        return presetType;
    }

    public Map<String, Object> getAdditionalData() {
        return new HashMap<>(additionalData);
    }

    /**
     * PDF 파일명 생성 (비즈니스 규칙)
     */
    public String generateFileName() {
        String sanitizedTitle = title.replaceAll("[^a-zA-Z0-9가-힣\\s-]", "")
                                     .replaceAll("\\s+", "_");
        return String.format("%s_%s.pdf", sanitizedTitle, contractId.getValue());
    }

    /**
     * 양측 서명이 모두 존재하는지 확인 (비즈니스 규칙)
     */
    public boolean hasBothSignatures() {
        return firstPartySignatureImage != null && !firstPartySignatureImage.isBlank()
            && secondPartySignatureImage != null && !secondPartySignatureImage.isBlank();
    }

    public static class Builder {
        private ContractId contractId;
        private String title;
        private String htmlContent;
        private String firstPartySignatureImage;
        private String secondPartySignatureImage;
        private PresetType presetType;
        private Map<String, Object> additionalData;

        public Builder contractId(ContractId contractId) {
            this.contractId = contractId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder htmlContent(String htmlContent) {
            this.htmlContent = htmlContent;
            return this;
        }

        public Builder firstPartySignatureImage(String firstPartySignatureImage) {
            this.firstPartySignatureImage = firstPartySignatureImage;
            return this;
        }

        public Builder secondPartySignatureImage(String secondPartySignatureImage) {
            this.secondPartySignatureImage = secondPartySignatureImage;
            return this;
        }

        public Builder presetType(PresetType presetType) {
            this.presetType = presetType;
            return this;
        }

        public Builder additionalData(Map<String, Object> additionalData) {
            this.additionalData = additionalData;
            return this;
        }

        public ContractPdfData build() {
            return new ContractPdfData(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractPdfData that = (ContractPdfData) o;
        return Objects.equals(contractId, that.contractId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractId);
    }

    @Override
    public String toString() {
        return "ContractPdfData{" +
                "contractId=" + contractId +
                ", title='" + title + '\'' +
                ", hasBothSignatures=" + hasBothSignatures() +
                '}';
    }
}
