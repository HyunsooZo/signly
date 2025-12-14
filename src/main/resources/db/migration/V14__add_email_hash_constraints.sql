-- V14: email_hash 제약조건 및 인덱스 추가
-- ⚠️ 주의: V13 마이그레이션 완료 후 실행해야 함

-- 1. Users 테이블
-- 기존 email 인덱스 제거 (존재하면)
SET @drop_index = IF((SELECT COUNT(*) FROM information_schema.statistics 
    WHERE table_schema = DATABASE() AND table_name = 'users' AND index_name = 'idx_user_email') > 0,
    'ALTER TABLE users DROP INDEX idx_user_email', 
    'SELECT 1');
PREPARE stmt FROM @drop_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- email_hash를 NOT NULL로 변경
ALTER TABLE users MODIFY COLUMN email_hash VARCHAR(64) NOT NULL;

-- email_hash에 UNIQUE 제약조건 추가 (이메일 중복 방지)
ALTER TABLE users ADD CONSTRAINT uk_users_email_hash UNIQUE (email_hash);

-- 2. Contracts 테이블
-- 인덱스 추가 (검색 성능 향상)
CREATE INDEX idx_contract_first_party_hash ON contracts(first_party_email_hash);
CREATE INDEX idx_contract_second_party_hash ON contracts(second_party_email_hash);
CREATE INDEX idx_contract_party_hashes ON contracts(first_party_email_hash, second_party_email_hash);

-- 3. Email 컬럼 암호화 준비
-- email 컬럼 길이 확장 (암호화 시 길이 증가: AES-256 + Base64)
ALTER TABLE users MODIFY COLUMN email VARCHAR(500) NOT NULL;
ALTER TABLE contracts MODIFY COLUMN first_party_email VARCHAR(500) NOT NULL;
ALTER TABLE contracts MODIFY COLUMN second_party_email VARCHAR(500) NOT NULL;
