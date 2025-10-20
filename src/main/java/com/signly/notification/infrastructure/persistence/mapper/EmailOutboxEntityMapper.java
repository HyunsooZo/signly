package com.signly.notification.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signly.notification.domain.model.EmailAttachment;
import com.signly.notification.domain.model.EmailOutbox;
import com.signly.notification.domain.model.EmailOutboxId;
import com.signly.notification.infrastructure.persistence.entity.EmailOutboxEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class EmailOutboxEntityMapper {
    private final ObjectMapper objectMapper;

    public EmailOutboxEntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EmailOutboxEntity toEntity(EmailOutbox outbox) {
        EmailOutboxEntity entity = new EmailOutboxEntity();
        entity.setId(outbox.getId().getValue());
        entity.setEmailType(outbox.getEmailTemplate());
        entity.setRecipientEmail(outbox.getRecipientEmail());
        entity.setRecipientName(outbox.getRecipientName());
        entity.setTemplateVariables(serializeTemplateVariables(outbox.getTemplateVariables()));
        entity.setAttachments(serializeAttachments(outbox.getAttachments()));
        entity.setStatus(outbox.getStatus());
        entity.setRetryCount(outbox.getRetryCount());
        entity.setMaxRetries(outbox.getMaxRetries());
        entity.setErrorMessage(outbox.getErrorMessage());
        entity.setCreatedAt(outbox.getCreatedAt());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setSentAt(outbox.getSentAt());
        entity.setNextRetryAt(outbox.getNextRetryAt());
        return entity;
    }

    public EmailOutbox toDomain(EmailOutboxEntity entity) {
        return EmailOutbox.restore(
                EmailOutboxId.of(entity.getId()),
                entity.getEmailType(),
                entity.getRecipientEmail(),
                entity.getRecipientName(),
                deserializeTemplateVariables(entity.getTemplateVariables()),
                deserializeAttachments(entity.getAttachments()),
                entity.getStatus(),
                entity.getRetryCount(),
                entity.getMaxRetries(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getSentAt(),
                entity.getNextRetryAt()
        );
    }

    private String serializeTemplateVariables(Map<String, Object> variables) {
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize template variables", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deserializeTemplateVariables(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize template variables", e);
        }
    }

    private String serializeAttachments(List<EmailAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }

        try {
            // EmailAttachment를 직렬화 가능한 DTO로 변환
            List<Map<String, String>> attachmentDTOs = new ArrayList<>();
            for (EmailAttachment attachment : attachments) {
                Map<String, String> dto = new HashMap<>();
                dto.put("fileName", attachment.getFileName());
                dto.put("content", Base64.getEncoder().encodeToString(attachment.getContent()));
                dto.put("contentType", attachment.getContentType());
                attachmentDTOs.add(dto);
            }
            return objectMapper.writeValueAsString(attachmentDTOs);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize attachments", e);
        }
    }

    private List<EmailAttachment> deserializeAttachments(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }

        try {
            List<Map<String, String>> attachmentDTOs = objectMapper.readValue(
                    json,
                    new TypeReference<List<Map<String, String>>>() {}
            );

            List<EmailAttachment> attachments = new ArrayList<>();
            for (Map<String, String> dto : attachmentDTOs) {
                byte[] content = Base64.getDecoder().decode(dto.get("content"));
                EmailAttachment attachment = EmailAttachment.of(
                        dto.get("fileName"),
                        content,
                        dto.get("contentType")
                );
                attachments.add(attachment);
            }
            return attachments;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize attachments", e);
        }
    }
}
