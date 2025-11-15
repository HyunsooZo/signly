package com.signly.common.exception;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }

    public UnauthorizedException(
            String message,
            Throwable cause
    ) {
        super("UNAUTHORIZED", message, cause);
    }
}