package com.signly.notification.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signly.notification.domain.model.EmailOutbox;
import com.signly.notification.domain.model.EmailOutboxId;
import com.signly.notification.infrastructure.persistence.entity.EmailOutboxEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        entity.setStatus(outbox.getStatus());
        entity.setRetryCount(outbox.getRetryCount());
        entity.setMaxRetries(outbox.getMaxRetries());
        entity.setErrorMessage(outbox.getErrorMessage());
        entity.setCreatedAt(outbox.getCreatedAt());
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
}
