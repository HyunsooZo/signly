package com.signly.signature.domain.model;

import com.signly.common.domain.AggregateRoot;
import com.signly.contract.domain.model.ContractId;

public class ContractSignature extends AggregateRoot {
    private final SignatureId id;
    private final ContractId contractId;
    private final SignatureData signatureData;
    private final SignerInfo signerInfo;
    private final String signaturePath;

    private ContractSignature(SignatureId id, ContractId contractId,
                             SignatureData signatureData, SignerInfo signerInfo, String signaturePath) {
        this.id = id;
        this.contractId = contractId;
        this.signatureData = signatureData;
        this.signerInfo = signerInfo;
        this.signaturePath = signaturePath;
    }

    public static ContractSignature create(ContractId contractId, String signatureDataValue,
                                          String signerEmail, String signerName,
                                          String ipAddress, String deviceInfo) {
        return create(contractId, signatureDataValue, signerEmail, signerName, ipAddress, deviceInfo, null);
    }

    public static ContractSignature create(ContractId contractId, String signatureDataValue,
                                          String signerEmail, String signerName,
                                          String ipAddress, String deviceInfo,
                                          String signaturePath) {
        SignatureId id = SignatureId.generate();
        SignatureData data = SignatureData.of(signatureDataValue);
        SignerInfo info = SignerInfo.create(signerEmail, signerName, ipAddress, deviceInfo);

        return new ContractSignature(id, contractId, data, info, signaturePath);
    }

    public static ContractSignature restore(SignatureId id, ContractId contractId,
                                          SignatureData signatureData, SignerInfo signerInfo,
                                          String signaturePath) {
        return new ContractSignature(id, contractId, signatureData, signerInfo, signaturePath);
    }

    public boolean validate() {
        return !signatureData.isEmpty() &&
               signerInfo.signerEmail() != null &&
               signerInfo.signerName() != null &&
               signerInfo.ipAddress() != null;
    }

    public boolean verifyIntegrity() {
        return validate() && signatureData != null && !signatureData.isEmpty();
    }

    public SignatureId id() {
        return id;
    }

    public ContractId contractId() {
        return contractId;
    }

    public SignatureData signatureData() {
        return signatureData;
    }

    public SignerInfo signerInfo() {
        return signerInfo;
    }

    public String signaturePath() {
        return signaturePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContractSignature that = (ContractSignature) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ContractSignature{" +
                "id=" + id +
                ", contractId=" + contractId +
                ", signerEmail='" + signerInfo.signerEmail() + '\'' +
                ", signedAt=" + signerInfo.signedAt() +
                '}';
    }
}
