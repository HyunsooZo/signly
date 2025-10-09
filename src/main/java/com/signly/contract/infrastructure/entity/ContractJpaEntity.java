package com.signly.contract.infrastructure.entity;

import com.signly.common.domain.BaseEntity;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.domain.model.PresetType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contracts", indexes = {
    @Index(name = "idx_contract_creator_id", columnList = "creator_id"),
    @Index(name = "idx_contract_template_id", columnList = "template_id"),
    @Index(name = "idx_contract_status", columnList = "status"),
    @Index(name = "idx_contract_first_party_email", columnList = "first_party_email"),
    @Index(name = "idx_contract_second_party_email", columnList = "second_party_email"),
    @Index(name = "idx_contract_expires_at", columnList = "expires_at"),
    @Index(name = "idx_contract_sign_token", columnList = "sign_token")
})
public class ContractJpaEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 26)
    private String id;

    @Column(name = "creator_id", nullable = false, length = 26)
    private String creatorId;

    @Column(name = "template_id", length = 26)
    private String templateId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "first_party_name", nullable = false, length = 100)
    private String firstPartyName;

    @Column(name = "first_party_email", nullable = false, length = 255)
    private String firstPartyEmail;

    @Column(name = "first_party_organization", length = 200)
    private String firstPartyOrganization;

    @Column(name = "second_party_name", nullable = false, length = 100)
    private String secondPartyName;

    @Column(name = "second_party_email", nullable = false, length = 255)
    private String secondPartyEmail;

    @Column(name = "second_party_organization", length = 200)
    private String secondPartyOrganization;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ContractStatus status;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SignatureJpaEntity> signatures = new ArrayList<>();

    @Column(name = "sign_token", nullable = false, unique = true, length = 26)
    private String signToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "preset_type", length = 50, nullable = false)
    private PresetType presetType = PresetType.NONE;

    protected ContractJpaEntity() {}

    public ContractJpaEntity(String id, String creatorId, String templateId, String title,
                           String content, String firstPartyName, String firstPartyEmail,
                           String firstPartyOrganization, String secondPartyName,
                           String secondPartyEmail, String secondPartyOrganization,
                           ContractStatus status, String signToken, LocalDateTime expiresAt,
                           PresetType presetType) {
        this.id = id;
        this.creatorId = creatorId;
        this.templateId = templateId;
        this.title = title;
        this.content = content;
        this.firstPartyName = firstPartyName;
        this.firstPartyEmail = firstPartyEmail;
        this.firstPartyOrganization = firstPartyOrganization;
        this.secondPartyName = secondPartyName;
        this.secondPartyEmail = secondPartyEmail;
        this.secondPartyOrganization = secondPartyOrganization;
        this.status = status;
        this.signToken = signToken;
        this.expiresAt = expiresAt;
        this.presetType = presetType != null ? presetType : PresetType.NONE;
    }

    public String getId() {
        return id;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFirstPartyName() {
        return firstPartyName;
    }

    public String getFirstPartyEmail() {
        return firstPartyEmail;
    }

    public String getFirstPartyOrganization() {
        return firstPartyOrganization;
    }

    public String getSecondPartyName() {
        return secondPartyName;
    }

    public String getSecondPartyEmail() {
        return secondPartyEmail;
    }

    public String getSecondPartyOrganization() {
        return secondPartyOrganization;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public List<SignatureJpaEntity> getSignatures() {
        return signatures;
    }

    public void addSignature(SignatureJpaEntity signature) {
        signatures.add(signature);
        signature.setContract(this);
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getSignToken() {
        return signToken;
    }

    public void setSignToken(String signToken) {
        this.signToken = signToken;
    }

    public PresetType getPresetType() {
        return presetType;
    }

    public void setPresetType(PresetType presetType) {
        this.presetType = presetType != null ? presetType : PresetType.NONE;
    }
}