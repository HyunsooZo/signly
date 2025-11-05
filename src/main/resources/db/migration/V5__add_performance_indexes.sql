-- 성능 최적화를 위한 인덱스 추가
-- Template 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_template_owner_id ON contract_templates(owner_id);
CREATE INDEX IF NOT EXISTS idx_template_status ON contract_templates(status);
CREATE INDEX IF NOT EXISTS idx_template_is_preset ON contract_templates(is_preset);
CREATE INDEX IF NOT EXISTS idx_template_preset_id ON contract_templates(preset_id);
CREATE INDEX IF NOT EXISTS idx_template_owner_status ON contract_templates(owner_id, status);
CREATE INDEX IF NOT EXISTS idx_template_created_at ON contract_templates(created_at);

-- User 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_user_type ON users(user_type);
CREATE INDEX IF NOT EXISTS idx_user_status_type ON users(status, user_type);
CREATE INDEX IF NOT EXISTS idx_user_created_at ON users(created_at);

-- Signature 관련 추가 인덱스
CREATE INDEX IF NOT EXISTS idx_signature_contract_signer ON signatures(contract_id, signer_email);
CREATE INDEX IF NOT EXISTS idx_signature_contract_signed ON signatures(contract_id, signed_at);

-- Contract 관련 추가 인덱스
CREATE INDEX IF NOT EXISTS idx_contract_creator_status ON contracts(creator_id, status);
CREATE INDEX IF NOT EXISTS idx_contract_status_expires ON contracts(status, expires_at);
CREATE INDEX IF NOT EXISTS idx_contract_party_emails ON contracts(first_party_email, second_party_email);

-- Document 관련 추가 인덱스 (기존 인덱스 외에 추가)
CREATE INDEX IF NOT EXISTS idx_document_contract_created ON documents(contract_id, created_at);
CREATE INDEX IF NOT EXISTS idx_document_uploaded_created ON documents(uploaded_by, created_at);