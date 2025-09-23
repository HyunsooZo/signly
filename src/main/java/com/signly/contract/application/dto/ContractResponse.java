package com.signly.contract.application.dto;

import com.signly.contract.domain.model.ContractStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ContractResponse(
    String id,
    String creatorId,
    String templateId,
    String title,
    String content,
    PartyInfoResponse firstParty,
    PartyInfoResponse secondParty,
    ContractStatus status,
    List<SignatureResponse> signatures,
    List<String> pendingSigners,
    LocalDateTime expiresAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}