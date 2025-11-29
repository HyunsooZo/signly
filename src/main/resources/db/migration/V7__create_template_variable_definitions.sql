-- 템플릿 변수 정의 테이블 생성
CREATE TABLE template_variable_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    variable_name VARCHAR(100) NOT NULL UNIQUE COMMENT '변수명 (예: EMPLOYEE, WORK_START_TIME)',
    display_name VARCHAR(100) NOT NULL COMMENT '표시명 (예: 근로자, 근무시작시간)',
    category VARCHAR(50) NOT NULL COMMENT '카테고리 (EMPLOYEE_INFO, WORK_CONDITION 등)',
    variable_type VARCHAR(20) NOT NULL COMMENT '변수 타입 (TEXT, TIME, DATE, EMAIL, IMAGE 등)',
    description TEXT COMMENT '변수 설명',
    icon_class VARCHAR(50) COMMENT '아이콘 클래스 (bi-person, bi-clock 등)',
    
    -- UI 렌더링 정보
    input_size INT DEFAULT 10 COMMENT 'input size 속성',
    max_length INT COMMENT 'maxlength 속성',
    placeholder_example VARCHAR(200) COMMENT '플레이스홀더 예시 (예: 예) 09:00)',
    
    -- 검증 규칙
    is_required BOOLEAN DEFAULT false COMMENT '필수 여부',
    validation_rule VARCHAR(500) COMMENT '정규식 검증 규칙',
    validation_message VARCHAR(200) COMMENT '검증 실패 시 에러 메시지',
    
    -- 기본값
    default_value TEXT COMMENT '기본값',
    
    -- 메타데이터
    display_order INT DEFAULT 0 COMMENT '표시 순서',
    is_active BOOLEAN DEFAULT true COMMENT '활성화 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    
    INDEX idx_category (category),
    INDEX idx_variable_type (variable_type),
    INDEX idx_display_order (display_order),
    INDEX idx_variable_name (variable_name),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='템플릿 변수 정의';

-- 1. 근로자 정보
INSERT INTO template_variable_definition 
(variable_name, display_name, category, variable_type, icon_class, input_size, max_length, placeholder_example, display_order) VALUES
('EMPLOYEE', '근로자', 'EMPLOYEE_INFO', 'TEXT', 'bi-person', 6, 10, '예) 홍길동', 10),
('EMPLOYEE_NAME', '근로자명', 'EMPLOYEE_INFO', 'TEXT', 'bi-person', 6, 10, '예) 홍길동', 11),
('EMPLOYEE_ADDRESS', '근로자주소', 'EMPLOYEE_INFO', 'TEXT', 'bi-geo-alt', 20, 100, '예) 서울시 강남구', 12),
('EMPLOYEE_PHONE', '근로자연락처', 'EMPLOYEE_INFO', 'PHONE', 'bi-telephone', 13, 15, '예) 010-1234-5678', 13),
('EMPLOYEE_ID', '주민등록번호', 'EMPLOYEE_INFO', 'TEXT', 'bi-person-badge', 14, 14, '예) 900101-1******', 14),
('EMPLOYEE_EMAIL', '근로자이메일', 'EMPLOYEE_INFO', 'EMAIL', 'bi-envelope', 20, 50, '예) hong@example.com', 15);

-- 2. 사업주 정보
INSERT INTO template_variable_definition 
(variable_name, display_name, category, variable_type, icon_class, input_size, max_length, placeholder_example, display_order) VALUES
('EMPLOYER', '사업주', 'EMPLOYER_INFO', 'TEXT', 'bi-person-badge', 6, 10, '예) 김철수', 20),
('EMPLOYER_NAME', '사업주명', 'EMPLOYER_INFO', 'TEXT', 'bi-person-badge', 6, 10, '예) 김철수', 21),
('COMPANY_NAME', '회사명', 'EMPLOYER_INFO', 'TEXT', 'bi-building', 15, 50, '예) (주)테크컴퍼니', 22),
('EMPLOYER_ADDRESS', '사업주주소', 'EMPLOYER_INFO', 'TEXT', 'bi-geo-alt', 20, 100, '예) 서울시 강남구', 23),
('EMPLOYER_PHONE', '사업주전화', 'EMPLOYER_INFO', 'PHONE', 'bi-telephone', 13, 15, '예) 02-1234-5678', 24),
('BUSINESS_NUMBER', '사업자번호', 'EMPLOYER_INFO', 'TEXT', 'bi-card-text', 12, 12, '예) 123-45-67890', 25);

-- 3. 계약 정보
INSERT INTO template_variable_definition 
(variable_name, display_name, category, variable_type, icon_class, input_size, max_length, placeholder_example, display_order) VALUES
('CONTRACT_DATE', '계약일', 'CONTRACT_INFO', 'DATE', 'bi-calendar-event', 11, 10, '예) 2025-01-01', 30),
('CONTRACT_START_DATE', '시작일', 'CONTRACT_INFO', 'DATE', 'bi-calendar-check', 11, 10, '예) 2025-01-01', 31),
('CONTRACT_END_DATE', '종료일', 'CONTRACT_INFO', 'DATE', 'bi-calendar-x', 11, 10, '예) 2025-12-31', 32),
('WORKPLACE', '근무장소', 'CONTRACT_INFO', 'TEXT', 'bi-geo-alt', 20, 50, '예) 본사 사무실', 33),
('JOB_DESCRIPTION', '업무내용', 'CONTRACT_INFO', 'TEXTAREA', 'bi-briefcase', 20, 200, '예) 소프트웨어 개발', 34);

-- 4. 근무 조건
INSERT INTO template_variable_definition 
(variable_name, display_name, category, variable_type, icon_class, input_size, max_length, placeholder_example, validation_rule, validation_message, display_order) VALUES
('WORK_START_TIME', '근무시작시간', 'WORK_CONDITION', 'TIME', 'bi-clock', 6, 5, '예) 09:00', '^([01]?[0-9]|2[0-3]):[0-5][0-9]$', '시간 형식이 올바르지 않습니다 (HH:MM)', 40),
('WORK_END_TIME', '근무종료시간', 'WORK_CONDITION', 'TIME', 'bi-clock-fill', 6, 5, '예) 18:00', '^([01]?[0-9]|2[0-3]):[0-5][0-9]$', '시간 형식이 올바르지 않습니다 (HH:MM)', 41),
('BREAK_START_TIME', '휴게시작시간', 'WORK_CONDITION', 'TIME', 'bi-cup-hot', 6, 5, '예) 12:00', '^([01]?[0-9]|2[0-3]):[0-5][0-9]$', '시간 형식이 올바르지 않습니다 (HH:MM)', 42),
('BREAK_END_TIME', '휴게종료시간', 'WORK_CONDITION', 'TIME', 'bi-cup-hot-fill', 6, 5, '예) 13:00', '^([01]?[0-9]|2[0-3]):[0-5][0-9]$', '시간 형식이 올바르지 않습니다 (HH:MM)', 43),
('WORK_DAYS', '근무일수', 'WORK_CONDITION', 'TEXT', 'bi-calendar2-week', 10, 20, '예) 월~금', NULL, NULL, 44),
('HOLIDAYS', '휴일', 'WORK_CONDITION', 'TEXT', 'bi-calendar2-x', 10, 20, '예) 토, 일요일', NULL, NULL, 45);

-- 5. 임금 정보
INSERT INTO template_variable_definition 
(variable_name, display_name, category, variable_type, icon_class, input_size, max_length, placeholder_example, display_order) VALUES
('MONTHLY_SALARY', '월급', 'SALARY_INFO', 'CURRENCY', 'bi-cash', 12, 20, '예) 3,000,000', 50),
('HOURLY_WAGE', '시급', 'SALARY_INFO', 'CURRENCY', 'bi-cash-coin', 12, 20, '예) 15,000', 51),
('BONUS', '상여금', 'SALARY_INFO', 'TEXT', 'bi-gift', 15, 50, '예) 연 500만원', 52),
('OTHER_ALLOWANCES', '기타수당', 'SALARY_INFO', 'TEXT', 'bi-wallet2', 15, 50, '예) 식대 10만원', 53),
('PAYMENT_DAY', '지급일', 'SALARY_INFO', 'TEXT', 'bi-calendar-day', 5, 10, '예) 25', 54),
('PAYMENT_METHOD', '지급방법', 'SALARY_INFO', 'TEXT', 'bi-credit-card', 15, 30, '예) 계좌이체', 55);

-- 6. 서명 (IMAGE 타입)
INSERT INTO template_variable_definition 
(variable_name, display_name, category, variable_type, icon_class, input_size, max_length, placeholder_example, display_order) VALUES
('EMPLOYER_SIGNATURE', '사업주서명', 'SIGNATURE', 'TEXT', 'bi-pen', 10, 20, '예) (서명)', 60),
('EMPLOYEE_SIGNATURE', '근로자서명', 'SIGNATURE', 'TEXT', 'bi-pen-fill', 10, 20, '예) (서명)', 61),
('EMPLOYER_SIGNATURE_IMAGE', '사업주서명이미지', 'SIGNATURE', 'IMAGE', 'bi-pen', 0, 0, '', 62),
('EMPLOYEE_SIGNATURE_IMAGE', '근로자서명이미지', 'SIGNATURE', 'IMAGE', 'bi-pen-fill', 0, 0, '', 63),
('SIGNATURE_DATE', '서명일', 'SIGNATURE', 'DATE', 'bi-calendar-check', 11, 10, '예) 2025-01-01', 64);