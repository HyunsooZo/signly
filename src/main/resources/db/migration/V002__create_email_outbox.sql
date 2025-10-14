-- Email Outbox 테이블 생성 (Transactional Outbox Pattern)
CREATE TABLE email_outbox (
    id VARCHAR(26) PRIMARY KEY COMMENT 'ULID 형식의 고유 식별자',
    email_type VARCHAR(50) NOT NULL COMMENT '이메일 타입 (CONTRACT_SIGNING_REQUEST, CONTRACT_COMPLETED 등)',
    recipient_email VARCHAR(255) NOT NULL COMMENT '수신자 이메일',
    recipient_name VARCHAR(100) NOT NULL COMMENT '수신자 이름',
    template_variables TEXT NOT NULL COMMENT '템플릿 변수 (JSON)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '발송 상태 (PENDING, SENT, FAILED)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    max_retries INT NOT NULL DEFAULT 3 COMMENT '최대 재시도 횟수',
    error_message TEXT COMMENT '에러 메시지',
    created_at DATETIME(6) NOT NULL COMMENT '생성 시간',
    sent_at DATETIME(6) COMMENT '발송 완료 시간',
    next_retry_at DATETIME(6) COMMENT '다음 재시도 시간',
    INDEX idx_status_next_retry (status, next_retry_at),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='이메일 발송 Outbox 테이블';
