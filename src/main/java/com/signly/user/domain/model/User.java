package com.signly.user.domain.model;

import com.signly.common.domain.AggregateRoot;
import com.signly.common.exception.ValidationException;
import com.signly.user.application.PasswordHistoryService;
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
    @Getter
    private int failedLoginAttempts;
    private LocalDateTime lastFailedLoginAt;
    private LocalDateTime accountLockedAt;
    private AccountUnlockToken unlockToken;

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
            int failedLoginAttempts,
            LocalDateTime lastFailedLoginAt,
            LocalDateTime accountLockedAt,
            AccountUnlockToken unlockToken,
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
        this.failedLoginAttempts = failedLoginAttempts;
        this.lastFailedLoginAt = lastFailedLoginAt;
        this.accountLockedAt = accountLockedAt;
        this.unlockToken = unlockToken;
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
                0,  // failedLoginAttempts
                null,  // lastFailedLoginAt
                null,  // accountLockedAt
                null,  // unlockToken
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
            int failedLoginAttempts,
            LocalDateTime lastFailedLoginAt,
            LocalDateTime accountLockedAt,
            String unlockTokenValue,
            LocalDateTime unlockTokenExpiry,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        EncodedPassword password = EncodedPassword.of(encodedPassword);
        VerificationToken token = null;
        if (verificationTokenValue != null && verificationTokenExpiry != null) {
            token = VerificationToken.of(verificationTokenValue, verificationTokenExpiry);
        }
        
        AccountUnlockToken unlockToken = null;
        if (unlockTokenValue != null && unlockTokenExpiry != null) {
            unlockToken = AccountUnlockToken.of(unlockTokenValue, unlockTokenExpiry);
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
                failedLoginAttempts,
                lastFailedLoginAt,
                accountLockedAt,
                unlockToken,
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

    /**
     * 프로필 완성 여부 확인
     * - 기업 정보가 필수적으로 있어야 함
     *
     * @return 프로필 완성 여부
     */
    public boolean isProfileComplete() {
        if (company == null || company.isEmpty()) {
            return false;
        }
        return !company.name().trim().isEmpty();
    }

    // ========== 계정 잠금 관련 메서드 ==========

    /**
     * 로그인 실패 기록
     * - 실패 횟수 증가
     * - 5회 실패 시 자동 잠금
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        this.lastFailedLoginAt = LocalDateTime.now();
        updateTimestamp();

        // 5회 실패 시 계정 잠금
        if (this.failedLoginAttempts >= 5) {
            this.lockAccount();
        }
    }

    /**
     * 로그인 성공 시 실패 횟수 초기화
     */
    public void resetLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lastFailedLoginAt = null;
        updateTimestamp();
    }

    /**
     * 계정 잠금
     * - LOCKED 상태로 변경
     * - 잠금 시각 기록
     * - 해제 토큰 생성
     */
    public void lockAccount() {
        if (this.status == UserStatus.LOCKED) {
            return;  // 이미 잠긴 계정은 재잠금 방지
        }

        this.status = UserStatus.LOCKED;
        this.accountLockedAt = LocalDateTime.now();
        this.unlockToken = AccountUnlockToken.generate();
        updateTimestamp();
    }

    /**
     * 계정 해제 (토큰 검증 후)
     * - 토큰 검증
     * - ACTIVE 상태로 복구
     * - 임시 비밀번호 반환
     *
     * @param token 계정 해제 토큰
     * @return 암호화 전 임시 비밀번호
     */
    public String unlockAccount(String token) {
        if (this.status != UserStatus.LOCKED) {
            throw new ValidationException("잠긴 계정이 아닙니다");
        }

        if (this.unlockToken == null) {
            throw new ValidationException("해제 토큰이 없습니다. 고객지원에 문의해주세요.");
        }

        // 토큰 검증 (만료 + 일치)
        this.unlockToken.validate(token);

        // 임시 비밀번호 생성
        String tempPassword = generateTemporaryPassword();

        // 계정 해제
        this.status = UserStatus.ACTIVE;
        this.accountLockedAt = null;
        this.unlockToken = null;
        this.resetLoginAttempts();
        updateTimestamp();

        return tempPassword;
    }

    /**
     * 계정 잠금 여부 확인
     */
    public boolean isLocked() {
        return this.status == UserStatus.LOCKED;
    }

    /**
     * 남은 로그인 시도 횟수 반환
     */
    public int getRemainingLoginAttempts() {
        return Math.max(0, 5 - this.failedLoginAttempts);
    }

    /**
     * 임시 비밀번호 생성
     * - 영문 대소문자 + 숫자 + 특수문자 조합
     * - 8자리 랜덤
     */
    private String generateTemporaryPassword() {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "@$!%*#?&";

        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder password = new StringBuilder(8);

        // 최소 1개씩 보장
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // 나머지 4자리 랜덤
        String allChars = uppercase + lowercase + digits + special;
        for (int i = 0; i < 4; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 셔플
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    /**
     * 계정 해제 토큰 값 반환 (영속성 계층 전용)
     */
    public String getUnlockTokenValue() {
        return unlockToken != null ? unlockToken.getValue() : null;
    }

    /**
     * 계정 해제 토큰 만료 시간 반환 (영속성 계층 전용)
     */
    public LocalDateTime getUnlockTokenExpiry() {
        return unlockToken != null ? unlockToken.getExpiryTime() : null;
    }

    /**
     * 마지막 로그인 실패 시각 반환
     */
    public LocalDateTime getLastFailedLoginAt() {
        return lastFailedLoginAt;
    }

    /**
     * 계정 잠금 시각 반환
     */
    public LocalDateTime getAccountLockedAt() {
        return accountLockedAt;
    }
}