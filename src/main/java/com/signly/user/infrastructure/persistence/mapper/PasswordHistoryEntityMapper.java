package com.signly.user.infrastructure.persistence.mapper;

import com.signly.user.domain.model.PasswordHistory;
import com.signly.user.domain.model.UserId;
import com.signly.user.infrastructure.persistence.entity.PasswordHistoryEntity;
import org.springframework.stereotype.Component;

@Component
public class PasswordHistoryEntityMapper {

    public PasswordHistoryEntity toEntity(PasswordHistory passwordHistory) {
        return new PasswordHistoryEntity(
                passwordHistory.getId(),
                passwordHistory.getUserId().value(),
                passwordHistory.getPasswordHashValue(),
                passwordHistory.getChangedAt(),
                passwordHistory.getIpAddress(),
                passwordHistory.getUserAgent(),
                passwordHistory.getCreatedAt()
        );
    }

    public PasswordHistory toDomain(PasswordHistoryEntity entity) {
        return PasswordHistory.restore(
                entity.getId(),
                UserId.of(entity.getUserId()),
                entity.getPasswordHash(),
                entity.getChangedAt(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getCreatedAt()
        );
    }
}
