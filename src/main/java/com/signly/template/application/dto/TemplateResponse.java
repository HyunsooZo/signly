package com.signly.template.application.dto;

import com.signly.template.domain.model.TemplateStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TemplateResponse {
    private final String templateId;
    private final String ownerId;
    private final String title;
    private final String content;
    private final int version;
    private final TemplateStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TemplateResponse(String templateId,
                            String ownerId,
                            String title,
                            String content,
                            int version,
                            TemplateStatus status,
                            LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
        this.templateId = templateId;
        this.ownerId = ownerId;
        this.title = title;
        this.content = content;
        this.version = version;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String templateId() {
        return templateId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String ownerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public String title() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String content() {
        return content;
    }

    public int getVersion() {
        return version;
    }

    public int version() {
        return version;
    }

    public TemplateStatus getStatus() {
        return status;
    }

    public TemplateStatus status() {
        return status;
    }

    public Date getCreatedAt() {
        return toDate(createdAt);
    }

    public Date getUpdatedAt() {
        return toDate(updatedAt);
    }

    public LocalDateTime getCreatedAtLocalDateTime() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAtLocalDateTime() {
        return updatedAt;
    }

    private Date toDate(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }
}
