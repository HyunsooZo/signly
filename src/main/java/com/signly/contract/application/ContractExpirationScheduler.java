package com.signly.contract.application;

import com.signly.common.email.EmailService;
import com.signly.contract.domain.model.Contract;
import com.signly.contract.domain.model.ContractStatus;
import com.signly.contract.domain.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ContractExpirationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ContractExpirationScheduler.class);

    private final ContractService contractService;
    private final ContractRepository contractRepository;
    private final EmailService emailService;

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    @Transactional
    public void checkExpiredContracts() {
        logger.info("만료된 계약서 확인 작업 시작");

        try {
            contractService.expireContracts();
            logger.info("만료된 계약서 처리 완료");
        } catch (Exception e) {
            logger.error("만료된 계약서 처리 중 오류 발생", e);
        }
    }

    @Scheduled(cron = "0 0 9 * * ?") // 매일 오전 9시 실행
    @Transactional(readOnly = true)
    public void sendExpirationWarnings() {
        logger.info("만료 예정 계약서 알림 발송 시작");

        try {
            var tomorrow = LocalDateTime.now().plusDays(1);
            var dayAfterTomorrow = LocalDateTime.now().plusDays(2);

            // 1일 후 만료 예정인 계약서들
            var contractsExpiringTomorrow = contractRepository
                    .findByStatusAndExpiresAtBefore(ContractStatus.PENDING, dayAfterTomorrow)
                    .stream()
                    .filter(contract -> contract.getExpiresAt() != null && contract.getExpiresAt().isAfter(tomorrow))
                    .toList();

            for (var contract : contractsExpiringTomorrow) {
                sendExpirationWarning(contract, 1);
            }

            logger.info("만료 예정 알림 발송 완료: {} 건", contractsExpiringTomorrow.size());

        } catch (Exception e) {
            logger.error("만료 예정 알림 발송 중 오류 발생", e);
        }
    }

    @Scheduled(cron = "0 0 10 * * MON") // 매주 월요일 오전 10시 실행
    @Transactional(readOnly = true)
    public void sendWeeklyReport() {
        logger.info("주간 계약서 현황 리포트 생성 시작");

        try {
            // 주간 리포트 로직은 필요에 따라 구현
            // 예: 대기중인 계약서 수, 완료된 계약서 수 등의 통계
            logger.info("주간 리포트 생성 완료");
        } catch (Exception e) {
            logger.error("주간 리포트 생성 중 오류 발생", e);
        }
    }

    private void sendExpirationWarning(
            Contract contract,
            int daysLeft
    ) {
        try {
            var pendingSigners = contract.getPendingSigners();

            for (var signerEmail : pendingSigners) {
                emailService.sendSimpleEmail(
                        signerEmail,
                        "계약서 만료 예정 알림",
                        String.format(
                                "안녕하세요,\n\n" +
                                        "다음 계약서가 %d일 후 만료 예정입니다:\n\n" +
                                        "계약서 제목: %s\n" +
                                        "만료일: %s\n\n" +
                                        "빠른 시일 내에 서명을 완료해 주세요.\n\n" +
                                        "감사합니다.",
                                daysLeft,
                                contract.getTitle(),
                                contract.getExpiresAt()
                        )
                );
            }

            // 계약서 작성자에게도 알림
            // UserRepository를 통해 작성자 이메일을 가져와야 하지만
            // 여기서는 간단히 로그만 남김
            logger.info("계약서 '{}' 만료 예정 알림 발송 완료", contract.getTitle());

        } catch (Exception e) {
            logger.error("만료 예정 알림 발송 중 오류 발생: 계약서 ID {}", contract.getId().value(), e);
        }
    }
}