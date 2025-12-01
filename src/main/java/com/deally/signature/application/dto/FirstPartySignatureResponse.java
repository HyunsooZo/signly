package com.deally.signature.application.dto;

import com.deally.signature.domain.model.FirstPartySignature;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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

    public Date getUpdatedAtDate() {
        return updatedAt == null ? null : Date.from(updatedAt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static FirstPartySignatureResponse from(FirstPartySignature signature) {
        return new FirstPartySignatureResponse(
                signature.getSignatureId().value(),
                signature.getOwnerId().value(),
                signature.getStoragePath(),
                signature.getFileMetadata().fileName(),
                signature.getFileMetadata().mimeType(),
                signature.getFileMetadata().fileSize(),
                signature.getFileMetadata().checksum(),
                signature.getUpdatedAt()
        );
    }
}
