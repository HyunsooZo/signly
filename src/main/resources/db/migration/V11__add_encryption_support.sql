-- ========================================
-- Migration V11: Add Encryption Support
-- ========================================
-- Description: 민감 데이터 암호화를 위한 컬럼 길이 확장
-- Date: 2024-12-13
-- ========================================

-- 1. users 테이블 - 사업자 정보 암호화
ALTER TABLE users 
  MODIFY COLUMN business_phone VARCHAR(500) COMMENT '암호화된 사업자 전화번호',
  MODIFY COLUMN business_address VARCHAR(1000) COMMENT '암호화된 사업자 주소';

-- 2. contract_signatures 테이블 - 서명자 정보 암호화
ALTER TABLE contract_signatures
  MODIFY COLUMN signer_email VARCHAR(500) NOT NULL COMMENT '암호화된 서명자 이메일',
  MODIFY COLUMN signer_name VARCHAR(500) NOT NULL COMMENT '암호화된 서명자 이름',
  MODIFY COLUMN ip_address VARCHAR(200) COMMENT '암호화된 IP 주소';

-- LONGTEXT는 이미 충분하므로 변경 불필요
-- signature_data: LONGTEXT (암호화 후에도 충분)

-- 3. contracts 테이블 - 당사자 정보 암호화 (선택적)
ALTER TABLE contracts
  MODIFY COLUMN first_party_email VARCHAR(500) NOT NULL COMMENT '암호화된 갑 이메일',
  MODIFY COLUMN second_party_email VARCHAR(500) NOT NULL COMMENT '암호화된 을 이메일',
  MODIFY COLUMN first_party_name VARCHAR(500) NOT NULL COMMENT '암호화된 갑 이름',
  MODIFY COLUMN second_party_name VARCHAR(500) NOT NULL COMMENT '암호화된 을 이름';

-- 4. 인덱스 영향 검토
-- 주의: 암호화된 필드는 검색 성능이 저하됨
-- 필요시 해시 인덱스 컬럼 추가 고려 (예: email_hash)

-- 5. 기존 데이터 마이그레이션은 별도 스크립트로 처리
-- (선택사항: 기존 평문 데이터를 암호화하려면 애플리케이션 레벨에서 처리)