package com.signly.template.application.dto;

import com.signly.template.domain.model.TemplateStatus;

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