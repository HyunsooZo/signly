package com.signly.signature.infrastructure.persistence.mapper;

import com.signly.contract.domain.model.ContractId;
import com.signly.signature.domain.model.ContractSignature;
import com.signly.signature.domain.model.SignatureData;
import com.signly.signature.domain.model.SignatureId;
import com.signly.signature.domain.model.SignerInfo;
import com.signly.signature.infrastructure.persistence.entity.SignatureEntity;
import org.springframework.stereotype.Component;

@Component
public class SignatureEntityMapper {

    public SignatureEntity toEntity(ContractSignature signature) {
        return new SignatureEntity(
                signature.id().value(),
                signature.contractId().value(),
                signature.signerInfo().signerEmail(),
                signature.signerInfo().signerName(),
                signature.signatureData().value(),
                signature.signerInfo().signedAt(),
                signature.signerInfo().ipAddress(),
                signature.signerInfo().deviceInfo()
        );
    }

    public ContractSignature toDomain(SignatureEntity entity) {
        return ContractSignature.create(
                ContractId.of(entity.getContractId()),
                entity.getSignatureData(),
                entity.getSignerEmail(),
                entity.getSignerName(),
                entity.getIpAddress(),
                entity.getDeviceInfo()
        );
    }
}