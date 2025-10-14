package com.signly.notification.infrastructure.persistence.repository;

import com.signly.notification.domain.model.EmailOutbox;
import com.signly.notification.domain.model.EmailOutboxId;
import com.signly.notification.domain.model.EmailOutboxStatus;
import com.signly.notification.domain.repository.EmailOutboxRepository;
import com.signly.notification.infrastructure.persistence.entity.EmailOutboxEntity;
import com.signly.notification.infrastructure.persistence.mapper.EmailOutboxEntityMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class EmailOutboxRepositoryImpl implements EmailOutboxRepository {
    private final EmailOutboxJpaRepository jpaRepository;
    private final EmailOutboxEntityMapper mapper;

    public EmailOutboxRepositoryImpl(EmailOutboxJpaRepository jpaRepository, EmailOutboxEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public EmailOutbox save(EmailOutbox outbox) {
        EmailOutboxEntity entity = mapper.toEntity(outbox);
        EmailOutboxEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EmailOutbox> findById(EmailOutboxId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<EmailOutbox> findPendingEmails(int limit) {
        List<EmailOutboxEntity> entities = jpaRepository.findPendingEmails(
                EmailOutboxStatus.PENDING,
                LocalDateTime.now(),
                limit
        );
        return entities.stream()
                .limit(limit)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(EmailOutboxId id) {
        jpaRepository.deleteById(id.getValue());
    }
}
