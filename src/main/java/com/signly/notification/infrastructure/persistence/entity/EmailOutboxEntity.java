package com.signly.notification.infrastructure.persistence.entity;

import com.signly.notification.domain.model.EmailOutboxStatus;
import com.signly.notification.domain.model.EmailTemplate;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "email_outbox",
        indexes = {
                @Index(name = "idx_status_next_retry", columnList = "status, next_retry_at"),
                @Index(name = "idx_created_at", columnList = "created_at")
        })
public class EmailOutboxEntity {

    @Id
    @Column(name = "id", length = 26)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 50)
    private EmailTemplate emailType;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Lob
    @Column(name = "template_variables", nullable = false, columnDefinition = "LONGTEXT")
    private String templateVariables;

    @Lob
    @Column(name = "attachments", columnDefinition = "LONGTEXT")
    private String attachments;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmailOutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @PrePersist
    public void prePersist() {
        var now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
