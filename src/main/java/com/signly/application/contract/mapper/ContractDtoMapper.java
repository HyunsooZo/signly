package com.signly.application.contract.mapper;

import com.signly.application.contract.dto.ContractResponse;
import com.signly.application.contract.dto.PartyInfoResponse;
import com.signly.application.contract.dto.SignatureResponse;
import com.signly.domain.contract.model.Contract;
import com.signly.domain.contract.model.PartyInfo;
import com.signly.domain.contract.model.Signature;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContractDtoMapper {

    public ContractResponse toResponse(Contract contract) {
        return new ContractResponse(
            contract.getId().getValue(),
            contract.getCreatorId().getValue(),
            contract.getTemplateId() != null ? contract.getTemplateId().getValue() : null,
            contract.getTitle(),
            contract.getContent().getValue(),
            toPartyInfoResponse(contract.getFirstParty()),
            toPartyInfoResponse(contract.getSecondParty()),
            contract.getStatus(),
            toSignatureResponses(contract.getSignatures()),
            contract.getPendingSigners(),
            contract.getExpiresAt(),
            contract.getCreatedAt(),
            contract.getUpdatedAt()
        );
    }

    private PartyInfoResponse toPartyInfoResponse(PartyInfo partyInfo) {
        return new PartyInfoResponse(
            partyInfo.getName(),
            partyInfo.getEmail(),
            partyInfo.getOrganizationName()
        );
    }

    private List<SignatureResponse> toSignatureResponses(List<Signature> signatures) {
        return signatures.stream()
            .map(this::toSignatureResponse)
            .collect(Collectors.toList());
    }

    private SignatureResponse toSignatureResponse(Signature signature) {
        return new SignatureResponse(
            signature.getSignerEmail(),
            signature.getSignerName(),
            signature.getSignedAt(),
            signature.getIpAddress()
        );
    }
}