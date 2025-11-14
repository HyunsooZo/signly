package com.signly.document.infrastructure.persistence.entity;

import com.signly.document.domain.model.DocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_contract_id", columnList = "contract_id"),
        @Index(name = "idx_uploaded_by", columnList = "uploaded_by"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_contract_type", columnList = "contract_id, type"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class DocumentJpaEntity {
    @Id
    @Column(name = "id", length = 26)
    private String id;

    @Column(name = "contract_id", nullable = false, length = 26)
    private String contractId;

    @Column(name = "uploaded_by", nullable = false, length = 26)
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

    public DocumentJpaEntity(
            String id,
            String contractId,
            String uploadedBy,
            DocumentType type,
            String filename,
            String originalFilename,
            String contentType,
            Long fileSize,
            String checksum,
            String storagePath
    ) {
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

}