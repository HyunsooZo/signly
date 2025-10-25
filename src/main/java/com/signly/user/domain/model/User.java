package com.signly.user.domain.model;

import com.signly.common.domain.AggregateRoot;
import com.signly.common.exception.ValidationException;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Getter
public class User extends AggregateRoot {

    private UserId userId;
    private Email email;
    private String encodedPassword;
    private String name;
    private Company company;
    private UserType userType;
    private UserStatus status;

    protected User() {
        super();
    }

    private User(
            UserId userId,
            Email email,
            String encodedPassword,
            String name,
            Company company,
            UserType userType,
            UserStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.userId = userId;
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.name = name;
        this.company = company != null ? company : Company.empty();
        this.userType = userType;
        this.status = status;
    }

    public static User create(
            Email email,
            Password password,
            String name,
            Company company,
            UserType userType,
            PasswordEncoder passwordEncoder
    ) {
        validateCreateParameters(email, password, name, userType);

        UserId userId = UserId.generate();
        String encodedPassword = passwordEncoder.encode(password.getValue());
        UserStatus status = UserStatus.ACTIVE;

        return new User(
                userId,
                email,
                encodedPassword,
                name,
                company,
                userType,
                status,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static User restore(
            UserId userId,
            Email email,
            String encodedPassword,
            String name,
            Company company,
            UserType userType,
            UserStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new User(
                userId,
                email,
                encodedPassword,
                name,
                company,
                userType,
                status,
                createdAt,
                updatedAt
        );
    }

    public boolean validatePassword(
            Password password,
            PasswordEncoder passwordEncoder
    ) {
        return passwordEncoder.matches(password.getValue(), this.encodedPassword);
    }

    public boolean canCreateTemplate() {
        return this.userType == UserType.OWNER && this.status == UserStatus.ACTIVE;
    }

    public boolean canCreateContract() {
        return this.status == UserStatus.ACTIVE;
    }

    public void activate() {
        if (this.status == UserStatus.SUSPENDED) {
            throw new ValidationException("정지된 사용자는 활성화할 수 없습니다");
        }
        this.status = UserStatus.ACTIVE;
        updateTimestamp();
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        updateTimestamp();
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
        updateTimestamp();
    }

    public void updateProfile(String name, Company company) {
        validateName(name);
        this.name = name;
        this.company = company != null ? company : Company.empty();
        updateTimestamp();
    }

    public void changePassword(Password oldPassword, Password newPassword, PasswordEncoder passwordEncoder) {
        if (!validatePassword(oldPassword, passwordEncoder)) {
            throw new ValidationException("기존 비밀번호가 일치하지 않습니다");
        }
        this.encodedPassword = passwordEncoder.encode(newPassword.getValue());
        updateTimestamp();
    }

    public void resetPassword(Password newPassword, PasswordEncoder passwordEncoder) {
        this.encodedPassword = passwordEncoder.encode(newPassword.getValue());
        updateTimestamp();
    }

    private static void validateCreateParameters(Email email, Password password, String name, UserType userType) {
        if (email == null) {
            throw new ValidationException("이메일은 필수입니다");
        }
        if (password == null) {
            throw new ValidationException("비밀번호는 필수입니다");
        }
        validateName(name);
        if (userType == null) {
            throw new ValidationException("사용자 타입은 필수입니다");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("이름은 필수입니다");
        }
        if (name.trim().length() > 100) {
            throw new ValidationException("이름은 100자를 초과할 수 없습니다");
        }
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}