package com.signly.user.infrastructure.persistence.mapper;

import com.signly.user.domain.model.*;
import com.signly.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserEntity toEntity(User user) {
        Company company = user.getCompany();
        return new UserEntity(
                user.getUserId().getValue(),
                user.getEmail().getValue(),
                user.getEncodedPassword(),
                user.getName(),
                company != null ? company.getName() : null,
                company != null ? company.getPhone() : null,
                company != null ? company.getAddress() : null,
                user.getUserType(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public User toDomain(UserEntity entity) {
        Company company = Company.of(
                entity.getCompanyName(),
                entity.getBusinessPhone(),
                entity.getBusinessAddress()
        );

        return User.restore(
                UserId.of(entity.getUserId()),
                Email.of(entity.getEmail()),
                entity.getPassword(),
                entity.getName(),
                company,
                entity.getUserType(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void updateEntity(UserEntity entity, User user) {
        Company company = user.getCompany();
        entity.setPassword(user.getEncodedPassword());
        entity.setName(user.getName());
        entity.setCompanyName(company != null ? company.getName() : null);
        entity.setBusinessPhone(company != null ? company.getPhone() : null);
        entity.setBusinessAddress(company != null ? company.getAddress() : null);
        entity.setStatus(user.getStatus());
        entity.setUpdatedAt(user.getUpdatedAt());
    }
}