-- V17: 계정 잠금 기능 추가
-- Description: 로그인 실패 추적 및 계정 잠금 기능을 위한 컬럼 추가

-- users 테이블에 계정 잠금 관련 컬럼 추가
ALTER TABLE users
    ADD COLUMN failed_login_attempts INT DEFAULT 0 NOT NULL COMMENT '로그인 실패 횟수',
    ADD COLUMN last_failed_login_at DATETIME(6) NULL COMMENT '마지막 로그인 실패 시각',
    ADD COLUMN account_locked_at DATETIME(6) NULL COMMENT '계정 잠금 시각',
    ADD COLUMN unlock_token VARCHAR(255) NULL COMMENT '계정 해제 토큰',
    ADD COLUMN unlock_token_expiry DATETIME(6) NULL COMMENT '해제 토큰 만료 시각';

-- UserStatus enum에 LOCKED 추가
ALTER TABLE users
    MODIFY COLUMN status ENUM('PENDING', 'ACTIVE', 'INACTIVE', 'SUSPENDED', 'LOCKED') NOT NULL;

-- 인덱스 추가 (성능 최적화)
CREATE INDEX idx_unlock_token ON users (unlock_token);
CREATE INDEX idx_account_locked ON users (status, account_locked_at);

-- 기존 사용자 데이터는 failed_login_attempts가 0으로 자동 설정됨
