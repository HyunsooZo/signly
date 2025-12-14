package com.signly.contract.infrastructure.entity;

import com.signly.common.domain.BaseEntity;
import com.signly.common.encryption.StringEncryptionConverter;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.domain.model.PresetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "contracts", indexes = {
        @Index(name = "idx_contract_creator_id", columnList = "creator_id"),
        @Index(name = "idx_contract_template_id", columnList = "template_id"),
        @Index(name = "idx_contract_status", columnList = "status"),
        @Index(name = "idx_contract_expires_at", columnList = "expires_at"),
        @Index(name = "idx_contract_sign_token", columnList = "sign_token"),
        @Index(name = "idx_contract_creator_status", columnList = "creator_id, status"),
        @Index(name = "idx_contract_status_expires", columnList = "status, expires_at")
        // email 인덱스는 V14 마이그레이션에서 emailHash 기반으로 추가됨
})
public class ContractJpaEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 26)
    private String id;

    @Column(name = "creator_id", nullable = false, length = 26)
    private String creatorId;

    @Column(name = "template_id", length = 26)
    private String templateId;

    @Setter
    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Setter
    @Lob
    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Setter
    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "template_data", columnDefinition = "JSON")
    private String templateData;

    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "first_party_name", nullable = false, length = 500)
    private String firstPartyName;

    // 갑(First Party) 이메일 (암호화 저장)
    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "first_party_email", nullable = false, length = 500)
    private String firstPartyEmail;

    // 갑(First Party) 이메일 해시 (검색용, Blind Index)
    @Setter
    @Column(name = "first_party_email_hash", length = 64)
    private String firstPartyEmailHash;

    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "first_party_organization", length = 200)
    private String firstPartyOrganization;

    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "second_party_name", nullable = false, length = 500)
    private String secondPartyName;

    // 을(Second Party) 이메일 (암호화 저장)
    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "second_party_email", nullable = false, length = 500)
    private String secondPartyEmail;

    // 을(Second Party) 이메일 해시 (검색용, Blind Index)
    @Setter
    @Column(name = "second_party_email_hash", length = 64)
    private String secondPartyEmailHash;

    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "second_party_organization", length = 200)
    private String secondPartyOrganization;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ContractStatus status;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<SignatureEntity> signatures = new ArrayList<>();

    @Setter
    @Column(name = "sign_token", nullable = false, unique = true, length = 26)
    private String signToken;

    @Setter
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "preset_type", length = 50, nullable = false)
    private PresetType presetType = PresetType.NONE;

    @Setter
    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "pdf_path", length = 1000)
    private String pdfPath;

    public ContractJpaEntity(
            String id,
            String creatorId,
            String templateId,
            String title,
            String content,
            String templateData,
            String firstPartyName,
            String firstPartyEmail,
            String firstPartyOrganization,
            String secondPartyName,
            String secondPartyEmail,
            String secondPartyOrganization,
            ContractStatus status,
            String signToken,
            LocalDateTime expiresAt,
            PresetType presetType
    ) {
        this.id = id;
        this.creatorId = creatorId;
        this.templateId = templateId;
        this.title = title;
        this.content = content;
        this.templateData = templateData;
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

    public void addSignature(SignatureEntity signature) {
        signatures.add(signature);
        signature.setContract(this);
    }

    public void setPresetType(PresetType presetType) {
        this.presetType = presetType != null ? presetType : PresetType.NONE;
    }

}