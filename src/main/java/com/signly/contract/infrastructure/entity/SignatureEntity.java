package com.signly.contract.infrastructure.entity;

import com.signly.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "contract_signatures")
public class SignatureEntity extends BaseEntity {

    @Id
    @Column(name = "signature_id", length = 26)
    private String signatureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractJpaEntity contract;

    @Column(name = "signer_email", length = 255, nullable = false)
    private String signerEmail;

    @Column(name = "signer_name", length = 100, nullable = false)
    private String signerName;

    @Lob
    @Column(name = "signature_data", columnDefinition = "LONGTEXT", nullable = false)
    private String signatureData;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Lob
    @Column(name = "device_info", columnDefinition = "LONGTEXT")
    private String deviceInfo;

    @Column(name = "signature_path", length = 1000)
    private String signaturePath;

    protected SignatureEntity() {}

    public SignatureEntity(
            String signatureId,
            ContractJpaEntity contract,
            String signerEmail,
            String signerName,
            String signatureData,
            LocalDateTime signedAt,
            String ipAddress,
            String deviceInfo,
            String signaturePath
    ) {
        this.signatureId = signatureId;
        this.contract = contract;
        this.signerEmail = signerEmail;
        this.signerName = signerName;
        this.signatureData = signatureData;
        this.signedAt = signedAt;
        this.ipAddress = ipAddress;
        this.deviceInfo = deviceInfo;
        this.signaturePath = signaturePath;
    }

    public void setContract(ContractJpaEntity contract) {
        this.contract = contract;
    }

}