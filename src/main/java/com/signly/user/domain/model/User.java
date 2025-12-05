package com.signly.user.domain.model;

import com.signly.common.domain.AggregateRoot;
import com.signly.common.exception.ValidationException;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

public class User extends AggregateRoot {

    @Getter
    private UserId userId;
    @Getter
    private Email email;
    private EncodedPassword encodedPassword;
    @Getter
    private String name;
    @Getter
    private Company company;
    @Getter
    private UserType userType;
    @Getter
    private UserStatus status;
    private boolean emailVerified;
    private VerificationToken verificationToken;

    protected User() {
        super();
    }

    private User(
            UserId userId,
            Email email,
            EncodedPassword encodedPassword,
            String name,
            Company company,
            UserType userType,
            UserStatus status,
            boolean emailVerified,
            VerificationToken verificationToken,
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
        this.emailVerified = emailVerified;
        this.verificationToken = verificationToken;
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
        EncodedPassword encodedPassword = EncodedPassword.from(password, passwordEncoder);
        UserStatus status = UserStatus.PENDING;  // 이메일 인증 대기 상태로 생성
        boolean emailVerified = false;
        VerificationToken verificationToken = null;  // 별도로 생성해야 함

        return new User(
                userId,
                email,
                encodedPassword,
                name,
                company,
                userType,
                status,
                emailVerified,
                verificationToken,
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
            boolean emailVerified,
            String verificationTokenValue,
            LocalDateTime verificationTokenExpiry,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        EncodedPassword password = EncodedPassword.of(encodedPassword);
        VerificationToken token = null;
        if (verificationTokenValue != null && verificationTokenExpiry != null) {
            token = VerificationToken.of(verificationTokenValue, verificationTokenExpiry);
        }
        
        return new User(
                userId,
                email,
                password,
                name,
                company,
                userType,
                status,
                emailVerified,
                token,
                createdAt,
                updatedAt
        );
    }

    public boolean validatePassword(
            Password password,
            PasswordEncoder passwordEncoder
    ) {
        return this.encodedPassword.matches(password, passwordEncoder);
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

    public void updateProfile(
            String name,
            Company company
    ) {
        validateName(name);
        this.name = name;
        this.company = company != null ? company : Company.empty();
        updateTimestamp();
    }

    public void changePassword(
            Password oldPassword,
            Password newPassword,
            PasswordEncoder passwordEncoder
    ) {
        if (!validatePassword(oldPassword, passwordEncoder)) {
            throw new ValidationException("기존 비밀번호가 일치하지 않습니다");
        }
        this.encodedPassword = EncodedPassword.from(newPassword, passwordEncoder);
        updateTimestamp();
    }

    public void resetPassword(
            Password newPassword,
            PasswordEncoder passwordEncoder
    ) {
        this.encodedPassword = EncodedPassword.from(newPassword, passwordEncoder);
        updateTimestamp();
    }

    private static void validateCreateParameters(
            Email email,
            Password password,
            String name,
            UserType userType
    ) {
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

    /**
     * 이메일 인증 토큰 생성
     * - 새로운 인증 토큰 생성 및 PENDING 상태로 전환
     * 
     * @return 생성된 인증 토큰
     */
    public VerificationToken generateVerificationToken() {
        this.verificationToken = VerificationToken.generate();
        this.status = UserStatus.PENDING;
        this.emailVerified = false;
        updateTimestamp();
        return this.verificationToken;
    }

    /**
     * 이메일 인증 처리 (멱등성 보장)
     * - 이미 인증된 경우 성공 처리 (예외 없이 반환)
     * - 토큰 검증 후 ACTIVE 상태로 전환
     * 
     * @param token 인증 토큰
     */
    public void verifyEmail(String token) {
        // 멱등성 보장: 이미 인증된 경우 성공 처리
        if (this.emailVerified && this.status == UserStatus.ACTIVE) {
            return;  // 이미 인증 완료
        }

        // 토큰이 없는 경우
        if (this.verificationToken == null) {
            if (this.emailVerified) {
                return;  // 인증 완료 상태면 성공으로 처리
            }
            throw new ValidationException("인증 토큰이 없습니다");
        }

        // 토큰 검증
        this.verificationToken.validate(token);

        // 인증 처리
        this.emailVerified = true;
        this.status = UserStatus.ACTIVE;
        this.verificationToken = null;  // 인증 완료 후 토큰 제거
        updateTimestamp();
    }

    /**
     * 이메일 인증 여부 확인
     * 
     * @return 인증 완료 여부
     */
    public boolean isEmailVerified() {
        return this.emailVerified;
    }

    /**
     * 로그인 가능 여부 확인
     * 
     * @return 로그인 가능 여부 (ACTIVE + 이메일 인증 완료)
     */
    public boolean canLogin() {
        return this.status == UserStatus.ACTIVE && this.emailVerified;
    }

    /**
     * 인증 토큰 값 반환 (영속성 계층 전용)
     * 
     * @return 인증 토큰 값 (없으면 null)
     */
    public String getVerificationTokenValue() {
        return verificationToken != null ? verificationToken.getValue() : null;
    }

    /**
     * 인증 토큰 만료 시간 반환 (영속성 계층 전용)
     * 
     * @return 만료 시간 (없으면 null)
     */
    public LocalDateTime getVerificationTokenExpiry() {
        return verificationToken != null ? verificationToken.getExpiryTime() : null;
    }

    /**
     * 인코딩된 비밀번호 값을 반환
     * 주로 영속성 계층에서 사용하며, 도메인 로직에서는 직접 사용을 최소화해야 함
     *
     * @return 인코딩된 비밀번호 문자열
     */
    public String getEncodedPassword() {
        return encodedPassword.value();
    }
}