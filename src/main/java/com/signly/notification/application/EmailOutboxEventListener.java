package com.signly.notification.application;

import com.signly.notification.application.dto.EmailRequest;
import com.signly.notification.domain.event.EmailOutboxCreatedEvent;
import com.signly.notification.domain.model.EmailOutbox;
import com.signly.notification.domain.model.EmailOutboxId;
import com.signly.notification.domain.repository.EmailOutboxRepository;
import com.signly.notification.infrastructure.gateway.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EmailOutboxEventListener {
    private static final Logger logger = LoggerFactory.getLogger(EmailOutboxEventListener.class);

    private final EmailOutboxRepository outboxRepository;
    private final EmailSender emailSender;

    public EmailOutboxEventListener(
            EmailOutboxRepository outboxRepository,
            EmailSender emailSender
    ) {
        this.outboxRepository = outboxRepository;
        this.emailSender = emailSender;
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailOutboxCreated(EmailOutboxCreatedEvent event) {
        try {
            logger.debug("EmailOutbox 생성 이벤트 수신: outboxId={}", event.getOutboxId().value());

            processEmailOutbox(event.getOutboxId());

        } catch (Exception e) {
            logger.error("이메일 발송 이벤트 처리 중 예외 발생 (스케줄러가 재시도 예정): outboxId={}", event.getOutboxId().value(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processEmailOutbox(EmailOutboxId outboxId) {
        var outboxOpt = outboxRepository.findById(outboxId);

        if (outboxOpt.isEmpty()) {
            logger.warn("EmailOutbox를 찾을 수 없음: outboxId={}", outboxId.value());
            return;
        }

        EmailOutbox outbox = outboxOpt.get();

        if (!outbox.canRetry()) {
            logger.debug("발송 불가 상태: outboxId={}, status={}, retryCount={}",
                    outboxId.value(), outbox.getStatus(), outbox.getRetryCount());
            return;
        }

        try {
            EmailRequest request = new EmailRequest(
                    outbox.getRecipientEmail(),
                    outbox.getRecipientName(),
                    outbox.getEmailTemplate(),
                    outbox.getTemplateVariables(),
                    outbox.getAttachments()
            );

            emailSender.sendEmail(request);

            outbox.markAsSent();
            outboxRepository.save(outbox);

            logger.info("이메일 즉시 발송 성공: outboxId={}, recipient={}, template={}",
                    outboxId.value(),
                    outbox.getRecipientEmail(),
                    outbox.getEmailTemplate());

        } catch (Exception e) {
            // 발송 실패 - 상태 업데이트 (재시도 카운트 증가, nextRetryAt 설정)
            logger.warn("이메일 즉시 발송 실패 (스케줄러가 재시도 예정): outboxId={}, recipient={}",
                    outboxId.value(), outbox.getRecipientEmail(), e);

            outbox.markAsFailed(e.getMessage());
            outboxRepository.save(outbox);
        }
    }
}
