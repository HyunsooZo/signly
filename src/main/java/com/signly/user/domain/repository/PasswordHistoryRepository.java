package com.signly.user.domain.repository;

import com.signly.user.domain.model.PasswordHistory;
import com.signly.user.domain.model.UserId;

import java.time.LocalDateTime;
import java.util.List;

public interface PasswordHistoryRepository {

    /**
     * 비밀번호 이력 저장
     */
    PasswordHistory save(PasswordHistory passwordHistory);

    /**
     * 사용자의 최근 N개 비밀번호 이력 조회 (90일 이내만)
     */
    List<PasswordHistory> findRecentByUserIdWithin90Days(UserId userId, int limit);

    /**
     * 특정 날짜 이전의 이력 삭제
     */
    int deleteOlderThan(LocalDateTime cutoffDate);
}
