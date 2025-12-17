package com.signly.common.storage.strategy;

import com.signly.common.exception.ValidationException;
import com.signly.common.storage.util.FileSecurityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileStorageStrategySecurityTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageStrategy storage;
    private FileSecurityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileSecurityValidator();
        storage = new LocalFileStorageStrategy(
                tempDir.toString(),
                10485760L, // 10MB
                validator
        );
    }

    @Test
    @DisplayName("Path Traversal 공격 차단 - 상위 디렉토리 이동 시도")
    void storeFile_PathTraversal_ShouldThrow() {
        byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
        
        // Path Traversal 시도
        assertThrows(ValidationException.class, () -> 
            storage.storeFile(data, "test.pdf", "application/pdf", "../../etc"));
        
        assertThrows(ValidationException.class, () -> 
            storage.storeFile(data, "test.pdf", "application/pdf", "../admin"));
    }

    @Test
    @DisplayName("정상적인 하위 경로는 허용되어야 함")
    void storeFile_ValidSubPath_ShouldSucceed() {
        byte[] pdfData = "%PDF-1.4\nvalid pdf content".getBytes(StandardCharsets.UTF_8);
        byte[] pngData = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}; // PNG 시그니처
        
        // 실제 서비스에서 사용하는 경로 패턴
        assertDoesNotThrow(() -> 
            storage.storeFile(pdfData, "test.pdf", "application/pdf", "contracts/completed"));
        
        assertDoesNotThrow(() -> 
            storage.storeFile(pngData, "signature.png", "image/png", "signatures/first-party/123"));
        
        assertDoesNotThrow(() -> 
            storage.storeFile(pdfData, "contract.pdf", "application/pdf", "signatures/contracts/456/user-email"));
    }

    @Test
    @DisplayName("백슬래시가 포함된 카테고리는 차단")
    void storeFile_CategoryWithBackslash_ShouldThrow() {
        byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
        
        assertThrows(ValidationException.class, () -> 
            storage.storeFile(data, "test.pdf", "application/pdf", "uploads\\admin"));
    }

    @Test
    @DisplayName("절대 경로는 차단")
    void storeFile_AbsolutePath_ShouldThrow() {
        byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
        
        assertThrows(ValidationException.class, () -> 
            storage.storeFile(data, "test.pdf", "application/pdf", "/absolute/path"));
    }

    @Test
    @DisplayName("Null Byte Injection 방어")
    void storeFile_NullByteInjection_ShouldSanitize() {
        // PDF 파일 시그니처
        byte[] pdfData = "%PDF-1.4\ntest".getBytes(StandardCharsets.UTF_8);
        
        // Null byte를 포함한 파일명으로 JSP 업로드 시도
        var storedFile = storage.storeFile(
                pdfData, 
                "malicious.jsp\0.pdf", 
                "application/pdf", 
                "uploads"
        );
        
        // 원본 파일명에서 null byte가 제거되었는지 확인
        assertFalse(storedFile.originalFilename().contains("\0"));
    }

    @Test
    @DisplayName("확장자 위조 방어 - 실제 PDF를 PNG로 위장")
    void storeFile_ExtensionSpoofing_ShouldUseRealMimeType() {
        // PDF 파일 시그니처
        byte[] pdfData = "%PDF-1.4\ntest content".getBytes(StandardCharsets.UTF_8);
        
        // .png 확장자로 위조 시도
        var storedFile = storage.storeFile(
                pdfData, 
                "document.png",  // 확장자는 PNG
                "image/png",     // Content-Type도 PNG로 위조
                "uploads"
        );
        
        // 실제로 감지된 MIME 타입은 PDF
        assertEquals("application/pdf", storedFile.contentType());
        
        // 저장된 파일명은 .pdf 확장자를 가져야 함
        assertTrue(storedFile.storedFilename().endsWith(".pdf"));
    }

    @Test
    @DisplayName("악성 스크립트 업로드 차단 - HTML/JavaScript")
    void storeFile_MaliciousScript_ShouldThrow() {
        byte[] htmlData = "<script>alert('XSS')</script>".getBytes(StandardCharsets.UTF_8);
        
        // HTML/JS는 허용 목록에 없으므로 차단되어야 함
        assertThrows(ValidationException.class, () -> 
            storage.storeFile(htmlData, "hack.png", "image/png", "uploads"));
    }

    @Test
    @DisplayName("정상적인 PDF 파일 업로드는 성공해야 함")
    void storeFile_ValidPdf_ShouldSucceed() {
        byte[] pdfData = "%PDF-1.4\nvalid pdf content".getBytes(StandardCharsets.UTF_8);
        
        var storedFile = storage.storeFile(
                pdfData, 
                "document.pdf", 
                "application/pdf", 
                "contracts"
        );
        
        assertNotNull(storedFile);
        assertEquals("application/pdf", storedFile.contentType());
        assertTrue(storedFile.storedFilename().endsWith(".pdf"));
        assertEquals("contracts/" + storedFile.storedFilename(), storedFile.filePath());
    }

    @Test
    @DisplayName("파일 로드 시 Path Traversal 방어")
    void loadFile_PathTraversal_ShouldThrow() {
        assertThrows(ValidationException.class, () -> 
            storage.loadFile("../../etc/passwd"));
        
        assertThrows(ValidationException.class, () -> 
            storage.loadFile("../admin/secret.txt"));
    }

    @Test
    @DisplayName("파일 삭제 시 Path Traversal 방어")
    void deleteFile_PathTraversal_ShouldThrow() {
        assertThrows(ValidationException.class, () -> 
            storage.deleteFile("../../etc/passwd"));
    }
}
