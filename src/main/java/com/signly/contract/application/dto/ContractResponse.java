package com.signly.contract.application.dto;

import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.domain.model.PresetType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class ContractResponse {
    private final String id;
    private final String creatorId;
    private final String templateId;
    private final String title;
    private final String content;
    private final PartyInfoResponse firstParty;
    private final PartyInfoResponse secondParty;
    private final ContractStatus status;
    private final List<SignatureResponse> signatures;
    private final List<String> pendingSigners;
    @Getter(AccessLevel.NONE)
    private final LocalDateTime expiresAt;
    private final PresetType presetType;
    @Getter(AccessLevel.NONE)
    private final LocalDateTime createdAt;
    @Getter(AccessLevel.NONE)
    private final LocalDateTime updatedAt;

    public Date getExpiresAt() {
        return toDate(expiresAt);
    }

    public Date getCreatedAt() {
        return toDate(createdAt);
    }

    public Date getUpdatedAt() {
        return toDate(updatedAt);
    }

    public LocalDateTime getExpiresAtLocalDateTime() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAtLocalDateTime() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAtLocalDateTime() {
        return updatedAt;
    }

    private Date toDate(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }
}
