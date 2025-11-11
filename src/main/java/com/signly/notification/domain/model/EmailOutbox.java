package com.signly.notification.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class EmailOutbox {
    private final EmailOutboxId id;
    private final EmailTemplate emailTemplate;
    private final String recipientEmail;
    private final String recipientName;
    private final Map<String, Object> templateVariables;
    private final List<EmailAttachment> attachments;
    private EmailOutboxStatus status;
    private int retryCount;
    private final int maxRetries;
    private String errorMessage;
    private final LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime nextRetryAt;

    private EmailOutbox(
            EmailOutboxId id,
            EmailTemplate emailTemplate,
            String recipientEmail,
            String recipientName,
            Map<String, Object> templateVariables,
            List<EmailAttachment> attachments,
            int maxRetries
    ) {
        this.id = id;
        this.emailTemplate = emailTemplate;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.templateVariables = templateVariables;
        this.attachments = attachments != null ? new ArrayList<>(attachments) : new ArrayList<>();
        this.status = EmailOutboxStatus.PENDING;
        this.retryCount = 0;
        this.maxRetries = maxRetries;
        this.createdAt = LocalDateTime.now();
        this.nextRetryAt = LocalDateTime.now(); // 즉시 발송 시도
    }

    /**
     * 첨부파일 없는 이메일 생성
     */
    public static EmailOutbox create(
            EmailTemplate emailTemplate,
            String recipientEmail,
            String recipientName,
            Map<String, Object> templateVariables
    ) {
        return new EmailOutbox(
                EmailOutboxId.generate(),
                emailTemplate,
                recipientEmail,
                recipientName,
                templateVariables,
                Collections.emptyList(),
                3 // 기본 최대 재시도 횟수
        );
    }

    /**
     * 첨부파일 있는 이메일 생성
     */
    public static EmailOutbox create(
            EmailTemplate emailTemplate,
            String recipientEmail,
            String recipientName,
            Map<String, Object> templateVariables,
            List<EmailAttachment> attachments
    ) {
        return new EmailOutbox(
                EmailOutboxId.generate(),
                emailTemplate,
                recipientEmail,
                recipientName,
                templateVariables,
                attachments,
                3 // 기본 최대 재시도 횟수
        );
    }

    public static EmailOutbox restore(
            EmailOutboxId id,
            EmailTemplate emailTemplate,
            String recipientEmail,
            String recipientName,
            Map<String, Object> templateVariables,
            List<EmailAttachment> attachments,
            EmailOutboxStatus status,
            int retryCount,
            int maxRetries,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime sentAt,
            LocalDateTime nextRetryAt
    ) {
        EmailOutbox outbox = new EmailOutbox(id, emailTemplate, recipientEmail, recipientName, templateVariables, attachments, maxRetries);
        outbox.status = status;
        outbox.retryCount = retryCount;
        outbox.errorMessage = errorMessage;
        outbox.sentAt = sentAt;
        outbox.nextRetryAt = nextRetryAt;
        return outbox;
    }

    public void markAsSent() {
        this.status = EmailOutboxStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    public void markAsFailed(String errorMessage) {
        this.retryCount++;
        this.errorMessage = errorMessage;

        if (this.retryCount >= this.maxRetries) {
            this.status = EmailOutboxStatus.FAILED;
            this.nextRetryAt = null;
        } else {
            // Exponential backoff: 1분, 2분, 4분...
            int minutesToWait = (int) Math.pow(2, this.retryCount);
            this.nextRetryAt = LocalDateTime.now().plusMinutes(minutesToWait);
        }
    }

    public boolean canRetry() {
        return status == EmailOutboxStatus.PENDING
                && retryCount < maxRetries
                && (nextRetryAt == null || LocalDateTime.now().isAfter(nextRetryAt));
    }

    public List<EmailAttachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

}
