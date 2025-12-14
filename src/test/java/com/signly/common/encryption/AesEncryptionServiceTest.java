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

        // Salt for hashing
        properties.setSalt("testSaltForHashing123");

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

    // ==================== Hash Tests ====================

    @Test
    @DisplayName("동일한 입력은 항상 동일한 해시를 생성한다")
    void hash_same_input_same_output() {
        // Given
        String input = "test@example.com";

        // When
        String hash1 = encryptionService.hash(input);
        String hash2 = encryptionService.hash(input);

        // Then
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("다른 입력은 다른 해시를 생성한다")
    void hash_different_input_different_output() {
        // Given
        String input1 = "test1@example.com";
        String input2 = "test2@example.com";

        // When
        String hash1 = encryptionService.hash(input1);
        String hash2 = encryptionService.hash(input2);

        // Then
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("null을 해시하면 null을 반환한다")
    void hash_null_returns_null() {
        // When
        String hash = encryptionService.hash(null);

        // Then
        assertNull(hash);
    }

    @Test
    @DisplayName("해시는 64자리 16진수 문자열이다 (SHA-256)")
    void hash_returns_64_hex_chars() {
        // Given
        String input = "test@example.com";

        // When
        String hash = encryptionService.hash(input);

        // Then
        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("^[0-9a-f]{64}$"));
    }

    @Test
    @DisplayName("Salt가 설정되지 않으면 해시 생성 시 예외가 발생한다")
    void hash_missing_salt_throws_exception() {
        // Given
        properties.setSalt(null);
        AesEncryptionService service = new AesEncryptionService(properties);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            service.hash("test");
        });
    }

    // ==================== Email Hash Tests ====================

    @Test
    @DisplayName("이메일 해시는 대소문자를 구분하지 않는다 (정규화)")
    void hashEmail_case_insensitive() {
        // Given
        String email1 = "Test@Example.COM";
        String email2 = "test@example.com";
        String email3 = "TEST@EXAMPLE.COM";

        // When
        String hash1 = encryptionService.hashEmail(email1);
        String hash2 = encryptionService.hashEmail(email2);
        String hash3 = encryptionService.hashEmail(email3);

        // Then
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertEquals(hash2, hash3);
    }

    @Test
    @DisplayName("이메일 해시는 앞뒤 공백을 제거한다 (정규화)")
    void hashEmail_trims_whitespace() {
        // Given
        String email1 = "  test@example.com  ";
        String email2 = "test@example.com";
        String email3 = "\ttest@example.com\n";

        // When
        String hash1 = encryptionService.hashEmail(email1);
        String hash2 = encryptionService.hashEmail(email2);
        String hash3 = encryptionService.hashEmail(email3);

        // Then
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertEquals(hash2, hash3);
    }

    @Test
    @DisplayName("이메일 해시는 대소문자와 공백을 모두 정규화한다")
    void hashEmail_normalizes_case_and_whitespace() {
        // Given
        String email1 = "  Test@Example.COM  ";
        String email2 = "test@example.com";

        // When
        String hash1 = encryptionService.hashEmail(email1);
        String hash2 = encryptionService.hashEmail(email2);

        // Then
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("null 이메일을 해시하면 null을 반환한다")
    void hashEmail_null_returns_null() {
        // When
        String hash = encryptionService.hashEmail(null);

        // Then
        assertNull(hash);
    }

    @Test
    @DisplayName("동일한 이메일은 항상 동일한 해시를 생성한다 (일관성)")
    void hashEmail_consistency() {
        // Given
        String email = "user@example.com";

        // When
        String hash1 = encryptionService.hashEmail(email);
        String hash2 = encryptionService.hashEmail(email);
        String hash3 = encryptionService.hashEmail(email);

        // Then
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertEquals(hash2, hash3);
    }

    @Test
    @DisplayName("다른 이메일은 다른 해시를 생성한다")
    void hashEmail_different_emails_different_hashes() {
        // Given
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        // When
        String hash1 = encryptionService.hashEmail(email1);
        String hash2 = encryptionService.hashEmail(email2);

        // Then
        assertNotEquals(hash1, hash2);
    }
}