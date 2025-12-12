package com.signly.template.infrastructure.entity;

import com.signly.template.domain.model.TemplateStatus;
import jakarta.persistence.*;
import lombok.*;

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
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
}
