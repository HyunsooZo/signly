package com.deally.user.infrastructure.persistence.mapper;

import com.deally.user.domain.model.Company;
import com.deally.user.domain.model.Email;
import com.deally.user.domain.model.User;
import com.deally.user.domain.model.UserId;
import com.deally.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserEntity toEntity(User user) {
        Company company = user.getCompany();
        return new UserEntity(
                user.getUserId().value(),
                user.getEmail().value(),
                user.getEncodedPassword(),
                user.getName(),
                company != null ? company.name() : null,
                company != null ? company.phone() : null,
                company != null ? company.address() : null,
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

    public void updateEntity(
            UserEntity entity,
            User user
    ) {
        Company company = user.getCompany();
        entity.setPassword(user.getEncodedPassword());
        entity.setName(user.getName());
        entity.setCompanyName(company != null ? company.name() : null);
        entity.setBusinessPhone(company != null ? company.phone() : null);
        entity.setBusinessAddress(company != null ? company.address() : null);
        entity.setStatus(user.getStatus());
        entity.setUpdatedAt(user.getUpdatedAt());
    }
}