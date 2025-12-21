package com.signly.notification.domain.event;

import com.signly.notification.domain.model.EmailOutboxId;

/**
 * EmailOutbox 생성 이벤트
 * 트랜잭션 커밋 직후 발행되어 즉시 이메일 발송을 트리거
 */
public class EmailOutboxCreatedEvent {
    private final EmailOutboxId outboxId;

    public EmailOutboxCreatedEvent(EmailOutboxId outboxId) {
        this.outboxId = outboxId;
    }

    public EmailOutboxId getOutboxId() {
        return outboxId;
    }
}
