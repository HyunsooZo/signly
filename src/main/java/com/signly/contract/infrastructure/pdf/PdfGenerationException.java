package com.signly.contract.infrastructure.pdf;

/**
 * PDF 생성 실패 시 발생하는 예외
 * SRP: PDF 생성 관련 예외만 표현
 */
public class PdfGenerationException extends RuntimeException {

    public PdfGenerationException(String message) {
        super(message);
    }

    public PdfGenerationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
