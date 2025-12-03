package com.signly.document.application.dto;

import com.signly.document.domain.model.DocumentType;

public record CreateDocumentCommand(
        String contractId,
        DocumentType type,
        String fileName,
        String mimeType,
        long fileSize,
        String checksum,
        byte[] fileData
) {}