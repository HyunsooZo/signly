package com.signly.signature.infrastructure.persistence.entity;

import com.signly.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "first_party_signatures")
public class FirstPartySignatureEntity extends BaseEntity {

    @Id
    @Column(name = "signature_id", length = 26)
    private String signatureId;

    @Column(name = "owner_id", length = 26, nullable = false, unique = true)
    private String ownerId;

    @Column(name = "storage_path", nullable = false, length = 512)
    private String storagePath;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "checksum", nullable = false, length = 255)
    private String checksum;

    protected FirstPartySignatureEntity() {
    }

    public FirstPartySignatureEntity(String signatureId,
                                     String ownerId,
                                     String storagePath,
                                     String originalFilename,
                                     String mimeType,
                                     long fileSize,
                                     String checksum,
                                     LocalDateTime createdAt,
                                     LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.signatureId = signatureId;
        this.ownerId = ownerId;
        this.storagePath = storagePath;
        this.originalFilename = originalFilename;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.checksum = checksum;
    }

    public String getSignatureId() {
        return signatureId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getChecksum() {
        return checksum;
    }
}
