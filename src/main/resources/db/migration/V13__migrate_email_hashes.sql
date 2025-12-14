-- V13: 기존 이메일 데이터에 대한 해시값 생성
-- ⚠️ 주의: SHA2 함수 사용 (MySQL 5.5+)

-- Users 테이블: email_hash 생성
UPDATE users 
SET email_hash = SHA2(CONCAT(LOWER(TRIM(email)), 'myDefaultSaltForDevelopmentOnly123456'), 256)
WHERE email_hash IS NULL 
  AND email IS NOT NULL 
  AND TRIM(email) != '';

-- Contracts 테이블: first_party_email_hash 생성
UPDATE contracts 
SET first_party_email_hash = SHA2(CONCAT(LOWER(TRIM(first_party_email)), 'myDefaultSaltForDevelopmentOnly123456'), 256)
WHERE first_party_email_hash IS NULL 
  AND first_party_email IS NOT NULL 
  AND TRIM(first_party_email) != '';

-- Contracts 테이블: second_party_email_hash 생성
UPDATE contracts 
SET second_party_email_hash = SHA2(CONCAT(LOWER(TRIM(second_party_email)), 'myDefaultSaltForDevelopmentOnly123456'), 256)
WHERE second_party_email_hash IS NULL 
  AND second_party_email IS NOT NULL 
  AND TRIM(second_party_email) != '';
