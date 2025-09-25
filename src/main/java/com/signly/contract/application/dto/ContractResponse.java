package com.signly.contract.application.dto;

import com.signly.contract.domain.model.ContractStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ContractResponse(String id,
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
                            LocalDateTime updatedAt) {
        this.id = id;
        this.creatorId = creatorId;
        this.templateId = templateId;
        this.title = title;
        this.content = content;
        this.firstParty = firstParty;
        this.secondParty = secondParty;
        this.status = status;
        this.signatures = signatures;
        this.pendingSigners = pendingSigners;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String id() {
        return id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String creatorId() {
        return creatorId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String templateId() {
        return templateId;
    }

    public String getTitle() {
        return title;
    }

    public String title() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String content() {
        return content;
    }

    public PartyInfoResponse getFirstParty() {
        return firstParty;
    }

    public PartyInfoResponse getSecondParty() {
        return secondParty;
    }

    public PartyInfoResponse firstParty() {
        return firstParty;
    }

    public PartyInfoResponse secondParty() {
        return secondParty;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public ContractStatus status() {
        return status;
    }

    public List<SignatureResponse> getSignatures() {
        return signatures;
    }

    public List<String> getPendingSigners() {
        return pendingSigners;
    }

    public List<SignatureResponse> signatures() {
        return signatures;
    }

    public List<String> pendingSigners() {
        return pendingSigners;
    }

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

    private Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
