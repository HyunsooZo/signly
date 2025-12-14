-- V12: email_hash 컬럼 추가 (nullable로 시작)
-- 이메일 해시 기반 검색을 위한 Blind Index 패턴 적용

-- 1. Users 테이블: email_hash 컬럼 추가
ALTER TABLE users ADD COLUMN email_hash VARCHAR(64) NULL COMMENT 'SHA-256 해시값 (검색용, Blind Index)';

-- 2. Contracts 테이블: 당사자 이메일 해시 추가
ALTER TABLE contracts ADD COLUMN first_party_email_hash VARCHAR(64) NULL COMMENT '갑(First Party) 이메일 해시';
ALTER TABLE contracts ADD COLUMN second_party_email_hash VARCHAR(64) NULL COMMENT '을(Second Party) 이메일 해시';

-- 인덱스는 데이터 마이그레이션 후 V14에서 추가
