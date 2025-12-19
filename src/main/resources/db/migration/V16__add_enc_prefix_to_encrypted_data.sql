-- ========================================
-- Migration V15: Add {ENC} Prefix to Encrypted Fields
-- ========================================
-- Description: Add {ENC} prefix to all existing encrypted data
-- for reliable detection in AesEncryptionService.isEncrypted()
-- Date: 2025-12-19
-- Author: Claude Code
-- ========================================
-- Note: This migration uses direct WHERE conditions instead of
-- a helper function to avoid DELIMITER issues with Flyway
-- ========================================

-- ========================================
-- Table: users (5 fields)
-- ========================================

UPDATE users
SET email = CONCAT('{ENC}', email)
WHERE email IS NOT NULL
  AND email NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(email) > 20
  AND email REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE users
SET name = CONCAT('{ENC}', name)
WHERE name IS NOT NULL
  AND name NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(name) > 20
  AND name REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE users
SET company_name = CONCAT('{ENC}', company_name)
WHERE company_name IS NOT NULL
  AND company_name NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(company_name) > 20
  AND company_name REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE users
SET business_phone = CONCAT('{ENC}', business_phone)
WHERE business_phone IS NOT NULL
  AND business_phone NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(business_phone) > 20
  AND business_phone REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE users
SET business_address = CONCAT('{ENC}', business_address)
WHERE business_address IS NOT NULL
  AND business_address NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(business_address) > 20
  AND business_address REGEXP '^[A-Za-z0-9+/]+={0,2}$';

-- ========================================
-- Table: contracts (10 fields)
-- ========================================

UPDATE contracts
SET title = CONCAT('{ENC}', title)
WHERE title IS NOT NULL
  AND title NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(title) > 20
  AND title REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contracts
SET content = CONCAT('{ENC}', content)
WHERE content IS NOT NULL
  AND content NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(content) > 20
  AND content REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contracts
SET template_data = CONCAT('{ENC}', template_data)
WHERE template_data IS NOT NULL
  AND template_data NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(template_data) > 20
  AND template_data REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contracts
SET first_party_name = CONCAT('{ENC}', first_party_name)
WHERE first_party_name IS NOT NULL
  AND first_party_name NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(first_party_name) > 20
  AND first_party_name REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contracts
SET first_party_email = CONCAT('{ENC}', first_party_email)
WHERE first_party_email IS NOT NULL
  AND first_party_email NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(first_party_email) > 20
  AND first_party_email REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contracts
SET first_party_organization = CONCAT('{ENC}', first_party_organization)
WHERE first_party_organization IS NOT NULL
  AND first_party_organization NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(first_party_organization) > 20
  AND first_party_organization REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contracts
SET second_party_name = CONCAT('{ENC}', second_party_name)
WHERE second_party_name IS NOT NULL
  AND second_party_name NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(second_party_name) > 20
  AND second_party_name REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contracts
SET second_party_email = CONCAT('{ENC}', second_party_email)
WHERE second_party_email IS NOT NULL
  AND second_party_email NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(second_party_email) > 20
  AND second_party_email REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contracts
SET second_party_organization = CONCAT('{ENC}', second_party_organization)
WHERE second_party_organization IS NOT NULL
  AND second_party_organization NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(second_party_organization) > 20
  AND second_party_organization REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contracts
SET pdf_path = CONCAT('{ENC}', pdf_path)
WHERE pdf_path IS NOT NULL
  AND pdf_path NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(pdf_path) > 20
  AND pdf_path REGEXP '^[A-Za-z0-9+/]+={0,2}$';

-- ========================================
-- Table: contract_signatures (5 fields)
-- ========================================

UPDATE contract_signatures
SET signer_name = CONCAT('{ENC}', signer_name)
WHERE signer_name IS NOT NULL
  AND signer_name NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(signer_name) > 20
  AND signer_name REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contract_signatures
SET signature_data = CONCAT('{ENC}', signature_data)
WHERE signature_data IS NOT NULL
  AND signature_data NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(signature_data) > 20
  AND signature_data REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contract_signatures
SET ip_address = CONCAT('{ENC}', ip_address)
WHERE ip_address IS NOT NULL
  AND ip_address NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(ip_address) > 20
  AND ip_address REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contract_signatures
SET device_info = CONCAT('{ENC}', device_info)
WHERE device_info IS NOT NULL
  AND device_info NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(device_info) > 20
  AND device_info REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE contract_signatures
SET signature_path = CONCAT('{ENC}', signature_path)
WHERE signature_path IS NOT NULL
  AND signature_path NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(signature_path) > 20
  AND signature_path REGEXP '^[A-Za-z0-9+/]+={0,2}$';

-- ========================================
-- Table: first_party_signatures (2 fields)
-- ========================================

UPDATE first_party_signatures
SET storage_path = CONCAT('{ENC}', storage_path)
WHERE storage_path IS NOT NULL
  AND storage_path NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(storage_path) > 20
  AND storage_path REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE first_party_signatures
SET original_filename = CONCAT('{ENC}', original_filename)
WHERE original_filename IS NOT NULL
  AND original_filename NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(original_filename) > 20
  AND original_filename REGEXP '^[A-Za-z0-9+/]+={0,2}$';

-- ========================================
-- Table: documents (3 fields)
-- ========================================

UPDATE documents
SET filename = CONCAT('{ENC}', filename)
WHERE filename IS NOT NULL
  AND filename NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(filename) > 20
  AND filename REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE documents
SET original_filename = CONCAT('{ENC}', original_filename)
WHERE original_filename IS NOT NULL
  AND original_filename NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(original_filename) > 20
  AND original_filename REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE documents
SET storage_path = CONCAT('{ENC}', storage_path)
WHERE storage_path IS NOT NULL
  AND storage_path NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(storage_path) > 20
  AND storage_path REGEXP '^[A-Za-z0-9+/]+={0,2}$';

-- ========================================
-- Table: email_outbox (2 fields)
-- ========================================

UPDATE email_outbox
SET template_variables = CONCAT('{ENC}', template_variables)
WHERE template_variables IS NOT NULL
  AND template_variables NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(template_variables) > 20
  AND template_variables REGEXP '^[A-Za-z0-9+/]+={0,2}$';

UPDATE email_outbox
SET attachments = CONCAT('{ENC}', attachments)
WHERE attachments IS NOT NULL
  AND attachments NOT LIKE '{ENC}%'
  AND CHAR_LENGTH(attachments) > 20
  AND attachments REGEXP '^[A-Za-z0-9+/]+={0,2}$';

-- ========================================
-- Migration completed
-- All encrypted fields now have {ENC} prefix for reliable detection
-- ========================================
