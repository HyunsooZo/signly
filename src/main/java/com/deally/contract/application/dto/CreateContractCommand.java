package com.deally.contract.application.dto;

import com.deally.contract.domain.model.PresetType;

import java.time.LocalDateTime;
import java.util.Map;

public record CreateContractCommand(
        String templateId,
        String title,
        String content,
        Map<String, String> variableValues,
        String firstPartyName,
        String firstPartyEmail,
        String firstPartyOrganization,
        String secondPartyName,
        String secondPartyEmail,
        String secondPartyOrganization,
        LocalDateTime expiresAt,
        PresetType presetType
) {}