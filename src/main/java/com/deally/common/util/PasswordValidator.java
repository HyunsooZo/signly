package com.deally.common.util;

import lombok.Getter;

import java.util.regex.Pattern;

/**
 * 비밀번호 유효성 검증을 위한 유틸리티 클래스
 * <p>
 * 비밀번호 정책:
 * - 최소 8자 이상
 * - 최소 1개의 소문자 포함
 * - 최소 1개의 대문자 포함
 * - 최소 1개의 숫자 포함
 * - 최소 1개의 특수문자 포함 (@$!%*#?&)
 */
public final class PasswordValidator {

    private PasswordValidator() {
        // 유틸리티 클래스 - 인스턴스화 방지
    }

    /**
     * 비밀번호 검증을 위한 정규식 패턴
     * - 최소 8자 이상
     * - 대소문자, 숫자, 특수문자(@$!%*#?&) 포함
     */
    public static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";

    /**
     * 비밀번호 최소 길이
     */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * 비밀번호 요구사항 안내 메시지
     */
    public static final String PASSWORD_REQUIREMENT_MESSAGE =
            "비밀번호는 8자 이상이며 대소문자, 숫자, 특수문자를 포함해야 합니다";

    /**
     * 컴파일된 정규식 패턴 (성능 최적화)
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    /**
     * 비밀번호가 유효한지 검증합니다.
     *
     * @param password 검증할 비밀번호
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 비밀번호가 최소 길이 요구사항을 만족하는지 확인합니다.
     *
     * @param password 확인할 비밀번호
     * @return 최소 길이 이상이면 true, 그렇지 않으면 false
     */
    public static boolean hasMinimumLength(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * 비밀번호에 소문자가 포함되어 있는지 확인합니다.
     *
     * @param password 확인할 비밀번호
     * @return 소문자가 포함되어 있으면 true, 그렇지 않으면 false
     */
    public static boolean containsLowercase(String password) {
        return password != null && password.matches(".*[a-z].*");
    }

    /**
     * 비밀번호에 대문자가 포함되어 있는지 확인합니다.
     *
     * @param password 확인할 비밀번호
     * @return 대문자가 포함되어 있으면 true, 그렇지 않으면 false
     */
    public static boolean containsUppercase(String password) {
        return password != null && password.matches(".*[A-Z].*");
    }

    /**
     * 비밀번호에 숫자가 포함되어 있는지 확인합니다.
     *
     * @param password 확인할 비밀번호
     * @return 숫자가 포함되어 있으면 true, 그렇지 않으면 false
     */
    public static boolean containsDigit(String password) {
        return password != null && password.matches(".*\\d.*");
    }

    /**
     * 비밀번호에 특수문자가 포함되어 있는지 확인합니다.
     *
     * @param password 확인할 비밀번호
     * @return 특수문자가 포함되어 있으면 true, 그렇지 않으면 false
     */
    public static boolean containsSpecialCharacter(String password) {
        return password != null && password.matches(".*[@$!%*#?&].*");
    }

    /**
     * 비밀번호 유효성 검증 결과를 상세하게 반환합니다.
     *
     * @param password 검증할 비밀번호
     * @return 검증 결과 객체
     */
    public static PasswordValidationResult validateDetailed(String password) {
        if (password == null) {
            return PasswordValidationResult.invalid("비밀번호는 null일 수 없습니다");
        }

        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        if (!hasMinimumLength(password)) {
            isValid = false;
            errorMessage.append("비밀번호는 최소 ").append(MIN_PASSWORD_LENGTH).append("자 이상이어야 합니다. ");
        }

        if (!containsLowercase(password)) {
            isValid = false;
            errorMessage.append("소문자를 포함해야 합니다. ");
        }

        if (!containsUppercase(password)) {
            isValid = false;
            errorMessage.append("대문자를 포함해야 합니다. ");
        }

        if (!containsDigit(password)) {
            isValid = false;
            errorMessage.append("숫자를 포함해야 합니다. ");
        }

        if (!containsSpecialCharacter(password)) {
            isValid = false;
            errorMessage.append("특수문자(@$!%*#?&)를 포함해야 합니다. ");
        }

        if (isValid) {
            return PasswordValidationResult.valid();
        } else {
            return PasswordValidationResult.invalid(errorMessage.toString().trim());
        }
    }

    /**
     * 비밀번호 유효성 검증 결과를 담는 클래스
     */
    @Getter
    public static final class PasswordValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private PasswordValidationResult(
                boolean valid,
                String errorMessage
        ) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static PasswordValidationResult valid() {
            return new PasswordValidationResult(true, null);
        }

        public static PasswordValidationResult invalid(String errorMessage) {
            return new PasswordValidationResult(false, errorMessage);
        }

    }
}