-- Create audit_logs table for comprehensive audit logging
-- This table tracks all important operations for legal compliance

CREATE TABLE audit_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL COMMENT 'Entity type: CONTRACT, TEMPLATE, USER, etc.',
    entity_id VARCHAR(26) NOT NULL COMMENT 'Entity ID (ULID format)',
    action VARCHAR(50) NOT NULL COMMENT 'Action performed: CREATED, UPDATED, DELETED, etc.',
    user_id VARCHAR(26) NULL COMMENT 'User who performed the action (ULID format)',
    ip_address VARCHAR(45) NULL COMMENT 'Client IP address (supports IPv6)',
    user_agent TEXT NULL COMMENT 'Browser/client user agent',
    details JSON NULL COMMENT 'Detailed information about the action (before/after/metadata)',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'When the action occurred',
    
    -- Indexes for efficient querying
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_user_action (user_id, action),
    INDEX idx_audit_created_at (created_at),
    INDEX idx_audit_action (action),
    INDEX idx_audit_entity_action (entity_type, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Audit log for legal compliance and security tracking';

-- Add foreign key constraints for data integrity
ALTER TABLE audit_logs 
ADD CONSTRAINT fk_audit_user 
FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL;