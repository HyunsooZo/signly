package com.signly.contract.application.dto;

import java.time.LocalDateTime;

public record CreateContractCommand(
    String templateId,
    String title,
    String content,
    String firstPartyName,
    String firstPartyEmail,
    String firstPartyOrganization,
    String secondPartyName,
    String secondPartyEmail,
    String secondPartyOrganization,
    LocalDateTime expiresAt
) {}