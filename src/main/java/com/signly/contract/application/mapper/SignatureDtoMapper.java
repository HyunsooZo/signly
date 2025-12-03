package com.signly.contract.application.mapper;

import com.signly.contract.application.dto.SignatureResponse;
import com.signly.contract.domain.model.Signature;
import org.springframework.stereotype.Component;

@Component
public class SignatureDtoMapper {

    public SignatureResponse toResponse(Signature signature) {
        return new SignatureResponse(
                signature.signerEmail(),
                signature.signerName(),
                signature.signedAt(),
                signature.ipAddress()
        );
    }
}