package com.signly.user.application;

import com.signly.common.exception.ValidationException;
import com.signly.user.domain.model.*;
import com.signly.user.domain.repository.PasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordHistoryService {

    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 재사용 여부 확인
     * - 90일 이내 최근 3개 비밀번호와 비교
     *
     * @param userId 사용자 ID
     * @param newPassword 새 비밀번호
     * @throws ValidationException 재사용 시도 시
     */
    @Transactional(readOnly = true)
    public void validatePasswordReuse(UserId userId, Password newPassword) {
        List<PasswordHistory> recentHistory = passwordHistoryRepository
                .findRecentByUserIdWithin90Days(userId, 3);

        for (PasswordHistory history : recentHistory) {
            if (history.matches(newPassword, passwordEncoder)) {
                log.warn("Password reuse attempt detected for user: {}", userId.value());
                throw new ValidationException(
                        "최근 90일 이내 사용한 비밀번호는 재사용할 수 없습니다. " +
                                "다른 비밀번호를 선택해주세요."
                );
            }
        }

        log.debug("Password reuse validation passed for user: {}", userId.value());
    }

    /**
     * 비밀번호 이력 저장
     *
     * @param userId 사용자 ID
     * @param encodedPassword 암호화된 비밀번호
     * @param ipAddress IP 주소
     * @param userAgent User-Agent
     */
    @Transactional
    public void savePasswordHistory(
            UserId userId,
            EncodedPassword encodedPassword,
            String ipAddress,
            String userAgent
    ) {
        PasswordHistory history = PasswordHistory.create(
                userId,
                encodedPassword,
                ipAddress,
                userAgent
        );

        passwordHistoryRepository.save(history);
        log.info("Password history saved for user: {}", userId.value());
    }

    /**
     * 90일 이전 이력 삭제 (배치 작업용)
     *
     * @return 삭제된 레코드 수
     */
    @Transactional
    public int cleanupOldHistory() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        int deleted = passwordHistoryRepository.deleteOlderThan(cutoffDate);
        log.info("Deleted {} old password history records (older than {})", deleted, cutoffDate);
        return deleted;
    }
}
