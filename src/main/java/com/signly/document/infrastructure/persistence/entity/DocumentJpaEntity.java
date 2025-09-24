package com.signly.document.infrastructure.persistence.entity;

import com.signly.document.domain.model.DocumentType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class DocumentJpaEntity {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "contract_id", nullable = false, length = 36)
    private String contractId;

    @Column(name = "uploaded_by", nullable = false, length = 36)
    private String uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private DocumentType type;

    @Column(name = "filename", nullable = false, length = 500)
    private String filename;

    @Column(name = "original_filename", nullable = false, length = 500)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "checksum", nullable = false, length = 255)
    private String checksum;

    @Column(name = "storage_path", nullable = false, length = 1000)
    private String storagePath;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected DocumentJpaEntity() {}

    public DocumentJpaEntity(String id, String contractId, String uploadedBy,
                           DocumentType type, String filename, String originalFilename,
                           String contentType, Long fileSize, String checksum, String storagePath) {
        this.id = id;
        this.contractId = contractId;
        this.uploadedBy = uploadedBy;
        this.type = type;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.checksum = checksum;
        this.storagePath = storagePath;
    }

    public String getId() {
        return id;
    }

    public String getContractId() {
        return contractId;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public DocumentType getType() {
        return type;
    }

    public String getFilename() {
        return filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}