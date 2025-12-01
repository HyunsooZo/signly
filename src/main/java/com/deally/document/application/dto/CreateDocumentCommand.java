package com.deally.document.application.dto;

import com.deally.document.domain.model.DocumentType;

public record CreateDocumentCommand(
        String contractId,
        DocumentType type,
        String fileName,
        String mimeType,
        long fileSize,
        String checksum,
        byte[] fileData
) {}