package com.deally.document.domain.model;

import com.deally.common.domain.AggregateRoot;
import com.deally.common.exception.ValidationException;
import com.deally.contract.domain.model.ContractId;
import com.deally.user.domain.model.UserId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Document extends AggregateRoot {
    private final DocumentId id;
    private final ContractId contractId;
    private final UserId uploadedBy;
    private final DocumentType type;
    private final FileMetadata metadata;
    private final String storagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Document() {
        this.id = null;
        this.contractId = null;
        this.uploadedBy = null;
        this.type = null;
        this.metadata = null;
        this.storagePath = null;
        this.createdAt = null;
        this.updatedAt = null;
    }

    private Document(
            DocumentId id,
            ContractId contractId,
            UserId uploadedBy,
            DocumentType type,
            FileMetadata metadata,
            String storagePath
    ) {
        this.id = id;
        this.contractId = contractId;
        this.uploadedBy = uploadedBy;
        this.type = type;
        this.metadata = metadata;
        this.storagePath = storagePath;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Document create(
            ContractId contractId,
            UserId uploadedBy,
            DocumentType type,
            FileMetadata metadata,
            String storagePath
    ) {
        validateStoragePath(storagePath);

        return new Document(DocumentId.generate(), contractId, uploadedBy,
                type, metadata, storagePath);
    }

    public static Document restore(
            DocumentId id,
            ContractId contractId,
            UserId uploadedBy,
            DocumentType type,
            FileMetadata metadata,
            String storagePath,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        Document document = new Document(id, contractId, uploadedBy, type, metadata, storagePath);
        document.createdAt = createdAt;
        document.updatedAt = updatedAt;
        return document;
    }

    private static void validateStoragePath(String storagePath) {
        if (storagePath == null || storagePath.trim().isEmpty()) {
            throw new ValidationException("저장 경로는 필수입니다");
        }
    }

    public boolean isContract() {
        return type == DocumentType.CONTRACT_PDF;
    }

    public boolean isSignature() {
        return type == DocumentType.SIGNATURE_IMAGE;
    }

    public boolean isAttachment() {
        return type == DocumentType.ATTACHMENT;
    }
}