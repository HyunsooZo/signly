package com.signly.common.exception;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }

    public NotFoundException(
            String message,
            Throwable cause
    ) {
        super("NOT_FOUND", message, cause);
    }
}