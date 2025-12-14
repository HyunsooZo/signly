package com.signly.contract.infrastructure.entity;

import com.signly.common.domain.BaseEntity;
import com.signly.common.encryption.StringEncryptionConverter;
import jakarta.persistence.*;
import lombok.*;

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

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractJpaEntity contract;

    @Column(name = "signer_email", length = 500, nullable = false)
    private String signerEmail;

    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "signer_name", length = 500, nullable = false)
    private String signerName;

    @Lob
    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "signature_data", columnDefinition = "LONGTEXT", nullable = false)
    private String signatureData;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "ip_address", length = 200)
    private String ipAddress;

    @Lob
    @Column(name = "device_info", columnDefinition = "LONGTEXT")
    @Convert(converter = StringEncryptionConverter.class)
    private String deviceInfo;

    @Column(name = "signature_path", length = 1000)
    @Convert(converter = StringEncryptionConverter.class)
    private String signaturePath;

}
