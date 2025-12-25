package com.signly.user.domain.model;

import com.signly.common.exception.ValidationException;
import com.signly.user.application.PasswordHistoryService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserTest {

    private PasswordEncoder passwordEncoder;
    private Email validEmail;
    private Password validPassword;
    private Company testCompany;
    @Mock
    private PasswordHistoryService passwordHistoryService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        validEmail = Email.of("test@example.com");
        validPassword = Password.of("Test123!@#");
        testCompany = Company.of("테스트 회사", "010-1234-5678", "서울특별시");
    }

    @Test
    @DisplayName("유효한 정보로 사용자를 생성할 수 있다")
    void createUser_WithValidInfo_ShouldSuccess() {
        User user = User.create(
                validEmail,
                validPassword,
                "홍길동",
                testCompany,
                UserType.OWNER,
                passwordEncoder
        );

        assertThat(user.getUserId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo(validEmail);
        assertThat(user.getName()).isEqualTo("홍길동");
        assertThat(user.getCompany()).isEqualTo(testCompany);
        assertThat(user.getUserType()).isEqualTo(UserType.OWNER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);  // 이메일 인증 대기 상태
        assertThat(user.isEmailVerified()).isFalse();  // 아직 인증되지 않음
        assertThat(user.isActive()).isFalse();  // PENDING 상태라 비활성
    }

    @Test
    @DisplayName("null 이메일로 사용자 생성 시 예외가 발생한다")
    void createUser_WithNullEmail_ShouldThrowException() {
        assertThatThrownBy(() ->
                User.create(null, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder)
        ).isInstanceOf(ValidationException.class)
         .hasMessage("이메일은 필수입니다");
    }

    @Test
    @DisplayName("null 비밀번호로 사용자 생성 시 예외가 발생한다")
    void createUser_WithNullPassword_ShouldThrowException() {
        assertThatThrownBy(() ->
                User.create(validEmail, null, "홍길동", testCompany, UserType.OWNER, passwordEncoder)
        ).isInstanceOf(ValidationException.class)
         .hasMessage("비밀번호는 필수입니다");
    }

    @Test
    @DisplayName("빈 이름으로 사용자 생성 시 예외가 발생한다")
    void createUser_WithEmptyName_ShouldThrowException() {
        assertThatThrownBy(() ->
                User.create(validEmail, validPassword, "", testCompany, UserType.OWNER, passwordEncoder)
        ).isInstanceOf(ValidationException.class)
         .hasMessage("이름은 필수입니다");
    }

    @Test
    @DisplayName("올바른 비밀번호로 검증하면 true를 반환한다")
    void validatePassword_WithCorrectPassword_ShouldReturnTrue() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);

        boolean result = user.validatePassword(validPassword, passwordEncoder);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("잘못된 비밀번호로 검증하면 false를 반환한다")
    void validatePassword_WithIncorrectPassword_ShouldReturnFalse() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);
        Password wrongPassword = Password.of("Wrong123!@#");

        boolean result = user.validatePassword(wrongPassword, passwordEncoder);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("OWNER 타입이고 ACTIVE 상태인 사용자는 템플릿을 생성할 수 있다")
    void canCreateTemplate_OwnerAndActive_ShouldReturnTrue() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);
        
        // 이메일 인증 처리
        var token = user.generateVerificationToken();
        user.verifyEmail(token.getValue());

        boolean result = user.canCreateTemplate();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("CONTRACTOR 타입 사용자는 템플릿을 생성할 수 없다")
    void canCreateTemplate_Contractor_ShouldReturnFalse() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.CONTRACTOR, passwordEncoder);

        boolean result = user.canCreateTemplate();

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("비활성화된 사용자는 템플릿을 생성할 수 없다")
    void canCreateTemplate_InactiveUser_ShouldReturnFalse() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);
        user.deactivate();

        boolean result = user.canCreateTemplate();

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자를 활성화할 수 있다")
    void activate_ShouldChangeStatusToActive() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);
        user.deactivate();

        user.activate();

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("정지된 사용자는 활성화할 수 없다")
    void activate_SuspendedUser_ShouldThrowException() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);
        user.suspend();

        assertThatThrownBy(() -> user.activate())
                .isInstanceOf(ValidationException.class)
                .hasMessage("정지된 사용자는 활성화할 수 없습니다");
    }

    @Test
    @DisplayName("프로필을 수정할 수 있다")
    void updateProfile_ShouldUpdateNameAndCompany() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);

        Company newCompany = Company.of("새로운 회사", "02-0000-1111", "경기도 성남시");

        user.updateProfile("김철수", newCompany);

        assertThat(user.getName()).isEqualTo("김철수");
        assertThat(user.getCompany()).isEqualTo(newCompany);
    }

@Test
    @DisplayName("올바른 기존 비밀번호로 변경하면 성공한다")
    void changePassword_WithCorrectOldPassword_ShouldChangePassword() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);
        Password newPassword = Password.of("NewPass123!@#");

        user.changePassword(validPassword, newPassword, passwordEncoder);

        assertThat(user.validatePassword(newPassword, passwordEncoder)).isTrue();
        assertThat(user.validatePassword(validPassword, passwordEncoder)).isFalse();
    }

    @Test
    @DisplayName("잘못된 기존 비밀번호로 변경 시도하면 예외가 발생한다")
    void changePassword_WithIncorrectOldPassword_ShouldThrowException() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);
        Password wrongPassword = Password.of("Wrong123!@#");
        Password newPassword = Password.of("NewPass123!@#");

        assertThatThrownBy(() -> user.changePassword(wrongPassword, newPassword, passwordEncoder))
                .isInstanceOf(ValidationException.class)
                .hasMessage("기존 비밀번호가 일치하지 않습니다");
    }

    // ========== 이메일 인증 테스트 ==========

    @Test
    @DisplayName("이메일 인증 토큰을 생성할 수 있다")
    void generateVerificationToken_ShouldCreateToken() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);

        var token = user.generateVerificationToken();

        assertThat(token).isNotNull();
        assertThat(token.getValue()).isNotBlank();
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    @DisplayName("이메일 인증에 성공하면 ACTIVE 상태가 된다")
    void verifyEmail_WithValidToken_ShouldActivateUser() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);
        var token = user.generateVerificationToken();

        user.verifyEmail(token.getValue());

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("이메일 인증 멱등성: 이미 인증된 사용자는 재인증해도 예외가 발생하지 않는다")
    void verifyEmail_AlreadyVerified_ShouldNotThrowException() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);
        var token = user.generateVerificationToken();

        user.verifyEmail(token.getValue());
        
        // 두 번째 인증 시도 (멱등성 보장)
        assertThatNoException().isThrownBy(() -> user.verifyEmail(token.getValue()));
        
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("잘못된 토큰으로 이메일 인증 시도하면 예외가 발생한다")
    void verifyEmail_WithInvalidToken_ShouldThrowException() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);
        user.generateVerificationToken();

        assertThatThrownBy(() -> user.verifyEmail("invalid-token"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("유효하지 않은 인증 토큰입니다");
    }

    @Test
    @DisplayName("PENDING 상태인 사용자는 계약을 생성할 수 없다")
    void canCreateContract_PendingUser_ShouldReturnFalse() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);

        boolean result = user.canCreateContract();

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("이메일 인증 후에는 계약을 생성할 수 있다")
    void canCreateContract_AfterVerification_ShouldReturnTrue() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);
        var token = user.generateVerificationToken();
        user.verifyEmail(token.getValue());

        boolean result = user.canCreateContract();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canLogin은 ACTIVE이면서 이메일 인증이 완료된 경우만 true를 반환한다")
    void canLogin_ActiveAndVerified_ShouldReturnTrue() {
        User user = User.create(validEmail, validPassword, "홍길동", testCompany, UserType.OWNER, passwordEncoder);
        
        // PENDING 상태에서는 로그인 불가
        assertThat(user.canLogin()).isFalse();
        
        // 이메일 인증 후 로그인 가능
        var token = user.generateVerificationToken();
        user.verifyEmail(token.getValue());
        
        assertThat(user.canLogin()).isTrue();
    }
}
