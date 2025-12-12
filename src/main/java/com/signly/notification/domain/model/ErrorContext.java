package com.signly.notification.domain.model;

import java.time.LocalDateTime;

public record ErrorContext(
        String errorType,
        String message,
        String stackTrace,
        String requestUrl,
        String requestMethod,
        String userIp,
        String userAgent,
        LocalDateTime timestamp
) {
    public static ErrorContext of(
            Exception exception,
            String requestUrl,
            String requestMethod,
            String userIp,
            String userAgent
    ) {
        return new ErrorContext(
                exception.getClass().getSimpleName(),
                exception.getMessage() != null ? exception.getMessage() : "No message",
                getStackTraceAsString(exception, 10),
                requestUrl,
                requestMethod,
                userIp,
                userAgent,
                LocalDateTime.now()
        );
    }

    private static String getStackTraceAsString(
            Exception exception,
            int maxLines
    ) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        StringBuilder sb = new StringBuilder();

        int linesToShow = Math.min(maxLines, stackTrace.length);
        for (int i = 0; i < linesToShow; i++) {
            sb.append("at ").append(stackTrace[i].toString()).append("\n");
        }

        if (stackTrace.length > maxLines) {
            sb.append("... ").append(stackTrace.length - maxLines).append(" more");
        }

        return sb.toString();
    }
}
