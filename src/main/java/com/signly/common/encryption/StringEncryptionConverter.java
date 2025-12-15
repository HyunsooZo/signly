package com.signly.common.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
@RequiredArgsConstructor
public class StringEncryptionConverter implements AttributeConverter<String, String> {

    private final AesEncryptionService encryptionService;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return encryptionService.encrypt(attribute);
        } catch (Exception e) {
            log.error("Failed to encrypt attribute for database storage", e);
            throw new RuntimeException("Encryption failed during database conversion", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        if (!encryptionService.isEncrypted(dbData)) {
            return dbData;
        }

        try {
            String decrypted = encryptionService.decrypt(dbData);
            if (decrypted == null) {
                log.warn("Failed to decrypt database data, returning original value");
                return dbData;
            }
            return decrypted;
        } catch (Exception e) {
            log.error("Failed to decrypt database data", e);
            return dbData;
        }
    }
}