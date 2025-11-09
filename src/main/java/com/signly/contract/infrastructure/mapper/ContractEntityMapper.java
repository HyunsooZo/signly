package com.signly.contract.infrastructure.mapper;

import com.signly.contract.domain.model.*;
import com.signly.template.domain.model.TemplateId;
import com.signly.user.domain.model.UserId;
import com.signly.contract.infrastructure.entity.ContractJpaEntity;
import com.signly.contract.infrastructure.entity.SignatureJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContractEntityMapper {

    public ContractJpaEntity toEntity(Contract contract) {
        ContractJpaEntity entity = new ContractJpaEntity(
            contract.getId().getValue(),
            contract.getCreatorId().getValue(),
            contract.getTemplateId() != null ? contract.getTemplateId().getValue() : null,
            contract.getTitle(),
            contract.getContent().getValue(),
            null,
            contract.getFirstParty().getName(),
            contract.getFirstParty().getEmail(),
            contract.getFirstParty().getOrganizationName(),
            contract.getSecondParty().getName(),
            contract.getSecondParty().getEmail(),
            contract.getSecondParty().getOrganizationName(),
            contract.getStatus(),
            contract.getSignToken().value(),
            contract.getExpiresAt(),
            contract.getPresetType()
        );

        entity.setPdfPath(contract.getPdfPath());

        List<SignatureJpaEntity> signatureEntities = contract.getSignatures().stream()
            .map(this::toSignatureEntity)
            .collect(Collectors.toList());

        signatureEntities.forEach(entity::addSignature);

        return entity;
    }

    public Contract toDomain(ContractJpaEntity entity) {
        UserId creatorId = UserId.of(entity.getCreatorId());
        TemplateId templateId = entity.getTemplateId() != null ? TemplateId.of(entity.getTemplateId()) : null;
        ContractId contractId = ContractId.of(entity.getId());
        ContractContent content = ContractContent.of(entity.getContent());

        PartyInfo firstParty = PartyInfo.of(
            entity.getFirstPartyName(),
            entity.getFirstPartyEmail(),
            entity.getFirstPartyOrganization()
        );

        PartyInfo secondParty = PartyInfo.of(
            entity.getSecondPartyName(),
            entity.getSecondPartyEmail(),
            entity.getSecondPartyOrganization()
        );

        List<Signature> signatures = entity.getSignatures().stream()
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

    private SignatureJpaEntity toSignatureEntity(Signature signature) {
        return new SignatureJpaEntity(
            signature.getSignerEmail(),
            signature.getSignerName(),
            signature.getSignedAt(),
            signature.getSignatureData(),
            signature.getIpAddress()
        );
    }

    private Signature toDomainSignature(SignatureJpaEntity entity) {
        return Signature.create(
            entity.getSignerEmail(),
            entity.getSignerName(),
            entity.getSignatureData(),
            entity.getIpAddress()
        );
    }

}