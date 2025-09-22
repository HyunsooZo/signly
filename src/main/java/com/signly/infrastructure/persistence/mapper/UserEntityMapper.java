package com.signly.infrastructure.persistence.mapper;

import com.signly.domain.user.model.*;
import com.signly.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserEntity toEntity(User user) {
        return new UserEntity(
                user.getUserId().getValue(),
                user.getEmail().getValue(),
                user.getEncodedPassword(),
                user.getName(),
                user.getCompanyName(),
                user.getUserType(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public User toDomain(UserEntity entity) {
        return User.restore(
                UserId.of(entity.getUserId()),
                Email.of(entity.getEmail()),
                entity.getPassword(),
                entity.getName(),
                entity.getCompanyName(),
                entity.getUserType(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void updateEntity(UserEntity entity, User user) {
        entity.setPassword(user.getEncodedPassword());
        entity.setName(user.getName());
        entity.setCompanyName(user.getCompanyName());
        entity.setStatus(user.getStatus());
        entity.setUpdatedAt(user.getUpdatedAt());
    }
}