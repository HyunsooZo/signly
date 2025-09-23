package com.signly.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, WebRequest request) {
        logger.warn("Validation error: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex, WebRequest request) {
        logger.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
        logger.warn("Unauthorized access: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "UNAUTHORIZED",
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex, WebRequest request) {
        logger.warn("Forbidden access: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "FORBIDDEN",
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        logger.warn("Business error: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "BUSINESS_ERROR",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("Method argument validation failed");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                "VALIDATION_ERROR",
                "입력값 검증에 실패했습니다",
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false),
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        logger.warn("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "ACCESS_DENIED",
                "접근 권한이 없습니다",
                HttpStatus.FORBIDDEN.value(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        logger.warn("Bad credentials: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "BAD_CREDENTIALS",
                "인증 정보가 올바르지 않습니다",
                HttpStatus.UNAUTHORIZED.value(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, WebRequest request) {
        logger.warn("File upload size exceeded: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "FILE_SIZE_EXCEEDED",
                "업로드 파일 크기가 제한을 초과했습니다",
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getDescription(false),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    public record ErrorResponse(
            String error,
            String message,
            int status,
            String path,
            LocalDateTime timestamp
    ) {}

    public record ValidationErrorResponse(
            String error,
            String message,
            int status,
            String path,
            LocalDateTime timestamp,
            Map<String, String> fieldErrors
    ) {}
}