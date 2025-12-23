package com.signly.user.application;

import com.signly.common.audit.application.AuditLogService;
import com.signly.common.audit.application.dto.CreateAuditLogCommand;
import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.AuditDetails;
import com.signly.common.audit.domain.model.EntityType;
import com.signly.common.exception.ValidationException;
import com.signly.notification.application.EmailNotificationService;
import com.signly.notification.domain.model.EmailOutbox;
import com.signly.notification.domain.model.EmailTemplate;
import com.signly.notification.domain.repository.EmailOutboxRepository;
import com.signly.notification.domain.event.EmailOutboxCreatedEvent;
import com.signly.user.domain.model.Password;
import com.signly.user.domain.model.User;
import com.signly.user.domain.repository.UserRepository;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 계정 잠금 관리 서비스
 * - 계정 잠금 이메일 발송
 * - 계정 해제 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountLockService {

    private final UserRepository userRepository;
    private final EmailOutboxRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 계정 잠금 이메일 발송
     *
     * @param user 잠긴 사용자
     */
    @Transactional
    public void sendAccountLockedEmail(User user) {
        try {
            String unlockUrl = buildUnlockUrl(user.getUnlockTokenValue());

            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", user.getName());
            variables.put("unlockUrl", unlockUrl);
            variables.put("expiryHours", "24");
            variables.put("lockedAt", user.getAccountLockedAt());

            EmailOutbox outbox = EmailOutbox.create(
                    EmailTemplate.ACCOUNT_LOCKED,
                    user.getEmail().value(),
                    user.getName(),
                    variables
            );

            EmailOutbox saved = outboxRepository.save(outbox);
            eventPublisher.publishEvent(new EmailOutboxCreatedEvent(saved.getId()));

            log.info("Account locked email sent to outbox for user: {}", user.getUserId().value());

        } catch (Exception e) {
            log.error("Failed to send account locked email for user: {}", user.getUserId().value(), e);
        }
    }

    /**
     * 계정 해제 처리
     * - 토큰 검증
     * - 임시 비밀번호 생성 및 설정
     * - 해제 완료 이메일 발송
     *
     * @param token 계정 해제 토큰
     * @param ipAddress IP 주소
     * @return 사용자 이메일
     */
    @Transactional
    public String unlockAccount(String token, String ipAddress) {
        // 토큰으로 사용자 조회
        User user = userRepository.findByUnlockToken(token)
                .orElseThrow(() -> new ValidationException("유효하지 않은 계정 해제 링크입니다"));

        // 계정 해제 및 임시 비밀번호 생성
        String tempPassword = user.unlockAccount(token);

        // 임시 비밀번호 암호화 및 저장
        Password newPassword = Password.of(tempPassword);
        user.resetPassword(newPassword, passwordEncoder);

        userRepository.save(user);

        // 감사 로그 기록
auditLogService.createAuditLog(new CreateAuditLogCommand(
                EntityType.USER,
                user.getUserId().value(),
                AuditAction.ACCOUNT_UNLOCKED,
                user.getUserId(),
                ipAddress,
                null,
                AuditDetails.of(null, null, Map.of(
                        "unlocked_at", LocalDateTime.now().toString(),
                        "temp_password_generated", true
                ))
        ));

        // 임시 비밀번호 이메일 발송
        sendTemporaryPasswordEmail(user, tempPassword);

        log.info("Account unlocked successfully for user: {}", user.getUserId().value());

        return user.getEmail().value();
    }

    /**
     * 임시 비밀번호 이메일 발송
     *
     * @param user 사용자
     * @param tempPassword 임시 비밀번호
     */
    private void sendTemporaryPasswordEmail(User user, String tempPassword) {
        try {
            String loginUrl = buildLoginUrl();

            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", user.getName());
            variables.put("tempPassword", tempPassword);
            variables.put("loginUrl", loginUrl);

            EmailOutbox outbox = EmailOutbox.create(
                    EmailTemplate.ACCOUNT_UNLOCKED,
                    user.getEmail().value(),
                    user.getName(),
                    variables
            );

            EmailOutbox saved = outboxRepository.save(outbox);
            eventPublisher.publishEvent(new EmailOutboxCreatedEvent(saved.getId()));

            log.info("Temporary password email sent to outbox for user: {}", user.getUserId().value());

        } catch (Exception e) {
            log.error("Failed to send temporary password email for user: {}", user.getUserId().value(), e);
        }
    }

    private String buildUnlockUrl(String token) {
        return String.format("%s/unlock-account?token=%s", baseUrl, token);
    }

    private String buildLoginUrl() {
        return String.format("%s/login", baseUrl);
    }
}
