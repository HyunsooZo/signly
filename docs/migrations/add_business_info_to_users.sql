-- ============================================
-- Migration: Add business_phone and business_address to users table
-- Date: 2025-10-10
-- Description: 사용자 테이블에 사업장 전화번호와 주소 컬럼 추가
-- ============================================

USE signly_db;

-- Add business_phone column
ALTER TABLE users
ADD COLUMN business_phone varchar(20) NULL AFTER company_name;

-- Add business_address column
ALTER TABLE users
ADD COLUMN business_address varchar(500) NULL AFTER business_phone;

-- Verify the changes
DESCRIBE users;
