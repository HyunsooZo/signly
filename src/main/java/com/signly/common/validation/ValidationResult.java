package com.signly.common.validation;

/**
 * 검증 결과를 담는 레코드
 */
public record ValidationResult(
        boolean isValid,
        String errorMessage
) {

    /**
     * 성공 결과 생성
     */
    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    /**
     * 실패 결과 생성
     */
    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }
}