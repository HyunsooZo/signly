package com.deally.contract.infrastructure.mapper;

import com.deally.common.util.UlidGenerator;
import com.deally.contract.domain.model.Signature;
import com.deally.contract.infrastructure.entity.SignatureEntity;
import org.springframework.stereotype.Component;

@Component
public class SignatureEntityMapper {

    public SignatureEntity toEntity(Signature signature) {
        return new SignatureEntity(
                UlidGenerator.generate(),
                null, // contract will be set later
                signature.signerEmail(),
                signature.signerName(),
                signature.signatureData(),
                signature.signedAt(),
                signature.ipAddress(),
                signature.deviceInfo(),
                signature.signaturePath()
        );
    }

    public Signature toDomain(SignatureEntity entity) {
        return Signature.create(
                entity.getSignerEmail(),
                entity.getSignerName(),
                entity.getSignatureData(),
                entity.getIpAddress(),
                entity.getDeviceInfo(),
                entity.getSignaturePath()
        );
    }
}
