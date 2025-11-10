package com.signly.contract.infrastructure.mapper;

import com.signly.contract.domain.model.Signature;
import com.signly.contract.infrastructure.entity.SignatureEntity;
import org.springframework.stereotype.Component;

@Component
public class SignatureEntityMapper {

    public SignatureEntity toEntity(Signature signature) {
        return new SignatureEntity(
                null, // signatureId will be generated
                null, // contract will be set later
                signature.getSignerEmail(),
                signature.getSignerName(),
                signature.getSignatureData(),
                signature.getSignedAt(),
                signature.getIpAddress(),
                signature.getDeviceInfo(),
                signature.getSignaturePath()
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