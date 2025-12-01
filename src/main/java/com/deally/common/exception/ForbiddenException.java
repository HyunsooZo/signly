package com.deally.common.exception;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }

    public ForbiddenException(
            String message,
            Throwable cause
    ) {
        super("FORBIDDEN", message, cause);
    }
}