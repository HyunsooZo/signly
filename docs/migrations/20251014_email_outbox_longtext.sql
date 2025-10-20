-- Migration: expand email_outbox payload columns for large attachments
-- Applies to MySQL 8+ / MariaDB
ALTER TABLE email_outbox
    MODIFY COLUMN template_variables LONGTEXT NOT NULL,
    MODIFY COLUMN attachments LONGTEXT NULL;
