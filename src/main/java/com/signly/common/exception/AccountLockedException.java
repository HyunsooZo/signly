package com.signly.common.exception;

/**
 * 계정 잠금 예외
 * - 로그인 5회 실패 시 발생
 * - HTTP 403 Forbidden 응답
 */
public class AccountLockedException extends RuntimeException {

    public AccountLockedException(String message) {
        super(message);
    }

    public AccountLockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
