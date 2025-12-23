package com.signly.user.application;

import com.signly.common.audit.application.AuditLogService;
import com.signly.common.audit.application.dto.CreateAuditLogCommand;
import com.signly.common.audit.domain.model.AuditAction;
import com.signly.common.audit.domain.model.AuditDetails;
import com.signly.common.audit.domain.model.EntityType;
import com.signly.user.domain.model.Email;
import com.signly.user.domain.model.User;
import com.signly.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 로그인 시도 추적 서비스
 * - 로그인 실패 횟수 관리
 * - 5회 실패 시 계정 자동 잠금
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final AccountLockService accountLockService;
    private final AuditLogService auditLogService;

    /**
     * 로그인 실패 기록
     * - 실패 횟수 증가
     * - 5회 실패 시 자동 잠금 + 이메일 발송
     *
     * @param email 이메일
     * @param ipAddress IP 주소
     */
    @Transactional
    public void recordFailedAttempt(String email, String ipAddress) {
        Email emailObj = Email.of(email);
        User user = userRepository.findByEmail(emailObj).orElse(null);

        if (user == null) {
            // 존재하지 않는 이메일은 무시 (보안상 정보 노출 방지)
            logFailedAttempt(null, email, ipAddress, "USER_NOT_FOUND");
            return;
        }

        // 이미 잠긴 계정은 더 이상 카운트 안함
        if (user.isLocked()) {
            log.debug("Account already locked for user: {}", user.getUserId().value());
            return;
        }

        // 로그인 실패 기록 (5회 시 자동 잠금)
        user.recordFailedLogin();
        userRepository.save(user);

        // 감사 로그 기록
        if (user.isLocked()) {
            logAccountLocked(user, ipAddress);
            // 계정 잠금 이메일 발송
            accountLockService.sendAccountLockedEmail(user);
        } else {
            logFailedAttempt(user.getUserId().value(), email, ipAddress, 
                String.format("INVALID_PASSWORD (attempts: %d/5)", user.getFailedLoginAttempts()));
        }

        log.info("Login attempt failed for user: {} (attempts: {}/5)", 
            user.getUserId().value(), user.getFailedLoginAttempts());
    }

    /**
     * 로그인 성공 시 실패 횟수 초기화
     *
     * @param email 이메일
     */
    @Transactional
    public void resetAttempts(String email) {
        Email emailObj = Email.of(email);
        userRepository.findByEmail(emailObj).ifPresent(user -> {
            if (user.getFailedLoginAttempts() > 0) {
                user.resetLoginAttempts();
                userRepository.save(user);
                log.info("Login attempts reset for user: {}", user.getUserId().value());
            }
        });
    }

    /**
     * 계정 잠금 여부 확인
     *
     * @param email 이메일
     * @return 잠금 여부
     */
    @Transactional(readOnly = true)
    public boolean isAccountLocked(String email) {
        Email emailObj = Email.of(email);
        return userRepository.findByEmail(emailObj)
                .map(User::isLocked)
                .orElse(false);
    }

    /**
     * 남은 로그인 시도 횟수 반환
     *
     * @param email 이메일
     * @return 남은 시도 횟수 (0~5)
     */
    @Transactional(readOnly = true)
    public int getRemainingAttempts(String email) {
        Email emailObj = Email.of(email);
        return userRepository.findByEmail(emailObj)
                .map(User::getRemainingLoginAttempts)
                .orElse(5);
    }

    /**
     * 잠금 해제 이메일 재전송
     *
     * @param email 이메일
     * @param baseUrl 기본 URL
     */
    @Transactional
    public void resendUnlockEmail(String email, String baseUrl) {
        Email emailObj = Email.of(email);
        User user = userRepository.findByEmail(emailObj)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (!user.isLocked()) {
            throw new IllegalArgumentException("잠긴 계정이 아닙니다");
        }

        // 잠금 해제 이메일 재전송
        accountLockService.sendAccountLockedEmail(user);

        log.info("Account unlock email resent for user: {}", user.getUserId().value());
    }

    // ========== Private Helper Methods ==========

    private void logFailedAttempt(String userId, String email, String ipAddress, String reason) {
        auditLogService.createAuditLog(new CreateAuditLogCommand(
                EntityType.USER,
                userId,
                AuditAction.LOGIN_FAILED,
                userId != null ? com.signly.user.domain.model.UserId.of(userId) : null,
                ipAddress,
                null,
                AuditDetails.of(null, null, Map.of("email", email, "reason", reason))
        ));
    }

    private void logAccountLocked(User user, String ipAddress) {
        auditLogService.createAuditLog(new CreateAuditLogCommand(
                EntityType.USER,
                user.getUserId().value(),
                AuditAction.ACCOUNT_LOCKED,
                user.getUserId(),
                ipAddress,
                null,
                AuditDetails.of(null, null, Map.of(
                        "attempts", user.getFailedLoginAttempts(),
                        "locked_at", user.getAccountLockedAt().toString()
                ))
        ));
    }
}
