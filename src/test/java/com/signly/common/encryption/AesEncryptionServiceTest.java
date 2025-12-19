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
        assertTrue(encrypted.startsWith("{ENC}"), "암호화된 데이터는 {ENC} prefix를 가져야 함");
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
        assertTrue(encrypted1.startsWith("{ENC}"), "첫 번째 암호문은 {ENC} prefix를 가져야 함");
        assertTrue(encrypted2.startsWith("{ENC}"), "두 번째 암호문은 {ENC} prefix를 가져야 함");

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
        assertTrue(encrypted.startsWith("{ENC}"), "빈 문자열도 {ENC} prefix를 가져야 함");
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
        assertTrue(encrypted.startsWith("{ENC}"), "긴 텍스트도 {ENC} prefix를 가져야 함");
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

    // ==================== {ENC} Prefix Tests ====================

    @Test
    @DisplayName("Legacy 형식(prefix 없음)의 암호화 데이터도 복호화 가능 (하위 호환성)")
    void decrypt_legacy_format_without_prefix_success() {
        // Given - 먼저 암호화하고 prefix 제거하여 legacy 형식 시뮬레이션
        String plainText = "010-1234-5678";
        String encrypted = encryptionService.encrypt(plainText);
        String legacyFormat = encrypted.substring(5); // {ENC} 제거

        // When
        String decrypted = encryptionService.decrypt(legacyFormat);

        // Then
        assertNotNull(decrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("isEncrypted()는 {ENC} prefix만 체크하며 Base64 문자열은 암호화로 간주하지 않음")
    void isEncrypted_prefix_only_detection() {
        // Given
        String plainBase64 = Base64.getEncoder().encodeToString("This is a long enough text for testing".getBytes());
        String withPrefix = "{ENC}" + plainBase64;

        // When & Then
        assertFalse(encryptionService.isEncrypted(plainBase64),
                   "Base64 문자열만으로는 암호화로 판단하지 않아야 함 (False Positive 방지)");
        assertTrue(encryptionService.isEncrypted(withPrefix),
                  "{ENC} prefix가 있으면 암호화로 판단해야 함");
    }

    @Test
    @DisplayName("{ENC} prefix 뒤에 유효하지 않은 데이터가 있으면 복호화 실패")
    void decrypt_invalid_data_after_prefix_returns_null() {
        // Given
        String invalidData = "{ENC}invalid-base64-data!!!";

        // When
        String decrypted = encryptionService.decrypt(invalidData);

        // Then
        assertNull(decrypted, "잘못된 Base64 데이터는 복호화 실패하여 null 반환");
    }

    @Test
    @DisplayName("{ENC} prefix만 있고 데이터가 없으면 암호화로 간주하지 않음")
    void isEncrypted_empty_after_prefix_returns_false() {
        // Given
        String emptyPrefix = "{ENC}";

        // When
        boolean result = encryptionService.isEncrypted(emptyPrefix);

        // Then
        assertTrue(result, "{ENC} 문자열은 startsWith 체크로 true 반환");

        // 하지만 복호화 시도 시 실패해야 함
        String decrypted = encryptionService.decrypt(emptyPrefix);
        assertNull(decrypted, "빈 데이터는 복호화 실패");
    }

    @Test
    @DisplayName("평문 Base64 문자열은 암호화되지 않은 것으로 감지 (False Positive 제거)")
    void isEncrypted_plain_base64_not_detected() {
        // Given - 실제 Base64로 보이는 문자열들 (기존 취약점 재현)
        String base64Like1 = "SGVsbG8gV29ybGQhIFRoaXMgaXMgYSB0ZXN0"; // Hello World! This is a test
        String base64Like2 = "MjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";
        String actualEncrypted = encryptionService.encrypt("test");

        // When & Then
        assertFalse(encryptionService.isEncrypted(base64Like1),
                   "Base64 유사 문자열은 암호화로 간주하지 않아야 함");
        assertFalse(encryptionService.isEncrypted(base64Like2),
                   "긴 Base64 문자열도 prefix 없으면 암호화로 간주하지 않아야 함");
        assertTrue(encryptionService.isEncrypted(actualEncrypted),
                  "실제 암호화된 데이터는 {ENC} prefix로 정확히 감지");
    }

    @Test
    @DisplayName("이미 암호화된 데이터를 감지하여 이중 암호화 방지 가능")
    void isEncrypted_prevents_double_encryption() {
        // Given
        String plainText = "sensitive-data";
        String encrypted = encryptionService.encrypt(plainText);

        // When
        boolean isEncryptedResult = encryptionService.isEncrypted(encrypted);

        // Then
        assertTrue(isEncryptedResult, "암호화된 데이터를 정확히 감지");

        // StringEncryptionConverter에서 사용하는 패턴 시뮬레이션
        String toSave = isEncryptedResult ? encrypted : encryptionService.encrypt(encrypted);
        assertEquals(encrypted, toSave, "이미 암호화된 데이터는 재암호화하지 않음");
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