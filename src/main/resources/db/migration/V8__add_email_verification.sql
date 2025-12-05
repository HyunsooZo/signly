-- Add email verification columns to users table
-- Migration: V8__add_email_verification.sql

-- Add PENDING status to enum
ALTER TABLE users
MODIFY COLUMN status ENUM('PENDING', 'ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL;

-- Add email verification columns
ALTER TABLE users
ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE AFTER status,
ADD COLUMN verification_token VARCHAR(255) NULL AFTER email_verified,
ADD COLUMN verification_token_expiry DATETIME(6) NULL AFTER verification_token;

-- Add index for verification token lookup
CREATE INDEX idx_verification_token ON users (verification_token);

-- Update existing users to have email verified (backward compatibility)
UPDATE users 
SET email_verified = TRUE 
WHERE status = 'ACTIVE';
