package com.deally.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class EmailTest {

    @Test
    @DisplayName("유효한 이메일 형식으로 Email 객체를 생성할 수 있다")
    void createEmail_WithValidFormat_ShouldSuccess() {
        String validEmail = "test@example.com";

        Email email = Email.of(validEmail);

        assertThat(email.value()).isEqualTo(validEmail);
    }

    @Test
    @DisplayName("이메일 생성 시 대소문자가 소문자로 변환된다")
    void createEmail_ShouldConvertToLowerCase() {
        String mixedCaseEmail = "Test@Example.COM";

        Email email = Email.of(mixedCaseEmail);

        assertThat(email.value()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("앞뒤 공백이 제거된다")
    void createEmail_ShouldTrimWhitespace() {
        String emailWithSpaces = "  test@example.com  ";

        Email email = Email.of(emailWithSpaces);

        assertThat(email.value()).isEqualTo("test@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email", "@example.com", "test@", "test.example.com"})
    @DisplayName("유효하지 않은 이메일 형식으로 생성 시 예외가 발생한다")
    void createEmail_WithInvalidFormat_ShouldThrowException(String invalidEmail) {
        assertThatThrownBy(() -> Email.of(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("null 이메일로 생성 시 예외가 발생한다")
    void createEmail_WithNull_ShouldThrowException() {
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일은 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("같은 값의 Email 객체는 동등하다")
    void equals_WithSameValue_ShouldBeEqual() {
        Email email1 = Email.of("test@example.com");
        Email email2 = Email.of("test@example.com");

        assertThat(email1).isEqualTo(email2);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }

    @Test
    @DisplayName("다른 값의 Email 객체는 동등하지 않다")
    void equals_WithDifferentValue_ShouldNotBeEqual() {
        Email email1 = Email.of("test1@example.com");
        Email email2 = Email.of("test2@example.com");

        assertThat(email1).isNotEqualTo(email2);
    }
}