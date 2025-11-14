package com.signly.template.infrastructure.entity;

import com.signly.template.domain.model.TemplateStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_templates", indexes = {
        @Index(name = "idx_template_owner_id", columnList = "owner_id"),
        @Index(name = "idx_template_status", columnList = "status"),
        @Index(name = "idx_template_is_preset", columnList = "is_preset"),
        @Index(name = "idx_template_preset_id", columnList = "preset_id"),
        @Index(name = "idx_template_owner_status", columnList = "owner_id, status"),
        @Index(name = "idx_template_created_at", columnList = "created_at")
})
public class TemplateEntity {

    @Id
    @Column(name = "template_id", length = 26)
    private String templateId;

    @Column(name = "owner_id", length = 26, nullable = true)
    private String ownerId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "JSON", nullable = false)
    private String content;

    @Column(name = "version", nullable = false)
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TemplateStatus status;

    @Column(name = "is_preset", nullable = false)
    private boolean isPreset;

    @Column(name = "preset_id", length = 100, nullable = true)
    private String presetId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected TemplateEntity() {
    }

    public TemplateEntity(
            String templateId,
            String ownerId,
            String title,
            String content,
            int version,
            TemplateStatus status,
            boolean isPreset,
            String presetId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.templateId = templateId;
        this.ownerId = ownerId;
        this.title = title;
        this.content = content;
        this.version = version;
        this.status = status;
        this.isPreset = isPreset;
        this.presetId = presetId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getVersion() {
        return version;
    }

    public TemplateStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setStatus(TemplateStatus status) {
        this.status = status;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPreset() {
        return isPreset;
    }

    public String getPresetId() {
        return presetId;
    }
}