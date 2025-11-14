package com.signly.contract.infrastructure.mapper;

import com.signly.contract.domain.model.*;
import com.signly.template.domain.model.TemplateId;
import com.signly.user.domain.model.UserId;
import com.signly.common.util.UlidGenerator;
import com.signly.contract.infrastructure.entity.ContractJpaEntity;
import com.signly.contract.infrastructure.entity.SignatureEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContractEntityMapper {

    public ContractJpaEntity toEntity(Contract contract) {
        ContractJpaEntity entity = new ContractJpaEntity(
            contract.getId().value(),
            contract.getCreatorId().value(),
            contract.getTemplateId() != null ? contract.getTemplateId().value() : null,
            contract.getTitle(),
            contract.getContent().content(),
            null,
            contract.getFirstParty().name(),
            contract.getFirstParty().email(),
            contract.getFirstParty().organizationName(),
            contract.getSecondParty().name(),
            contract.getSecondParty().email(),
            contract.getSecondParty().organizationName(),
            contract.getStatus(),
            contract.getSignToken().value(),
            contract.getExpiresAt(),
            contract.getPresetType()
        );

        entity.setPdfPath(contract.getPdfPath());

        var signatureEntities = contract.getSignatures().stream()
            .map(this::toSignatureEntity)
            .toList();

        signatureEntities.forEach(entity::addSignature);

        return entity;
    }

    public Contract toDomain(ContractJpaEntity entity) {
        var creatorId = UserId.of(entity.getCreatorId());
        var templateId = entity.getTemplateId() != null ? TemplateId.of(entity.getTemplateId()) : null;
        var contractId = ContractId.of(entity.getId());
        var content = ContractContent.of(entity.getContent());

        var firstParty = PartyInfo.of(
            entity.getFirstPartyName(),
            entity.getFirstPartyEmail(),
            entity.getFirstPartyOrganization()
        );

        var secondParty = PartyInfo.of(
            entity.getSecondPartyName(),
            entity.getSecondPartyEmail(),
            entity.getSecondPartyOrganization()
        );

        var signatures = entity.getSignatures().stream()
            .map(this::toDomainSignature)
            .collect(Collectors.toList());

        SignToken signToken = SignToken.of(entity.getSignToken());

        return Contract.restore(
            contractId,
            creatorId,
            templateId,
            entity.getTitle(),
            content,
            firstParty,
            secondParty,
            entity.getStatus(),
            signatures,
            signToken,
            entity.getExpiresAt(),
            entity.getPresetType(),
            entity.getPdfPath(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private SignatureEntity toSignatureEntity(Signature signature) {
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

    private Signature toDomainSignature(SignatureEntity entity) {
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