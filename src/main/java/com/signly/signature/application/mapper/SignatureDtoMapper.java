package com.signly.signature.application.mapper;

import com.signly.signature.application.dto.SignatureResponse;
import com.signly.signature.domain.model.ContractSignature;
import org.springframework.stereotype.Component;

@Component
public class SignatureDtoMapper {

    public SignatureResponse toResponse(ContractSignature signature) {
        return new SignatureResponse(
                signature.id().value(),
                signature.contractId().value(),
                signature.signerInfo().signerEmail(),
                signature.signerInfo().signerName(),
                signature.signerInfo().signedAt(),
                signature.signerInfo().ipAddress(),
                signature.signerInfo().deviceInfo()
        );
    }
}