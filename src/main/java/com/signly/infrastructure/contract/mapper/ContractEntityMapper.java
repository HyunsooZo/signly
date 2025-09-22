package com.signly.infrastructure.contract.mapper;

import com.signly.domain.contract.model.*;
import com.signly.domain.template.model.TemplateId;
import com.signly.domain.user.model.UserId;
import com.signly.infrastructure.contract.entity.ContractJpaEntity;
import com.signly.infrastructure.contract.entity.SignatureJpaEntity;
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
            contract.getFirstParty().getName(),
            contract.getFirstParty().getEmail(),
            contract.getFirstParty().getOrganizationName(),
            contract.getSecondParty().getName(),
            contract.getSecondParty().getEmail(),
            contract.getSecondParty().getOrganizationName(),
            contract.getStatus(),
            contract.getExpiresAt()
        );

        entity.setCreatedAt(contract.getCreatedAt());
        entity.setUpdatedAt(contract.getUpdatedAt());

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

        Contract contract = Contract.create(
            creatorId,
            templateId,
            entity.getTitle(),
            content,
            firstParty,
            secondParty,
            entity.getExpiresAt()
        );

        setPrivateFields(contract, contractId, entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());

        entity.getSignatures().forEach(signatureEntity -> {
            Signature signature = toDomainSignature(signatureEntity);
            addSignatureToContract(contract, signature);
        });

        return contract;
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

    private void setPrivateFields(Contract contract, ContractId id, ContractStatus status,
                                java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt) {
        try {
            java.lang.reflect.Field idField = Contract.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(contract, id);

            java.lang.reflect.Field statusField = Contract.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(contract, status);

            java.lang.reflect.Field createdAtField = Contract.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(contract, createdAt);

            java.lang.reflect.Field updatedAtField = Contract.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(contract, updatedAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set private fields", e);
        }
    }

    private void addSignatureToContract(Contract contract, Signature signature) {
        try {
            java.lang.reflect.Field signaturesField = Contract.class.getDeclaredField("signatures");
            signaturesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Signature> signatures = (List<Signature>) signaturesField.get(contract);
            signatures.add(signature);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add signature to contract", e);
        }
    }
}