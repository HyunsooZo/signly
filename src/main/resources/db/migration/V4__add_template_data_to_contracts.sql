-- 계약서 테이블에 JSON 템플릿 데이터 컬럼 추가
-- 원본 JSON 구조를 보관하여 향후 재편집 가능하도록 함

ALTER TABLE contracts
    ADD COLUMN template_data JSON COMMENT '원본 JSON 구조 (선택적, 향후 재편집용)' AFTER content;
