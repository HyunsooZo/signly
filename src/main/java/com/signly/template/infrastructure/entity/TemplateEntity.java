package com.signly.template.infrastructure.entity;

import com.signly.template.domain.model.TemplateStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_templates")
public class TemplateEntity {

    @Id
    @Column(name = "template_id", length = 36)
    private String templateId;

    @Column(name = "owner_id", length = 36, nullable = false)
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected TemplateEntity() {
    }

    public TemplateEntity(String templateId, String ownerId, String title, String content,
                         int version, TemplateStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
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
}