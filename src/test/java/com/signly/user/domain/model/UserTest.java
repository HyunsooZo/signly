package com.signly.user.domain.model;

import com.signly.common.exception.ValidationException;
import com.signly.common.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    private PasswordEncoder passwordEncoder;
    private Email validEmail;
    private Password validPassword;

    @BeforeEach
    void setUp() {
        passwordEncoder = new PasswordEncoder();
        validEmail = Email.of("test@example.com");
        validPassword = Password.of("Test123!@#");
    }

    @Test
    @DisplayName("유효한 정보로 사용자를 생성할 수 있다")
    void createUser_WithValidInfo_ShouldSuccess() {
        User user = User.create(
                validEmail,
                validPassword,
                "홍길동",
                "테스트 회사",
                UserType.OWNER,
                passwordEncoder
        );

        assertThat(user.getUserId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo(validEmail);
        assertThat(user.getName()).isEqualTo("홍길동");
        assertThat(user.getCompanyName()).isEqualTo("테스트 회사");
        assertThat(user.getUserType()).isEqualTo(UserType.OWNER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("null 이메일로 사용자 생성 시 예외가 발생한다")
    void createUser_WithNullEmail_ShouldThrowException() {
        assertThatThrownBy(() ->
                User.create(null, validPassword, "홍길동", "테스트 회사", UserType.OWNER, passwordEncoder)
        ).isInstanceOf(ValidationException.class)
         .hasMessage("이메일은 필수입니다");
    }

    @Test
    @DisplayName("null 비밀번호로 사용자 생성 시 예외가 발생한다")
    void createUser_WithNullPassword_ShouldThrowException() {
        assertThatThrownBy(() ->
                User.create(validEmail, null, "홍길동", "테스트 회사", UserType.OWNER, passwordEncoder)
        ).isInstanceOf(ValidationException.class)
         .hasMessage("비밀번호는 필수입니다");
    }

    @Test
    @DisplayName("빈 이름으로 사용자 생성 시 예외가 발생한다")
    void createUser_WithEmptyName_ShouldThrowException() {
        assertThatThrownBy(() ->
                User.create(validEmail, validPassword, "", "테스트 회사", UserType.OWNER, passwordEncoder)
        ).isInstanceOf(ValidationException.class)
         .hasMessage("이름은 필수입니다");
    }

    @Test
    @DisplayName("올바른 비밀번호로 검증하면 true를 반환한다")
    void validatePassword_WithCorrectPassword_ShouldReturnTrue() {
        User user = User.create(validEmail, validPassword, "홍길동", "테스트 회사", UserType.OWNER, passwordEncoder);

        boolean result = user.validatePassword(validPassword, passwordEncoder);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("잘못된 비밀번호로 검증하면 false를 반환한다")
    void validatePassword_WithIncorrectPassword_ShouldReturnFalse() {
        User user = User.create(validEmail, validPassword, "홍길동", "테스트 회사", UserType.OWNER, passwordEncoder);
        Password wrongPassword = Password.of("Wrong123!@#");

        boolean result = user.validatePassword(wrongPassword, passwordEncoder);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("OWNER 타입이고 ACTIVE 상태인 사용자는 템플릿을 생성할 수 있다")
    void canCreateTemplate_OwnerAndActive_ShouldReturnTrue() {
        User user = User.create(validEmail, validPassword, "홍길동", "테스트 회사", UserType.OWNER, passwordEncoder);

        boolean result = user.canCreateTemplate();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("CONTRACTOR 타입 사용자는 템플릿을 생성할 수 없다")
    void canCreateTemplate_Contractor_ShouldReturnFalse() {
        User user = User.create(validEmail, validPassword, "홍길동", "테스트 회사", UserType.CONTRACTOR, passwordEncoder);

        boolean result = user.canCreateTemplate();

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("비활성화된 사용자는 템플릿을 생성할 수 없다")
    void canCreateTemplate_InactiveUser_ShouldReturnFalse() {
        User user = User.create(validEmail, validPassword, "홍길동", "테스트 회사", UserType.OWNER, passwordEncoder);
        user.deactivate();

        boolean result = user.canCreateTemplate();

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자를 활성화할 수 있다")
    void activate_ShouldChangeStatusToActive() {
        User user = User.create(validEmail, validPassword, "홍길동", "테스트 회사", UserType.OWNER, passwordEncoder);
        user.deactivate();

        user.activate();

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("정지된 사용자는 활성화할 수 없다")
    void activate_SuspendedUser_ShouldThrowException() {
        User user = User.create(validEmail, validPassword, "홍길동", "테스트 회사", UserType.OWNER, passwordEncoder);
        user.suspend();

        assertThatThrownBy(() -> user.activate())
                .isInstanceOf(ValidationException.class)
                .hasMessage("정지된 사용자는 활성화할 수 없습니다");
    }

    @Test
    @DisplayName("프로필을 수정할 수 있다")
    void updateProfile_ShouldUpdateNameAndCompany() {
        User user = User.create(validEmail, validPassword, "홍길동", "테스트 회사", UserType.OWNER, passwordEncoder);

        user.updateProfile("김철수", "새로운 회사");

        assertThat(user.getName()).isEqualTo("김철수");
        assertThat(user.getCompanyName()).isEqualTo("새로운 회사");
    }

    @Test
    @DisplayName("비밀번호를 변경할 수 있다")
    void changePassword_WithCorrectOldPassword_ShouldChangePassword() {
        User user = User.create(validEmail, validPassword, "홍길동", "테스트 회사", UserType.OWNER, passwordEncoder);
        Password newPassword = Password.of("NewPass123!@#");

        user.changePassword(validPassword, newPassword, passwordEncoder);

        assertThat(user.validatePassword(newPassword, passwordEncoder)).isTrue();
        assertThat(user.validatePassword(validPassword, passwordEncoder)).isFalse();
    }

    @Test
    @DisplayName("잘못된 기존 비밀번호로 변경 시도하면 예외가 발생한다")
    void changePassword_WithIncorrectOldPassword_ShouldThrowException() {
        User user = User.create(validEmail, validPassword, "홍길동", "테스트 회사", UserType.OWNER, passwordEncoder);
        Password wrongPassword = Password.of("Wrong123!@#");
        Password newPassword = Password.of("NewPass123!@#");

        assertThatThrownBy(() -> user.changePassword(wrongPassword, newPassword, passwordEncoder))
                .isInstanceOf(ValidationException.class)
                .hasMessage("기존 비밀번호가 일치하지 않습니다");
    }
}