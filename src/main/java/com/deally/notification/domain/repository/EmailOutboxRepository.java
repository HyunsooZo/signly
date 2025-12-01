package com.deally.notification.domain.repository;

import com.deally.notification.domain.model.EmailOutbox;
import com.deally.notification.domain.model.EmailOutboxId;

import java.util.List;
import java.util.Optional;

public interface EmailOutboxRepository {
    EmailOutbox save(EmailOutbox outbox);

    Optional<EmailOutbox> findById(EmailOutboxId id);

    List<EmailOutbox> findPendingEmails(int limit);

    void delete(EmailOutboxId id);
}
