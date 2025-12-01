package com.deally.contract.application.mapper;

import com.deally.contract.application.dto.ContractResponse;
import com.deally.contract.application.dto.PartyInfoResponse;
import com.deally.contract.application.dto.SignatureResponse;
import com.deally.contract.domain.model.Contract;
import com.deally.contract.domain.model.PartyInfo;
import com.deally.contract.domain.model.Signature;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContractDtoMapper {

    public ContractResponse toResponse(Contract contract) {
        return new ContractResponse(
                contract.getId().value(),
                contract.getCreatorId().value(),
                contract.getTemplateId() != null ? contract.getTemplateId().value() : null,
                contract.getTitle(),
                contract.getContent().content(),
                toPartyInfoResponse(contract.getFirstParty()),
                toPartyInfoResponse(contract.getSecondParty()),
                contract.getStatus(),
                toSignatureResponses(contract.getSignatures()),
                contract.getPendingSigners(),
                contract.getExpiresAt(),
                contract.getPresetType(),
                contract.getPdfPath(),
                contract.getCreatedAt(),
                contract.getUpdatedAt()
        );
    }

    private PartyInfoResponse toPartyInfoResponse(PartyInfo partyInfo) {
        return new PartyInfoResponse(
                partyInfo.name(),
                partyInfo.email(),
                partyInfo.organizationName()
        );
    }

    private List<SignatureResponse> toSignatureResponses(List<Signature> signatures) {
        return signatures.stream()
                .map(this::toSignatureResponse)
                .collect(Collectors.toList());
    }

    private SignatureResponse toSignatureResponse(Signature signature) {
        return new SignatureResponse(
                signature.signerEmail(),
                signature.signerName(),
                signature.signedAt(),
                signature.ipAddress()
        );
    }
}