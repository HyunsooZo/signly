package com.signly.common.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AES 암호화 서비스 테스트")
class AesEncryptionServiceTest {

    private AesEncryptionService encryptionService;
    private EncryptionProperties properties;

    @BeforeEach
    void setUp() {
        properties = new EncryptionProperties();
        properties.setEnabled(true);
        properties.setAlgorithm("AES/GCM/NoPadding");
        properties.setIvLength(12);
        properties.setTagLength(128);
        properties.setCharset("UTF-8");
        
        // 32-byte key for AES-256
        String base64Key = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());
        properties.setSecretKey(base64Key);
        
        encryptionService = new AesEncryptionService(properties);
    }

    @Test
    @DisplayName("암호화 후 복호화하면 원본 데이터가 복원된다")
    void encrypt_decrypt_roundtrip_success() {
        // Given
        String plainText = "010-1234-5678";

        // When
        String encrypted = encryptionService.encrypt(plainText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertNotNull(encrypted);
        assertNotNull(decrypted);
        assertEquals(plainText, decrypted);
        assertNotEquals(plainText, encrypted);
    }

    @Test
    @DisplayName("동일한 평문을 암호화하면 매번 다른 암호문이 생성된다 (random IV)")
    void encrypt_same_plaintext_different_ciphertext() {
        // Given
        String plainText = "010-1234-5678";

        // When
        String encrypted1 = encryptionService.encrypt(plainText);
        String encrypted2 = encryptionService.encrypt(plainText);

        // Then
        assertNotEquals(encrypted1, encrypted2);

        // But both decrypt to the same value
        assertEquals(plainText, encryptionService.decrypt(encrypted1));
        assertEquals(plainText, encryptionService.decrypt(encrypted2));
    }

    @Test
    @DisplayName("유효하지 않은 암호문을 복호화하면 null을 반환한다")
    void decrypt_invalid_ciphertext_returns_null() {
        // Given
        String invalidCipherText = "invalid-base64-string";

        // When
        String decrypted = encryptionService.decrypt(invalidCipherText);

        // Then
        assertNull(decrypted);
    }

    @Test
    @DisplayName("너무 짧은 암호문을 복호화하면 null을 반환한다")
    void decrypt_too_short_ciphertext_returns_null() {
        // Given
        String shortCipherText = Base64.getEncoder().encodeToString("short".getBytes());

        // When
        String decrypted = encryptionService.decrypt(shortCipherText);

        // Then
        assertNull(decrypted);
    }

    @Test
    @DisplayName("null을 암호화하면 null을 반환한다")
    void encrypt_null_returns_null() {
        // When
        String encrypted = encryptionService.encrypt(null);

        // Then
        assertNull(encrypted);
    }

    @Test
    @DisplayName("null을 복호화하면 null을 반환한다")
    void decrypt_null_returns_null() {
        // When
        String decrypted = encryptionService.decrypt(null);

        // Then
        assertNull(decrypted);
    }

    @Test
    @DisplayName("빈 문자열을 암호화하면 암호문이 생성된다")
    void encrypt_empty_string_returns_encrypted() {
        // Given
        String emptyText = "";

        // When
        String encrypted = encryptionService.encrypt(emptyText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertNotNull(encrypted);
        assertNotEquals(emptyText, encrypted);
        assertEquals(emptyText, decrypted);
    }

    @Test
    @DisplayName("긴 텍스트도 정상적으로 암호화/복호화된다")
    void encrypt_long_text_success() {
        // Given
        String longText = "서울시 강남구 테헤란로 123 ABC빌딩 15층 (우편번호: 06234) " +
                         "전화: 02-1234-5678, 팩스: 02-1234-5679 " +
                         "담당자: 김철수 (이메일: kim@example.com)";

        // When
        String encrypted = encryptionService.encrypt(longText);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertNotNull(encrypted);
        assertEquals(longText, decrypted);
        assertNotEquals(longText, encrypted);
    }

    @Test
    @DisplayName("암호화된 텍스트를 감지할 수 있다")
    void isEncrypted_detects_encrypted_text() {
        // Given
        String plainText = "010-1234-5678";
        String encrypted = encryptionService.encrypt(plainText);

        // When & Then
        assertFalse(encryptionService.isEncrypted(plainText));
        assertTrue(encryptionService.isEncrypted(encrypted));
        assertFalse(encryptionService.isEncrypted(null));
    }

    @Test
    @DisplayName("암호화가 비활성화되면 isEncrypted는 false를 반환한다")
    void isEncrypted_disabled_returns_false() {
        // Given
        properties.setEnabled(false);
        encryptionService = new AesEncryptionService(properties);

        String plainText = "010-1234-5678";

        // When & Then
        assertFalse(encryptionService.isEncrypted(plainText));
    }

    @Test
    @DisplayName("암호화가 비활성화되면 원본을 그대로 반환한다")
    void encrypt_disabled_returns_original() {
        // Given
        properties.setEnabled(false);
        encryptionService = new AesEncryptionService(properties);

        String plainText = "010-1234-5678";

        // When
        String encrypted = encryptionService.encrypt(plainText);

        // Then
        assertEquals(plainText, encrypted);
    }

    @Test
    @DisplayName("암호화가 비활성화되면 복호화시 원본을 그대로 반환한다")
    void decrypt_disabled_returns_original() {
        // Given
        properties.setEnabled(false);
        encryptionService = new AesEncryptionService(properties);

        String cipherText = "some-cipher-text";

        // When
        String decrypted = encryptionService.decrypt(cipherText);

        // Then
        assertEquals(cipherText, decrypted);
    }

    @Test
    @DisplayName("유효하지 않은 키 길이를 사용하면 예외가 발생한다")
    void invalid_key_length_throws_exception() {
        // Given
        String shortKey = Base64.getEncoder().encodeToString("short".getBytes());
        properties.setSecretKey(shortKey);
        AesEncryptionService service = new AesEncryptionService(properties);

        // When & Then - 암호화 시도 시 lazy initialization으로 인해 RuntimeException 발생
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.encrypt("test");
        });

        // 원인(cause)이 IllegalArgumentException인지 확인
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("키가 설정되지 않으면 예외가 발생한다")
    void missing_key_throws_exception() {
        // Given
        properties.setSecretKey(null);
        AesEncryptionService service = new AesEncryptionService(properties);

        // When & Then - 암호화 시도 시 lazy initialization으로 인해 RuntimeException 발생
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.encrypt("test");
        });

        // 원인(cause)이 IllegalStateException인지 확인
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }
}