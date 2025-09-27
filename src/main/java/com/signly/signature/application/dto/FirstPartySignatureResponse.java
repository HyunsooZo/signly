package com.signly.signature.application.dto;

import com.signly.signature.domain.model.FirstPartySignature;

import java.time.LocalDateTime;

public record FirstPartySignatureResponse(
        String signatureId,
        String ownerId,
        String storagePath,
        String originalFilename,
        String mimeType,
        long fileSize,
        String checksum,
        LocalDateTime updatedAt
) {

    public static FirstPartySignatureResponse from(FirstPartySignature signature) {
        return new FirstPartySignatureResponse(
                signature.getSignatureId().value(),
                signature.getOwnerId().getValue(),
                signature.getStoragePath(),
                signature.getFileMetadata().getFileName(),
                signature.getFileMetadata().getMimeType(),
                signature.getFileMetadata().getFileSize(),
                signature.getFileMetadata().getChecksum(),
                signature.getUpdatedAt()
        );
    }
}
