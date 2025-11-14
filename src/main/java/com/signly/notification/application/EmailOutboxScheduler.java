package com.signly.notification.application;

import com.signly.notification.application.dto.EmailRequest;
import com.signly.notification.domain.model.EmailOutbox;
import com.signly.notification.domain.repository.EmailOutboxRepository;
import com.signly.notification.infrastructure.EmailSender;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailOutboxScheduler {
    private static final Logger logger = LoggerFactory.getLogger(EmailOutboxScheduler.class);
    private static final int BATCH_SIZE = 10; // 한 번에 처리할 이메일 수

    private final EmailOutboxRepository outboxRepository;
    private final EmailSender emailSender;

    @Scheduled(fixedDelay = 10000) // 10초마다 실행
    @Transactional
    public void processPendingEmails() {
        try {
            var pendingEmails = outboxRepository.findPendingEmails(BATCH_SIZE);

            if (pendingEmails.isEmpty()) {
                return;
            }

            logger.info("이메일 Outbox 처리 시작: {} 건", pendingEmails.size());

            int successCount = 0;
            int failureCount = 0;

            for (EmailOutbox outbox : pendingEmails) {
                if (processEmail(outbox)) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }

            logger.info("이메일 Outbox 처리 완료: {} 건 (성공: {}, 실패: {})",
                    pendingEmails.size(), successCount, failureCount);

        } catch (Exception e) {
            logger.error("이메일 Outbox 처리 중 오류 발생", e);
        }
    }

    private boolean processEmail(EmailOutbox outbox) {
        try {
            if (!outbox.canRetry()) {
                logger.warn("재시도 불가한 이메일: id={}", outbox.getId().value());
                return false;
            }

            EmailRequest request = new EmailRequest(
                    outbox.getRecipientEmail(),
                    outbox.getRecipientName(),
                    outbox.getEmailTemplate(),
                    outbox.getTemplateVariables(),
                    outbox.getAttachments()
            );

            emailSender.sendEmail(request);

            // 발송 성공
            outbox.markAsSent();
            outboxRepository.save(outbox);

            logger.info("이메일 발송 성공: id={}, recipient={}, attachments={}",
                    outbox.getId().value(), outbox.getRecipientEmail(),
                    outbox.getAttachments().size());

            return true;

        } catch (Exception e) {
            // 발송 실패
            logger.error("이메일 발송 실패: id={}, recipient={}",
                    outbox.getId().value(), outbox.getRecipientEmail(), e);

            outbox.markAsFailed(e.getMessage());
            outboxRepository.save(outbox);

            return false;
        }
    }
}
