package com.signly.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordValidator 테스트")
class PasswordValidatorTest {

    @Nested
    @DisplayName("isValid() 메서드 테스트")
    class IsValidTest {

        @ParameterizedTest
        @ValueSource(strings = {
            "Password123!",  // 기본 유효한 비밀번호
            "MySecure1@Pass", // 대소문자, 숫자, 특수문자
            "Test#1234",     // 다른 특수문자
            "Abcdefgh1@",    // 최소 길이
            "VerySecurePassword123$", // 긴 비밀번호
            "Aa1@aaaa",      // 최소 요구사항만 만족
            "PASSWORD123@",  // 대문자, 숫자, 특수문자 (소문자 없음 - 실패 예상)
            "password123!",  // 소문자, 숫자, 특수문자 (대문자 없음 - 실패 예상)
            "Passwordabc",   // 대소문자만 (숫자, 특수문자 없음 - 실패 예상)
            "12345678!",     // 숫자, 특수문자만 (대소문자 없음 - 실패 예상)
            "Password",      // 대소문자만 (숫자, 특수문자 없음 - 실패 예상)
            "Pass12!",       // 길이 부족 (7자 - 실패 예상)
            "",              // 빈 문자열 (실패 예상)
            "   "            // 공백만 (실패 예상)
        })
        @DisplayName("다양한 비밀번호 패턴 검증")
        void testPasswordValidation(String password) {
            // When
            boolean result = PasswordValidator.isValid(password);

            // Then
            switch (password) {
                case "Password123!", "MySecure1@Pass", "Test#1234", 
                     "Abcdefgh1@", "VerySecurePassword123$", "Aa1@aaaa":
                    assertThat(result).isTrue();
                    break;
                case "PASSWORD123@", "password123!", "Passwordabc", 
                     "12345678!", "Password", "Pass12!", "", "   ":
                    assertThat(result).isFalse();
                    break;
            }
        }

        @Test
        @DisplayName("유효한 비밀번호는 true를 반환한다")
        void testValidPassword() {
            // Given
            String validPassword = "Password123!";

            // When
            boolean result = PasswordValidator.isValid(validPassword);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("null 비밀번호는 false를 반환한다")
        void testNullPassword() {
            // When
            boolean result = PasswordValidator.isValid(null);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("개별 검증 메서드 테스트")
    class IndividualValidationTest {

        @Test
        @DisplayName("hasMinimumLength() 메서드 테스트")
        void testHasMinimumLength() {
            assertThat(PasswordValidator.hasMinimumLength("12345678")).isTrue();
            assertThat(PasswordValidator.hasMinimumLength("123456789")).isTrue();
            assertThat(PasswordValidator.hasMinimumLength("1234567")).isFalse();
            assertThat(PasswordValidator.hasMinimumLength(null)).isFalse();
        }

        @Test
        @DisplayName("containsLowercase() 메서드 테스트")
        void testContainsLowercase() {
            assertThat(PasswordValidator.containsLowercase("abc")).isTrue();
            assertThat(PasswordValidator.containsLowercase("ABC123")).isFalse();
            assertThat(PasswordValidator.containsLowercase("123")).isFalse();
            assertThat(PasswordValidator.containsLowercase(null)).isFalse();
        }

        @Test
        @DisplayName("containsUppercase() 메서드 테스트")
        void testContainsUppercase() {
            assertThat(PasswordValidator.containsUppercase("ABC")).isTrue();
            assertThat(PasswordValidator.containsUppercase("abc123")).isFalse();
            assertThat(PasswordValidator.containsUppercase("123")).isFalse();
            assertThat(PasswordValidator.containsUppercase(null)).isFalse();
        }

        @Test
        @DisplayName("containsDigit() 메서드 테스트")
        void testContainsDigit() {
            assertThat(PasswordValidator.containsDigit("123")).isTrue();
            assertThat(PasswordValidator.containsDigit("abc")).isFalse();
            assertThat(PasswordValidator.containsDigit("ABC")).isFalse();
            assertThat(PasswordValidator.containsDigit(null)).isFalse();
        }

        @Test
        @DisplayName("containsSpecialCharacter() 메서드 테스트")
        void testContainsSpecialCharacter() {
            assertThat(PasswordValidator.containsSpecialCharacter("test@")).isTrue();
            assertThat(PasswordValidator.containsSpecialCharacter("test$")).isTrue();
            assertThat(PasswordValidator.containsSpecialCharacter("test!")).isTrue();
            assertThat(PasswordValidator.containsSpecialCharacter("test%")).isTrue();
            assertThat(PasswordValidator.containsSpecialCharacter("test*")).isTrue();
            assertThat(PasswordValidator.containsSpecialCharacter("test#")).isTrue();
            assertThat(PasswordValidator.containsSpecialCharacter("test?")).isTrue();
            assertThat(PasswordValidator.containsSpecialCharacter("test&")).isTrue();
            assertThat(PasswordValidator.containsSpecialCharacter("test<")).isFalse(); // 허용되지 않는 특수문자
            assertThat(PasswordValidator.containsSpecialCharacter("test>")).isFalse(); // 허용되지 않는 특수문자
            assertThat(PasswordValidator.containsSpecialCharacter("test")).isFalse();
            assertThat(PasswordValidator.containsSpecialCharacter(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("validateDetailed() 메서드 테스트")
    class ValidateDetailedTest {

        @Test
        @DisplayName("유효한 비밀번호는 유효한 결과를 반환한다")
        void testValidDetailedResult() {
            // Given
            String validPassword = "Password123!";

            // When
            PasswordValidator.PasswordValidationResult result = 
                PasswordValidator.validateDetailed(validPassword);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("null 비밀번호는 적절한 에러 메시지를 반환한다")
        void testNullDetailedResult() {
            // When
            PasswordValidator.PasswordValidationResult result = 
                PasswordValidator.validateDetailed(null);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage()).isEqualTo("비밀번호는 null일 수 없습니다");
        }

        @Test
        @DisplayName("짧은 비밀번호는 길이 에러 메시지를 반환한다")
        void testShortPasswordDetailedResult() {
            // Given
            String shortPassword = "Pass12!";

            // When
            PasswordValidator.PasswordValidationResult result = 
                PasswordValidator.validateDetailed(shortPassword);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage()).contains("최소 8자 이상이어야 합니다");
        }

        @Test
        @DisplayName("소문자 없는 비밀번호는 소문자 에러 메시지를 반환한다")
        void testNoLowercaseDetailedResult() {
            // Given
            String password = "PASSWORD123!";

            // When
            PasswordValidator.PasswordValidationResult result = 
                PasswordValidator.validateDetailed(password);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage()).contains("소문자를 포함해야 합니다");
        }

        @Test
        @DisplayName("대문자 없는 비밀번호는 대문자 에러 메시지를 반환한다")
        void testNoUppercaseDetailedResult() {
            // Given
            String password = "password123!";

            // When
            PasswordValidator.PasswordValidationResult result = 
                PasswordValidator.validateDetailed(password);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage()).contains("대문자를 포함해야 합니다");
        }

        @Test
        @DisplayName("숫자 없는 비밀번호는 숫자 에러 메시지를 반환한다")
        void testNoDigitDetailedResult() {
            // Given
            String password = "Password!";

            // When
            PasswordValidator.PasswordValidationResult result = 
                PasswordValidator.validateDetailed(password);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage()).contains("숫자를 포함해야 합니다");
        }

        @Test
        @DisplayName("특수문자 없는 비밀번호는 특수문자 에러 메시지를 반환한다")
        void testNoSpecialCharacterDetailedResult() {
            // Given
            String password = "Password123";

            // When
            PasswordValidator.PasswordValidationResult result = 
                PasswordValidator.validateDetailed(password);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage()).contains("특수문자(@$!%*#?&)를 포함해야 합니다");
        }

        @Test
        @DisplayName("여러 문제가 있는 비밀번호는 모든 에러 메시지를 반환한다")
        void testMultipleIssuesDetailedResult() {
            // Given
            String password = "pass"; // 짧고, 대문자/숫자/특수문자 없음

            // When
            PasswordValidator.PasswordValidationResult result = 
                PasswordValidator.validateDetailed(password);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage())
                .contains("최소 8자 이상이어야 합니다")
                .contains("대문자를 포함해야 합니다")
                .contains("숫자를 포함해야 합니다")
                .contains("특수문자(@$!%*#?&)를 포함해야 합니다");
        }
    }

    @Nested
    @DisplayName("PasswordValidationResult 클래스 테스트")
    class PasswordValidationResultTest {

        @Test
        @DisplayName("valid() 정적 팩토리 메서드 테스트")
        void testValidFactory() {
            // When
            PasswordValidator.PasswordValidationResult result = 
                PasswordValidator.PasswordValidationResult.valid();

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("invalid() 정적 팩토리 메서드 테스트")
        void testInvalidFactory() {
            // Given
            String errorMessage = "테스트 에러";

            // When
            PasswordValidator.PasswordValidationResult result = 
                PasswordValidator.PasswordValidationResult.invalid(errorMessage);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage()).isEqualTo(errorMessage);
        }
    }

    @Nested
    @DisplayName("상수 값 테스트")
    class ConstantsTest {

        @Test
        @DisplayName("PASSWORD_REGEX 상수가 올바른 패턴을 포함한다")
        void testPasswordRegexConstant() {
            String regex = PasswordValidator.PASSWORD_REGEX;
            
            assertThat(regex).contains("?=.*[a-z]");  // 소문자
            assertThat(regex).contains("?=.*[A-Z]");  // 대문자
            assertThat(regex).contains("?=.*\\d");    // 숫자
            assertThat(regex).contains("?=.*[@$!%*#?&]"); // 특수문자
            assertThat(regex).contains("{8,}");       // 최소 8자
        }

        @Test
        @DisplayName("MIN_PASSWORD_LENGTH 상수가 8이다")
        void testMinPasswordLengthConstant() {
            assertThat(PasswordValidator.MIN_PASSWORD_LENGTH).isEqualTo(8);
        }

        @Test
        @DisplayName("PASSWORD_REQUIREMENT_MESSAGE 상수가 적절한 메시지를 포함한다")
        void testPasswordRequirementMessageConstant() {
            String message = PasswordValidator.PASSWORD_REQUIREMENT_MESSAGE;
            
            assertThat(message).contains("8자 이상");
            assertThat(message).contains("대소문자");
            assertThat(message).contains("숫자");
            assertThat(message).contains("특수문자");
        }
    }
}