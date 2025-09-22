package com.signly.domain.user.model;

import com.signly.common.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class PasswordTest {

    @ParameterizedTest
    @ValueSource(strings = {"Test123!@#", "Abc123!@#", "Password1!", "MySecure1@"})
    @DisplayName("유효한 비밀번호 형식으로 Password 객체를 생성할 수 있다")
    void createPassword_WithValidFormat_ShouldSuccess(String validPassword) {
        Password password = Password.of(validPassword);

        assertThat(password.getValue()).isEqualTo(validPassword);
    }

    @ParameterizedTest
    @ValueSource(strings = {"short1!", "1234567890", "abcdefgh", "ABCDEFGH", "!@#$%^&*", "Test123", "test123!", "TEST123!"})
    @DisplayName("유효하지 않은 비밀번호 형식으로 생성 시 예외가 발생한다")
    void createPassword_WithInvalidFormat_ShouldThrowException(String invalidPassword) {
        assertThatThrownBy(() -> Password.of(invalidPassword))
                .isInstanceOf(ValidationException.class)
                .hasMessage("비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 포함해야 합니다");
    }

    @Test
    @DisplayName("null 비밀번호로 생성 시 예외가 발생한다")
    void createPassword_WithNull_ShouldThrowException() {
        assertThatThrownBy(() -> Password.of(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("비밀번호는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("빈 문자열로 생성 시 예외가 발생한다")
    void createPassword_WithEmptyString_ShouldThrowException() {
        assertThatThrownBy(() -> Password.of(""))
                .isInstanceOf(ValidationException.class)
                .hasMessage("비밀번호는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("공백만 있는 문자열로 생성 시 예외가 발생한다")
    void createPassword_WithWhitespaceOnly_ShouldThrowException() {
        assertThatThrownBy(() -> Password.of("   "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("비밀번호는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("toString 메서드는 비밀번호를 마스킹한다")
    void toString_ShouldMaskPassword() {
        Password password = Password.of("Test123!@#");

        String result = password.toString();

        assertThat(result).isEqualTo("****");
    }

    @Test
    @DisplayName("같은 값의 Password 객체는 동등하다")
    void equals_WithSameValue_ShouldBeEqual() {
        Password password1 = Password.of("Test123!@#");
        Password password2 = Password.of("Test123!@#");

        assertThat(password1).isEqualTo(password2);
        assertThat(password1.hashCode()).isEqualTo(password2.hashCode());
    }

    @Test
    @DisplayName("다른 값의 Password 객체는 동등하지 않다")
    void equals_WithDifferentValue_ShouldNotBeEqual() {
        Password password1 = Password.of("Test123!@#");
        Password password2 = Password.of("Different1@#");

        assertThat(password1).isNotEqualTo(password2);
    }
}