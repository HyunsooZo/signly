package com.deally.contract.application.mapper;

import com.deally.contract.application.dto.SignatureResponse;
import com.deally.contract.domain.model.Signature;
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