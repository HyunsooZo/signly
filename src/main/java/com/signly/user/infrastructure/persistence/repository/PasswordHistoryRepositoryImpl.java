package com.signly.user.infrastructure.persistence.repository;

import com.signly.user.domain.model.PasswordHistory;
import com.signly.user.domain.model.UserId;
import com.signly.user.domain.repository.PasswordHistoryRepository;
import com.signly.user.infrastructure.persistence.entity.PasswordHistoryEntity;
import com.signly.user.infrastructure.persistence.mapper.PasswordHistoryEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PasswordHistoryRepositoryImpl implements PasswordHistoryRepository {

    private final PasswordHistoryJpaRepository passwordHistoryJpaRepository;
    private final PasswordHistoryEntityMapper passwordHistoryEntityMapper;

    @Override
    public PasswordHistory save(PasswordHistory passwordHistory) {
        PasswordHistoryEntity entity = passwordHistoryEntityMapper.toEntity(passwordHistory);
        PasswordHistoryEntity savedEntity = passwordHistoryJpaRepository.save(entity);
        return passwordHistoryEntityMapper.toDomain(savedEntity);
    }

    @Override
    public List<PasswordHistory> findRecentByUserIdWithin90Days(UserId userId, int limit) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        List<PasswordHistoryEntity> entities = passwordHistoryJpaRepository
                .findRecentByUserIdWithin90Days(
                        userId.value(),
                        cutoffDate,
                        PageRequest.of(0, limit)
                );

        return entities.stream()
                .map(passwordHistoryEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int deleteOlderThan(LocalDateTime cutoffDate) {
        return passwordHistoryJpaRepository.deleteOlderThan(cutoffDate);
    }
}
