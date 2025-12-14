package com.signly.user.infrastructure.persistence.mapper;

import com.signly.common.encryption.AesEncryptionService;
import com.signly.user.domain.model.Company;
import com.signly.user.domain.model.Email;
import com.signly.user.domain.model.User;
import com.signly.user.domain.model.UserId;
import com.signly.user.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEntityMapper {

    private final AesEncryptionService encryptionService;

    public UserEntity toEntity(User user) {
        Company company = user.getCompany();

        // 이메일 해시 생성 (Blind Index)
        String emailHash = encryptionService.hashEmail(user.getEmail().value());

        UserEntity entity = new UserEntity(
                user.getUserId().value(),
                user.getEmail().value(),
                emailHash,  // 해시 추가!
                user.getEncodedPassword(),
                user.getName(),
                company != null ? company.name() : null,
                company != null ? company.phone() : null,
                company != null ? company.address() : null,
                user.getUserType(),
                user.getStatus(),
                user.isEmailVerified(),
                user.getVerificationTokenValue(),
                user.getVerificationTokenExpiry(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
        return entity;
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
                entity.isEmailVerified(),
                entity.getVerificationToken(),
                entity.getVerificationTokenExpiry(),
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
        entity.setEmailVerified(user.isEmailVerified());
        entity.setVerificationToken(user.getVerificationTokenValue());
        entity.setVerificationTokenExpiry(user.getVerificationTokenExpiry());
        entity.setUpdatedAt(user.getUpdatedAt());
    }
}