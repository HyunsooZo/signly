package com.signly.application.template.dto;

import com.signly.domain.template.model.TemplateStatus;

import java.time.LocalDateTime;

public record TemplateResponse(
        String templateId,
        String ownerId,
        String title,
        String content,
        int version,
        TemplateStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}