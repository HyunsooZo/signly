-- Signly E-Signature Database Schema
-- Generated: 2025-10-10
-- Database: MySQL 9.2
-- Character Set: utf8mb4
-- Collation: utf8mb4_unicode_ci
-- Note: All ID columns use ULID format (26 characters)

-- ============================================
-- Table: contract_signatures
-- Description: 계약서 서명 정보 (서명 데이터 포함)
-- ============================================
CREATE TABLE IF NOT EXISTS contract_signatures (
    created_at datetime(6) NOT NULL,
    signed_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    contract_id varchar(26) NOT NULL,
    signature_id varchar(26) NOT NULL,
    ip_address varchar(45),
    signer_name varchar(100) NOT NULL,
    device_info TEXT,
    signature_data TEXT NOT NULL,
    signer_email varchar(255) NOT NULL,
    PRIMARY KEY (signature_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: contract_templates
-- Description: 계약서 템플릿 (프리셋 및 사용자 정의 템플릿)
-- ============================================
CREATE TABLE IF NOT EXISTS contract_templates (
    is_preset bit NOT NULL,
    version integer NOT NULL,
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    owner_id varchar(26),
    template_id varchar(26) NOT NULL,
    preset_id varchar(100),
    content JSON NOT NULL,
    title varchar(255) NOT NULL,
    status enum ('ACTIVE','ARCHIVED','DRAFT') NOT NULL,
    PRIMARY KEY (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: contracts
-- Description: 계약서 정보
-- ============================================
CREATE TABLE IF NOT EXISTS contracts (
    created_at datetime(6) NOT NULL,
    expires_at datetime(6),
    updated_at datetime(6) NOT NULL,
    creator_id varchar(26) NOT NULL,
    id varchar(26) NOT NULL,
    sign_token varchar(26) NOT NULL,
    template_id varchar(26),
    first_party_name varchar(100) NOT NULL,
    second_party_name varchar(100) NOT NULL,
    first_party_organization varchar(200),
    second_party_organization varchar(200),
    title varchar(200) NOT NULL,
    first_party_email varchar(255) NOT NULL,
    second_party_email varchar(255) NOT NULL,
    content TEXT NOT NULL,
    preset_type enum ('LABOR_STANDARD','NONE') NOT NULL,
    status enum ('CANCELLED','COMPLETED','DRAFT','EXPIRED','PENDING','SIGNED') NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UK_sign_token (sign_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: documents
-- Description: 문서 파일 (계약서 PDF, 첨부파일 등)
-- ============================================
CREATE TABLE IF NOT EXISTS documents (
    created_at datetime(6),
    file_size bigint NOT NULL,
    updated_at datetime(6),
    contract_id varchar(26) NOT NULL,
    id varchar(26) NOT NULL,
    uploaded_by varchar(26) NOT NULL,
    content_type varchar(100) NOT NULL,
    filename varchar(500) NOT NULL,
    original_filename varchar(500) NOT NULL,
    storage_path varchar(1000) NOT NULL,
    checksum varchar(255) NOT NULL,
    type enum ('ATTACHMENT','CONTRACT_PDF','SIGNATURE_IMAGE') NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: email_outbox
-- Description: 이메일 발송 아웃박스 (재시도 포함)
-- ============================================
CREATE TABLE IF NOT EXISTS email_outbox (
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    sent_at datetime(6),
    next_retry_at datetime(6),
    retry_count integer NOT NULL,
    max_retries integer NOT NULL,
    id varchar(26) NOT NULL,
    email_type varchar(50) NOT NULL,
    recipient_email varchar(255) NOT NULL,
    recipient_name varchar(100) NOT NULL,
    status enum ('PENDING','SENT','FAILED') NOT NULL,
    template_variables LONGTEXT NOT NULL,
    attachments LONGTEXT,
    error_message TEXT,
    PRIMARY KEY (id),
    KEY idx_status_next_retry (status, next_retry_at),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: first_party_signatures
-- Description: 1st Party (계약 작성자) 서명 이미지
-- ============================================
CREATE TABLE IF NOT EXISTS first_party_signatures (
    created_at datetime(6) NOT NULL,
    file_size bigint NOT NULL,
    updated_at datetime(6) NOT NULL,
    owner_id varchar(26) NOT NULL,
    signature_id varchar(26) NOT NULL,
    mime_type varchar(100) NOT NULL,
    storage_path varchar(512) NOT NULL,
    checksum varchar(255) NOT NULL,
    original_filename varchar(255) NOT NULL,
    PRIMARY KEY (signature_id),
    UNIQUE KEY UK_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: signatures
-- Description: 서명 이력 (auto-increment ID 사용)
-- ============================================
CREATE TABLE IF NOT EXISTS signatures (
    created_at datetime(6) NOT NULL,
    id bigint NOT NULL AUTO_INCREMENT,
    signed_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    contract_id varchar(26) NOT NULL,
    ip_address varchar(45) NOT NULL,
    signer_name varchar(100) NOT NULL,
    signer_email varchar(255) NOT NULL,
    signature_data TEXT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_signatures_contract FOREIGN KEY (contract_id) REFERENCES contracts (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: users
-- Description: 사용자 정보
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    created_at datetime(6) NOT NULL,
    updated_at datetime(6) NOT NULL,
    user_id varchar(26) NOT NULL,
    name varchar(100) NOT NULL,
    company_name varchar(200),
    business_phone varchar(20),
    business_address varchar(500),
    email varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    status enum ('ACTIVE','INACTIVE','SUSPENDED') NOT NULL,
    user_type enum ('CONTRACTOR','OWNER') NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY UK_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Indexes
-- ============================================

-- Contracts table indexes
CREATE INDEX idx_contract_creator_id ON contracts (creator_id);
CREATE INDEX idx_contract_template_id ON contracts (template_id);
CREATE INDEX idx_contract_status ON contracts (status);
CREATE INDEX idx_contract_first_party_email ON contracts (first_party_email);
CREATE INDEX idx_contract_second_party_email ON contracts (second_party_email);
CREATE INDEX idx_contract_expires_at ON contracts (expires_at);
CREATE INDEX idx_contract_sign_token ON contracts (sign_token);

-- Signatures table indexes
CREATE INDEX idx_signature_contract_id ON signatures (contract_id);
CREATE INDEX idx_signature_signer_email ON signatures (signer_email);
CREATE INDEX idx_signature_signed_at ON signatures (signed_at);

-- ============================================
-- Notes
-- ============================================
-- 1. All entity IDs (except signatures.id) use ULID format (26 characters)
-- 2. ULID provides lexicographic sorting and includes timestamp information
-- 3. Foreign key constraints are defined where necessary
-- 4. Enum types are used for status and type fields
-- 5. JSON type is used for template content storage
