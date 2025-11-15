-- 계약서 테이블에 PDF 파일 경로 컬럼 추가
ALTER TABLE contracts
    ADD COLUMN pdf_path VARCHAR(1000) COMMENT '완료된 계약서 PDF 파일 경로';

-- 인덱스 추가 (PDF 경로로 조회 시 성능 향상)
CREATE INDEX idx_contract_pdf_path ON contracts (pdf_path);
