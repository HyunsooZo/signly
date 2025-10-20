-- Migration: add file path column for contract signatures
-- Applies to MySQL 8+ / MariaDB
ALTER TABLE contract_signatures
    ADD COLUMN signature_path VARCHAR(1000);
