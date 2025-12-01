package com.deally.common.exception;

public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(
            String message,
            Throwable cause
    ) {
        super("VALIDATION_ERROR", message, cause);
    }
}