package com.signly.common.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
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
    void decrypt_invalid_ciphertext_returns_null() {
        // Given
        String invalidCipherText = "invalid-base64-string";

        // When
        String decrypted = encryptionService.decrypt(invalidCipherText);

        // Then
        assertNull(decrypted);
    }

    @Test
    void decrypt_too_short_ciphertext_returns_null() {
        // Given
        String shortCipherText = Base64.getEncoder().encodeToString("short".getBytes());

        // When
        String decrypted = encryptionService.decrypt(shortCipherText);

        // Then
        assertNull(decrypted);
    }

    @Test
    void encrypt_null_returns_null() {
        // When
        String encrypted = encryptionService.encrypt(null);

        // Then
        assertNull(encrypted);
    }

    @Test
    void decrypt_null_returns_null() {
        // When
        String decrypted = encryptionService.decrypt(null);

        // Then
        assertNull(decrypted);
    }

    @Test
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
    void isEncrypted_disabled_returns_false() {
        // Given
        properties.setEnabled(false);
        encryptionService = new AesEncryptionService(properties);
        
        String plainText = "010-1234-5678";

        // When & Then
        assertFalse(encryptionService.isEncrypted(plainText));
    }

    @Test
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
    void invalid_key_length_throws_exception() {
        // Given
        String shortKey = Base64.getEncoder().encodeToString("short".getBytes());
        properties.setSecretKey(shortKey);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new AesEncryptionService(properties);
        });
    }

    @Test
    void missing_key_throws_exception() {
        // Given
        properties.setSecretKey(null);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            new AesEncryptionService(properties);
        });
    }
}