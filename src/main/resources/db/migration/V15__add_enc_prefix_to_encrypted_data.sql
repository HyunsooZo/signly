-- ========================================
-- Migration V15: Add {ENC} Prefix to Encrypted Fields
-- ========================================
-- Description: Add {ENC} prefix to all existing encrypted data
-- for reliable detection in AesEncryptionService.isEncrypted()
-- Date: 2025-12-19
-- Author: Claude Code
-- ========================================

-- Helper function to detect if string is valid Base64 and should have prefix added
-- Returns 1 if should add prefix, 0 otherwise
DELIMITER $$

CREATE FUNCTION is_encryptable(input_str TEXT)
RETURNS TINYINT DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE result TINYINT DEFAULT 0;

    -- Check conditions:
    -- 1. String is not NULL
    -- 2. String doesn't already have {ENC} prefix
    -- 3. String length > 20 (reasonable minimum for Base64 + IV)
    -- 4. String matches Base64 pattern (A-Za-z0-9+/= only)
    IF input_str IS NOT NULL
       AND NOT input_str LIKE '{ENC}%'
       AND CHAR_LENGTH(input_str) > 20
       AND input_str REGEXP '^[A-Za-z0-9+/]+={0,2}$'
    THEN
        SET result = 1;
    END IF;

    RETURN result;
END$$

DELIMITER ;

-- ========================================
-- Table: users (5 fields)
-- ========================================

UPDATE users
SET email = CONCAT('{ENC}', email)
WHERE is_encryptable(email) = 1;

UPDATE users
SET name = CONCAT('{ENC}', name)
WHERE is_encryptable(name) = 1;

UPDATE users
SET company_name = CONCAT('{ENC}', company_name)
WHERE is_encryptable(company_name) = 1;

UPDATE users
SET business_phone = CONCAT('{ENC}', business_phone)
WHERE is_encryptable(business_phone) = 1;

UPDATE users
SET business_address = CONCAT('{ENC}', business_address)
WHERE is_encryptable(business_address) = 1;

-- ========================================
-- Table: contracts (10 fields)
-- ========================================

UPDATE contracts
SET title = CONCAT('{ENC}', title)
WHERE is_encryptable(title) = 1;

UPDATE contracts
SET content = CONCAT('{ENC}', content)
WHERE is_encryptable(content) = 1;

UPDATE contracts
SET template_data = CONCAT('{ENC}', template_data)
WHERE is_encryptable(template_data) = 1;

UPDATE contracts
SET first_party_name = CONCAT('{ENC}', first_party_name)
WHERE is_encryptable(first_party_name) = 1;

UPDATE contracts
SET first_party_email = CONCAT('{ENC}', first_party_email)
WHERE is_encryptable(first_party_email) = 1;

UPDATE contracts
SET first_party_organization = CONCAT('{ENC}', first_party_organization)
WHERE is_encryptable(first_party_organization) = 1;

UPDATE contracts
SET second_party_name = CONCAT('{ENC}', second_party_name)
WHERE is_encryptable(second_party_name) = 1;

UPDATE contracts
SET second_party_email = CONCAT('{ENC}', second_party_email)
WHERE is_encryptable(second_party_email) = 1;

UPDATE contracts
SET second_party_organization = CONCAT('{ENC}', second_party_organization)
WHERE is_encryptable(second_party_organization) = 1;

UPDATE contracts
SET pdf_path = CONCAT('{ENC}', pdf_path)
WHERE is_encryptable(pdf_path) = 1;

-- ========================================
-- Table: contract_signatures (5 fields)
-- ========================================

UPDATE contract_signatures
SET signer_name = CONCAT('{ENC}', signer_name)
WHERE is_encryptable(signer_name) = 1;

UPDATE contract_signatures
SET signature_data = CONCAT('{ENC}', signature_data)
WHERE is_encryptable(signature_data) = 1;

UPDATE contract_signatures
SET ip_address = CONCAT('{ENC}', ip_address)
WHERE is_encryptable(ip_address) = 1;

UPDATE contract_signatures
SET device_info = CONCAT('{ENC}', device_info)
WHERE is_encryptable(device_info) = 1;

UPDATE contract_signatures
SET signature_path = CONCAT('{ENC}', signature_path)
WHERE is_encryptable(signature_path) = 1;

-- ========================================
-- Table: first_party_signatures (2 fields)
-- ========================================

UPDATE first_party_signatures
SET storage_path = CONCAT('{ENC}', storage_path)
WHERE is_encryptable(storage_path) = 1;

UPDATE first_party_signatures
SET original_filename = CONCAT('{ENC}', original_filename)
WHERE is_encryptable(original_filename) = 1;

-- ========================================
-- Table: documents (3 fields)
-- ========================================

UPDATE documents
SET filename = CONCAT('{ENC}', filename)
WHERE is_encryptable(filename) = 1;

UPDATE documents
SET original_filename = CONCAT('{ENC}', original_filename)
WHERE is_encryptable(original_filename) = 1;

UPDATE documents
SET storage_path = CONCAT('{ENC}', storage_path)
WHERE is_encryptable(storage_path) = 1;

-- ========================================
-- Table: email_outbox (2 fields)
-- ========================================

UPDATE email_outbox
SET template_variables = CONCAT('{ENC}', template_variables)
WHERE is_encryptable(template_variables) = 1;

UPDATE email_outbox
SET attachments = CONCAT('{ENC}', attachments)
WHERE is_encryptable(attachments) = 1;

-- ========================================
-- Cleanup
-- ========================================
DROP FUNCTION IF EXISTS is_encryptable;

-- ========================================
-- Migration completed
-- All encrypted fields now have {ENC} prefix for reliable detection
-- ========================================
