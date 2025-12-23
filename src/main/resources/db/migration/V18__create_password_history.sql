-- V18: 비밀번호 이력 테이블 생성
-- Description: 비밀번호 재사용 방지를 위한 이력 테이블 (90일 이내 최근 3개 재사용 금지)

CREATE TABLE IF NOT EXISTS password_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL COMMENT '사용자 ID (users.user_id FK)',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 해시',
    changed_at DATETIME(6) NOT NULL COMMENT '비밀번호 변경 시각',
    ip_address VARCHAR(45) NULL COMMENT '변경 시 IP 주소',
    user_agent TEXT NULL COMMENT 'User-Agent',
    created_at DATETIME(6) NOT NULL COMMENT '레코드 생성 시각',

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_changed (user_id, changed_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='비밀번호 변경 이력 (90일 이내 최근 3개 재사용 방지)';
