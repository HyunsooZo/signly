-- 계약서 테이블에 PDF 파일 경로 컬럼 추가
ALTER TABLE contracts
    ADD COLUMN pdf_path VARCHAR(1000) COMMENT '완료된 계약서 PDF 파일 경로';

-- 인덱스는 prefix 길이 제한과 함께 추가 (MySQL 인덱스 키 길이 제한 3072 bytes 고려)
CREATE INDEX IF NOT EXISTS idx_contract_pdf_path ON contracts(pdf_path(255));
