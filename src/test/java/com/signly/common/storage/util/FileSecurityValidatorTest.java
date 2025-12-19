package com.signly.common.storage.util;

import com.signly.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FileSecurityValidatorTest {

    private FileSecurityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileSecurityValidator();
    }

    @Test
    @DisplayName("유효한 카테고리는 통과해야 함")
    void validateCategory_ValidCategory_ShouldPass() {
        assertDoesNotThrow(() -> validator.validateCategory("templates"));
        assertDoesNotThrow(() -> validator.validateCategory("user-uploads"));
        assertDoesNotThrow(() -> validator.validateCategory("contract_files"));
        assertDoesNotThrow(() -> validator.validateCategory("temp123"));
    }

    @Test
    @DisplayName("Path Traversal 시도는 차단되어야 함")
    void validateCategory_PathTraversal_ShouldThrow() {
        assertThrows(ValidationException.class, () -> 
            validator.validateCategory("../../../etc/passwd"));
        
        assertThrows(ValidationException.class, () -> 
            validator.validateCategory("..\\..\\windows\\system32"));
        
        assertThrows(ValidationException.class, () -> 
            validator.validateCategory("uploads/../admin"));
    }

    @Test
    @DisplayName("정상적인 하위 경로는 허용되어야 함")
    void validateCategory_ValidSubPath_ShouldPass() {
        assertDoesNotThrow(() -> validator.validateCategory("contracts/completed"));
        assertDoesNotThrow(() -> validator.validateCategory("signatures/first-party/123"));
        assertDoesNotThrow(() -> validator.validateCategory("signatures/contracts/456/user-email"));
    }

    @Test
    @DisplayName("백슬래시가 포함된 카테고리는 차단되어야 함")
    void validateCategory_WithBackslash_ShouldThrow() {
        assertThrows(ValidationException.class, () -> 
            validator.validateCategory("folder\\subfolder"));
    }

    @Test
    @DisplayName("절대 경로는 차단되어야 함")
    void validateCategory_AbsolutePath_ShouldThrow() {
        assertThrows(ValidationException.class, () -> 
            validator.validateCategory("/absolute/path"));
    }

    @Test
    @DisplayName("특수문자가 포함된 카테고리는 차단되어야 함")
    void validateCategory_WithSpecialChars_ShouldThrow() {
        assertThrows(ValidationException.class, () -> 
            validator.validateCategory("uploads@#$"));
        
        assertThrows(ValidationException.class, () -> 
            validator.validateCategory("uploads*"));
    }

    @Test
    @DisplayName("빈 카테고리는 차단되어야 함")
    void validateCategory_Empty_ShouldThrow() {
        assertThrows(ValidationException.class, () -> 
            validator.validateCategory(""));
        
        assertThrows(ValidationException.class, () -> 
            validator.validateCategory("   "));
        
        assertThrows(ValidationException.class, () -> 
            validator.validateCategory(null));
    }

    @Test
    @DisplayName("MIME 타입에서 올바른 확장자를 반환해야 함")
    void getSafeExtension_ValidMimeTypes_ShouldReturnCorrectExtension() {
        assertEquals(".pdf", validator.getSafeExtension("application/pdf"));
        assertEquals(".jpg", validator.getSafeExtension("image/jpeg"));
        assertEquals(".png", validator.getSafeExtension("image/png"));
        assertEquals(".gif", validator.getSafeExtension("image/gif"));
    }

    @Test
    @DisplayName("지원하지 않는 MIME 타입은 예외를 발생시켜야 함")
    void getSafeExtension_InvalidMimeType_ShouldThrow() {
        assertThrows(ValidationException.class, () -> 
            validator.getSafeExtension("application/x-executable"));
        
        assertThrows(ValidationException.class, () -> 
            validator.getSafeExtension("text/html"));
    }

    @Test
    @DisplayName("Null Byte Injection을 방어해야 함")
    void sanitizeFilename_NullByteInjection_ShouldRemove() {
        String malicious = "malicious.jsp\0.png";
        String sanitized = validator.sanitizeFilename(malicious);
        
        assertFalse(sanitized.contains("\0"));
        assertEquals("malicious.jsp.png", sanitized);
    }

    @Test
    @DisplayName("위험한 문자를 제거해야 함")
    void sanitizeFilename_DangerousChars_ShouldRemove() {
        String dangerous = "file<name>:with|chars?.txt";
        String sanitized = validator.sanitizeFilename(dangerous);
        
        assertFalse(sanitized.contains("<"));
        assertFalse(sanitized.contains(">"));
        assertFalse(sanitized.contains(":"));
        assertFalse(sanitized.contains("|"));
        assertFalse(sanitized.contains("?"));
    }

    @Test
    @DisplayName("MIME 타입 감지 - PDF 파일")
    void detectAndValidateMimeType_PdfFile_ShouldDetectCorrectly() {
        byte[] pdfHeader = "%PDF-1.4\n".getBytes(StandardCharsets.UTF_8);
        
        String detectedType = validator.detectAndValidateMimeType(pdfHeader, "application/pdf");
        assertEquals("application/pdf", detectedType);
    }

    @Test
    @DisplayName("MIME 타입 위조 감지 - 실제로는 다른 파일인 경우")
    void detectAndValidateMimeType_FakePng_ShouldThrow() {
        byte[] textData = "This is not a PNG file".getBytes(StandardCharsets.UTF_8);
        
        assertThrows(ValidationException.class, () ->
            validator.detectAndValidateMimeType(textData, "image/png"));
    }
}
