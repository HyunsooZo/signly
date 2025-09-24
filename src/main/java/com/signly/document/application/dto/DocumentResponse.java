package com.signly.document.application.dto;

import com.signly.document.domain.model.DocumentType;

import java.time.LocalDateTime;

public record DocumentResponse(
    String id,
    String contractId,
    String uploadedBy,
    DocumentType type,
    String fileName,
    String mimeType,
    long fileSize,
    String storagePath,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}