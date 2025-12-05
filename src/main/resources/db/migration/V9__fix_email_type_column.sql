-- Fix email_type column to ensure it's VARCHAR(50) and can store all enum values
-- Migration: V9__fix_email_type_column.sql

ALTER TABLE email_outbox
MODIFY COLUMN email_type VARCHAR(50) NOT NULL;
