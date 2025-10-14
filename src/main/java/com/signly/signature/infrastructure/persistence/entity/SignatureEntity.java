package com.signly.signature.infrastructure.persistence.entity;

import com.signly.common.domain.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_signatures")
public class SignatureEntity extends BaseEntity {

    @Id
    @Column(name = "signature_id", length = 26)
    private String signatureId;

    @Column(name = "contract_id", length = 26, nullable = false)
    private String contractId;

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

    protected SignatureEntity() {}

    public SignatureEntity(String signatureId, String contractId, String signerEmail, String signerName,
                          String signatureData, LocalDateTime signedAt, String ipAddress, String deviceInfo) {
        this.signatureId = signatureId;
        this.contractId = contractId;
        this.signerEmail = signerEmail;
        this.signerName = signerName;
        this.signatureData = signatureData;
        this.signedAt = signedAt;
        this.ipAddress = ipAddress;
        this.deviceInfo = deviceInfo;
    }

    public String getSignatureId() {
        return signatureId;
    }

    public String getContractId() {
        return contractId;
    }

    public String getSignerEmail() {
        return signerEmail;
    }

    public String getSignerName() {
        return signerName;
    }

    public String getSignatureData() {
        return signatureData;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }
}
