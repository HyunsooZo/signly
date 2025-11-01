package com.signly.contract.infrastructure.entity;

import com.signly.common.domain.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "signatures", indexes = {
    @Index(name = "idx_signature_contract_id", columnList = "contract_id"),
    @Index(name = "idx_signature_signer_email", columnList = "signer_email"),
    @Index(name = "idx_signature_signed_at", columnList = "signed_at"),
    @Index(name = "idx_signature_contract_signer", columnList = "contract_id, signer_email"),
    @Index(name = "idx_signature_contract_signed", columnList = "contract_id, signed_at")
})
public class SignatureJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractJpaEntity contract;

    @Column(name = "signer_email", nullable = false, length = 255)
    private String signerEmail;

    @Column(name = "signer_name", nullable = false, length = 100)
    private String signerName;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Lob
    @Column(name = "signature_data", nullable = false, columnDefinition = "TEXT")
    private String signatureData;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    protected SignatureJpaEntity() {}

    public SignatureJpaEntity(String signerEmail, String signerName, LocalDateTime signedAt,
                            String signatureData, String ipAddress) {
        this.signerEmail = signerEmail;
        this.signerName = signerName;
        this.signedAt = signedAt;
        this.signatureData = signatureData;
        this.ipAddress = ipAddress;
    }

    public Long getId() {
        return id;
    }

    public ContractJpaEntity getContract() {
        return contract;
    }

    public void setContract(ContractJpaEntity contract) {
        this.contract = contract;
    }

    public String getSignerEmail() {
        return signerEmail;
    }

    public String getSignerName() {
        return signerName;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public String getSignatureData() {
        return signatureData;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}