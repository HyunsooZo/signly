package com.deally.contract.infrastructure.entity;

import com.deally.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

    public void setContract(ContractJpaEntity contract) {
        this.contract = contract;
    }

}
