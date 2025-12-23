package com.signly.user.infrastructure.persistence.entity;

import com.signly.common.encryption.StringEncryptionConverter;
import com.signly.user.domain.model.UserStatus;
import com.signly.user.domain.model.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_status", columnList = "status"),
        @Index(name = "idx_user_type", columnList = "user_type"),
        @Index(name = "idx_user_status_type", columnList = "status, user_type"),
        @Index(name = "idx_user_created_at", columnList = "created_at"),
        @Index(name = "idx_verification_token", columnList = "verification_token"),
        @Index(name = "idx_unlock_token", columnList = "unlock_token"),
        @Index(name = "idx_account_locked", columnList = "status, account_locked_at")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @Column(name = "user_id", length = 26)
    private String userId;

    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "email", length = 500, nullable = false)
    private String email;

    @Column(name = "email_hash", length = 64, unique = true, nullable = false)
    private String emailHash;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "company_name", length = 200)
    private String companyName;

    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "business_phone", length = 500)
    private String businessPhone;

    @Convert(converter = StringEncryptionConverter.class)
    @Column(name = "business_address", length = 1000)
    private String businessAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 20, nullable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private UserStatus status;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "verification_token", length = 255)
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "last_failed_login_at")
    private LocalDateTime lastFailedLoginAt;

    @Column(name = "account_locked_at")
    private LocalDateTime accountLockedAt;

    @Column(name = "unlock_token", length = 255)
    private String unlockToken;

    @Column(name = "unlock_token_expiry")
    private LocalDateTime unlockTokenExpiry;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
