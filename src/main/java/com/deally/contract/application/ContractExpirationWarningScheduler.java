package com.deally.contract.application;

import com.deally.contract.domain.model.ContractStatus;
import com.deally.contract.domain.repository.ContractRepository;
import com.deally.notification.application.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 계약서 만료 임박 알림 스케줄러
 * SRP: 만료 임박 알림 발송만 담당
 */
@Component
@RequiredArgsConstructor
public class ContractExpirationWarningScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ContractExpirationWarningScheduler.class);
    private static final int WARNING_DAYS = 1;

    private final ContractRepository contractRepository;
    private final EmailNotificationService emailNotificationService;

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendExpirationWarnings() {
        logger.info("계약서 만료 임박 알림 스케줄러 시작");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime warningThreshold = now.plusDays(WARNING_DAYS);

            var expiringContracts = contractRepository.findByStatusAndExpiresAtBefore(ContractStatus.PENDING, warningThreshold);

            int sentCount = 0;
            for (var contract : expiringContracts) {
                if (contract.getExpiresAt() != null) {
                    long daysLeft = ChronoUnit.DAYS.between(now, contract.getExpiresAt());

                    if (daysLeft == WARNING_DAYS && daysLeft > 0) {
                        emailNotificationService.sendExpirationWarning(contract, (int) daysLeft);
                        sentCount++;
                    }
                }
            }

            logger.info("계약서 만료 임박 알림 발송 완료: {}건", sentCount);

        } catch (Exception e) {
            logger.error("계약서 만료 임박 알림 스케줄러 실행 중 오류 발생", e);
        }
    }
}
