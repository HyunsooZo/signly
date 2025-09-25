package com.signly.template.application.dto;

import com.signly.template.domain.model.TemplateStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Getter
@AllArgsConstructor
public class TemplateResponse {
    private final String templateId;
    private final String ownerId;
    private final String title;
    private final String content;
    private final int version;
    private final TemplateStatus status;
    @Getter(AccessLevel.NONE)
    private final LocalDateTime createdAt;
    @Getter(AccessLevel.NONE)
    private final LocalDateTime updatedAt;

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
