package com.signly.common.encryption;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AesEncryptionService {

    private final EncryptionProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    private SecretKey secretKey;
    
    private SecretKey getSecretKey() {
        if (secretKey == null) {
            String keyString = properties.getSecretKey();
            byte[] keyBytes = Base64.getDecoder().decode(keyString);
            
            if (keyBytes.length != 32) {
                throw new IllegalArgumentException("AES-256 requires exactly 32 bytes (256 bits) key. Got " + keyBytes.length + " bytes.");
            }
            
            secretKey = new SecretKeySpec(keyBytes, "AES");
        }
        return secretKey;
    }
    
    public String encrypt(String plainText) {
        if (!properties.isEnabled() || plainText == null) {
            return plainText;
        }
        
        try {
            byte[] iv = generateRandomIV();
            
            Cipher cipher = Cipher.getInstance(properties.getAlgorithm());
            GCMParameterSpec spec = new GCMParameterSpec(properties.getTagLength(), iv);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), spec);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return Base64.getEncoder().encodeToString(combined);
            
        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    public String decrypt(String encryptedText) {
        if (!properties.isEnabled() || encryptedText == null) {
            return encryptedText;
        }
        
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            
            if (decoded.length < properties.getIvLength()) {
                log.warn("Invalid encrypted data: too short");
                return null;
            }
            
            byte[] iv = new byte[properties.getIvLength()];
            byte[] encrypted = new byte[decoded.length - properties.getIvLength()];
            
            System.arraycopy(decoded, 0, iv, 0, properties.getIvLength());
            System.arraycopy(decoded, properties.getIvLength(), encrypted, 0, encrypted.length);
            
            Cipher cipher = Cipher.getInstance(properties.getAlgorithm());
            GCMParameterSpec spec = new GCMParameterSpec(properties.getTagLength(), iv);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.warn("Failed to decrypt data: {}", e.getMessage());
            return null;
        }
    }
    
    private byte[] generateRandomIV() {
        byte[] iv = new byte[properties.getIvLength()];
        secureRandom.nextBytes(iv);
        return iv;
    }
    
    public boolean isEncrypted(String text) {
        if (text == null || !properties.isEnabled()) {
            return false;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(text);
            return decoded.length >= properties.getIvLength();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 검색용 단방향 해시 생성 (SHA-256 + Salt)
     *
     * ⚠️ 중요: 이메일의 경우 반드시 정규화 후 호출해야 함
     * 예: hash(email.toLowerCase().trim())
     */
    public String hash(String rawText) {
        if (rawText == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Salt를 섞어서 레인보우 테이블 공격 방지
            String input = rawText + properties.getSalt();
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Hex String 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to generate hash", e);
            throw new RuntimeException("Hash generation failed", e);
        }
    }

    /**
     * 이메일 전용 해시 생성 (자동 정규화 포함)
     * 이메일은 case-insensitive이므로 소문자로 정규화 필요
     */
    public String hashEmail(String email) {
        if (email == null) {
            return null;
        }
        // 이메일 정규화: 소문자 + 공백 제거
        String normalized = email.toLowerCase().trim();
        return hash(normalized);
    }
}