-- 계약서 테이블에 PDF 파일 경로 컬럼 추가
ALTER TABLE contracts
    ADD COLUMN IF NOT EXISTS pdf_path VARCHAR(1000) COMMENT '완료된 계약서 PDF 파일 경로';

-- 인덱스는 별도로 추가 (IF NOT EXISTS 없이)
CREATE INDEX idx_contract_pdf_path ON contracts(pdf_path);
