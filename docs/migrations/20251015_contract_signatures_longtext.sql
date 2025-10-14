-- Migration: expand contract_signatures payload columns for large signature images
-- Applies to MySQL 8+ / MariaDB
ALTER TABLE contract_signatures
    MODIFY COLUMN signature_data LONGTEXT NOT NULL,
    MODIFY COLUMN device_info LONGTEXT NULL;
