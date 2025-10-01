-- 계약서 테이블에 preset_type 컬럼 추가
ALTER TABLE contracts
ADD COLUMN preset_type VARCHAR(50) NOT NULL DEFAULT 'NONE'
AFTER expires_at;

-- 인덱스 추가 (선택사항 - 프리셋 타입으로 필터링할 경우 유용)
CREATE INDEX idx_contract_preset_type ON contracts(preset_type);
