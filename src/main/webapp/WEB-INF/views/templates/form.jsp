<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        :root {
            --primary-color: #0066cc;
            --secondary-color: #6c757d;
            --border-color: #dee2e6;
            --hover-bg: #f8f9fa;
        }

        body {
            background: #f5f7fa;
            font-family: 'Malgun Gothic', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
        }

        /* 네비게이션 */
        .navbar {
            box-shadow: 0 2px 4px rgba(0,0,0,.08);
        }

        /* 메인 컨테이너 */
        .builder-container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 30px 15px;
        }

        /* 툴바 */
        .toolbar {
            background: white;
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,.08);
            position: sticky;
            top: 20px;
            z-index: 100;
        }

        .toolbar-section {
            border-right: 1px solid var(--border-color);
            padding-right: 20px;
            margin-right: 20px;
        }

        .toolbar-section:last-child {
            border-right: none;
            padding-right: 0;
            margin-right: 0;
        }

        .toolbar-btn {
            background: white;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 8px 16px;
            margin: 0 4px;
            cursor: pointer;
            transition: all 0.2s;
            font-size: 14px;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }

        .toolbar-btn:hover {
            background: var(--hover-bg);
            border-color: var(--primary-color);
            transform: translateY(-1px);
            box-shadow: 0 2px 4px rgba(0,0,0,.1);
        }

        .toolbar-btn.active {
            background: var(--primary-color);
            color: white;
            border-color: var(--primary-color);
        }

        .toolbar-btn i {
            font-size: 16px;
        }

        /* 문서 편집 영역 */
        .document-container {
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,.08);
            max-width: 900px;
            margin: 0 auto;
            min-height: 800px;
        }

        .document-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px 30px;
            border-radius: 12px 12px 0 0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .document-title-input {
            background: rgba(255,255,255,.2);
            border: 2px solid rgba(255,255,255,.3);
            color: white;
            padding: 8px 16px;
            border-radius: 8px;
            font-size: 18px;
            font-weight: 600;
            width: 400px;
        }

        .document-title-input::placeholder {
            color: rgba(255,255,255,.7);
        }

        .document-title-input:focus {
            background: rgba(255,255,255,.3);
            border-color: rgba(255,255,255,.5);
            outline: none;
        }

        .document-actions {
            display: flex;
            gap: 10px;
        }

        .document-body {
            padding: 40px;
            font-family: 'Malgun Gothic', sans-serif;
            line-height: 1.8;
            color: #333;
        }

        /* 편집 가능 섹션 */
        .editable-section {
            position: relative;
            padding: 6px;
            margin: 8px 0;
            border: 2px solid transparent;
            border-radius: 4px;
            transition: all 0.2s;
            min-height: 44px;
            display: flex;
            flex-direction: column;
        }

        .editable-section > :first-child {
            flex: 1 1 auto;
            width: 100%;
        }

        .editable-section:hover {
            background: rgba(0, 102, 204, 0.05);
            border-color: rgba(0, 102, 204, 0.2);
        }

        .editable-section.active {
            background: rgba(0, 102, 204, 0.08);
            border-color: var(--primary-color);
        }

        .section-controls {
            position: absolute;
            top: -30px;
            right: 0;
            display: none;
            gap: 5px;
            background: white;
            padding: 4px;
            border-radius: 6px;
            box-shadow: 0 2px 8px rgba(0,0,0,.15);
        }

        .editable-section:hover .section-controls,
        .editable-section.active .section-controls {
            display: flex;
        }

        .section-control-btn {
            width: 28px;
            height: 28px;
            border: 1px solid var(--border-color);
            background: white;
            border-radius: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.2s;
        }

        .section-control-btn:hover {
            background: var(--primary-color);
            color: white;
            border-color: var(--primary-color);
        }

        /* 섹션 타입별 스타일 */
        .section-title {
            font-size: 24px;
            font-weight: bold;
            text-align: center;
            margin: 30px 0;
            letter-spacing: 2px;
        }

        .section-text,
        .section-footer,
        .section-dotted-box,
        .section-title {
            font-size: 14px;
            line-height: 1.4;
            text-align: justify;
        }

        .section-clause {
            margin: 4px 0;
            padding: 4px 6px;
            display: flex;
            gap: 6px;
            align-items: center;
            border-radius: 4px;
            background: transparent;
        }

        .clause-number {
            font-weight: bold;
            display: inline-block;
            min-width: 18px;
            text-align: right;
            margin-top: 0;
        }

        .section-clause span[contenteditable="true"],
        .section-text[contenteditable="true"],
        .section-footer[contenteditable="true"],
        .section-dotted-box[contenteditable="true"],
        .section-title[contenteditable="true"] {
            min-height: 0;
            line-height: 1.4;
            padding: 2px 4px;
            flex: 1;
            display: block;
        }

        .section-dotted-box {
            border: 1px dashed #999;
            margin: 12px 0;
            border-radius: 4px;
            background: #fafafa;
        }

        .section-footer {
            margin-top: 12px;
            padding-top: 8px;
            border-top: 1px solid #ddd;
            text-align: center;
            font-size: 13px;
            color: #666;
        }

        .section-signature {
            margin: 30px 0;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 8px;
            background: #f9f9f9;
        }

        .signature-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 30px;
        }

        .signature-block {
            padding: 15px;
        }

        .signature-line {
            border-bottom: 1px solid #333;
            margin: 15px 0;
            padding-bottom: 5px;
            min-height: 30px;
        }

        /* 변수 스타일 */
        .template-variable {
            background: #ffe4b5;
            padding: 2px 8px;
            border-radius: 4px;
            border: 1px solid #ffa500;
            font-weight: 600;
            color: #d2691e;
            display: inline-block;
            margin: 0 2px;
            cursor: pointer;
            transition: all 0.2s;
        }

        .template-variable:hover {
            background: #ffd700;
            transform: scale(1.05);
        }

        /* 플레이스홀더 */
        .add-section-placeholder {
            border: 2px dashed #ccc;
            padding: 20px;
            text-align: center;
            margin: 20px 0;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.2s;
            color: #999;
        }

        .add-section-placeholder:hover {
            border-color: var(--primary-color);
            background: var(--hover-bg);
            color: var(--primary-color);
        }

        .add-section-placeholder i {
            font-size: 24px;
            display: block;
            margin-bottom: 8px;
        }

        /* 모달 */
        .variable-modal {
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: white;
            border-radius: 12px;
            padding: 30px;
            box-shadow: 0 10px 40px rgba(0,0,0,.2);
            z-index: 1000;
            max-width: 600px;
            width: 90%;
            display: none;
        }

        .variable-modal.show {
            display: block;
        }

        .modal-backdrop {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0,0,0,.5);
            z-index: 999;
            display: none;
        }

        .modal-backdrop.show {
            display: block;
        }

        .variable-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 10px;
            margin-top: 20px;
        }

        .variable-item {
            padding: 10px;
            border: 1px solid var(--border-color);
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.2s;
            text-align: center;
            font-size: 13px;
        }

        .variable-item:hover {
            background: var(--hover-bg);
            border-color: var(--primary-color);
            transform: translateY(-2px);
        }

        /* HTML 에디터 */
        .html-editor {
            width: 100%;
            min-height: 200px;
            font-family: 'Courier New', monospace;
            font-size: 13px;
            padding: 15px;
            border: 1px solid var(--border-color);
            border-radius: 6px;
            background: #f8f8f8;
        }

        /* 반응형 */
        @media (max-width: 768px) {
            .toolbar {
                position: static;
            }

            .signature-grid {
                grid-template-columns: 1fr;
            }

            .variable-grid {
                grid-template-columns: repeat(2, 1fr);
            }
        }

        /* 애니메이션 */
        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(-10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .editable-section {
            animation: slideIn 0.3s ease;
        }

        /* 사이드바 액션 버튼 */
        .floating-actions {
            position: fixed;
            right: 30px;
            bottom: 30px;
            display: flex;
            flex-direction: column;
            gap: 10px;
            z-index: 50;
        }

        .floating-btn {
            width: 56px;
            height: 56px;
            border-radius: 50%;
            background: var(--primary-color);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 12px rgba(0,0,0,.15);
            cursor: pointer;
            transition: all 0.3s;
        }

        .floating-btn:hover {
            transform: scale(1.1);
            box-shadow: 0 6px 20px rgba(0,0,0,.2);
        }
    </style>
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container-fluid">
        <a class="navbar-brand" href="/home">
            <i class="bi bi-file-earmark-text me-2"></i>Signly
        </a>
        <div class="navbar-nav ms-auto">
            <a class="nav-link" href="/home">대시보드</a>
            <a class="nav-link active" href="/templates">템플릿</a>
            <a class="nav-link" href="/contracts">계약서</a>
            <a class="nav-link" href="/profile/signature">서명 관리</a>
            <a class="nav-link" href="/logout">로그아웃</a>
        </div>
    </div>
</nav>

<div class="builder-container">
    <!-- 툴바 -->
    <div class="toolbar">
        <div class="d-flex align-items-center">
            <div class="toolbar-section">
                <strong class="me-3">섹션 추가:</strong>
                <button class="toolbar-btn" onclick="addSection('text')">
                    <i class="bi bi-text-left"></i> 일반 텍스트
                </button>
                <button class="toolbar-btn" onclick="addSection('clause')">
                    <i class="bi bi-list-ol"></i> 조항
                </button>
                <button class="toolbar-btn" onclick="addSection('dotted')">
                    <i class="bi bi-border-style"></i> 점선 박스
                </button>
                <button class="toolbar-btn" onclick="addSection('footer')">
                    <i class="bi bi-text-center"></i> 꼬릿말
                </button>
                <button class="toolbar-btn" onclick="addSection('signature')">
                    <i class="bi bi-pen"></i> 서명란
                </button>
                <button class="toolbar-btn" onclick="addSection('html')">
                    <i class="bi bi-code-slash"></i> HTML
                </button>
            </div>
            <div class="toolbar-section">
                <button class="toolbar-btn" onclick="showVariableModal()">
                    <i class="bi bi-braces"></i> 변수 추가
                </button>
            </div>
        </div>
    </div>

    <!-- 문서 편집 영역 -->
    <div class="document-container">
        <div class="document-header">
            <input type="text"
                   class="document-title-input"
                   id="templateTitle"
                   placeholder="템플릿 제목을 입력하세요"
                   value="${template.title}">
            <div class="document-actions">
                <button class="btn btn-light btn-sm" onclick="previewTemplate()">
                    <i class="bi bi-eye"></i> 미리보기
                </button>
                <button class="btn btn-success btn-sm" onclick="saveTemplate()">
                    <i class="bi bi-check-circle"></i> 저장
                </button>
            </div>
        </div>

        <div class="document-body" id="documentBody">
            <!-- 초기 플레이스홀더 -->
            <div class="add-section-placeholder" onclick="showAddSectionMenu(this)">
                <i class="bi bi-plus-circle"></i>
                <div>여기를 클릭하여 섹션을 추가하세요</div>
            </div>
        </div>
    </div>
</div>

<!-- 변수 선택 모달 -->
<div class="modal-backdrop" id="modalBackdrop"></div>
<div class="variable-modal" id="variableModal">
    <h5 class="mb-3">변수 선택</h5>
    <p class="text-muted small">클릭하여 현재 커서 위치에 변수를 삽입합니다</p>

    <div class="mb-3">
        <strong>근로자 정보</strong>
        <div class="variable-grid">
            <div class="variable-item" onclick="insertVariable('[EMPLOYEE]')">근로자명</div>
            <div class="variable-item" onclick="insertVariable('[EMPLOYEE_ADDRESS]')">근로자 주소</div>
            <div class="variable-item" onclick="insertVariable('[EMPLOYEE_PHONE]')">근로자 연락처</div>
            <div class="variable-item" onclick="insertVariable('[EMPLOYEE_ID]')">주민등록번호</div>
        </div>
    </div>

    <div class="mb-3">
        <strong>사업주 정보</strong>
        <div class="variable-grid">
            <div class="variable-item" onclick="insertVariable('[EMPLOYER]')">사업주명</div>
            <div class="variable-item" onclick="insertVariable('[COMPANY_NAME]')">사업체명</div>
            <div class="variable-item" onclick="insertVariable('[EMPLOYER_ADDRESS]')">사업장 주소</div>
            <div class="variable-item" onclick="insertVariable('[EMPLOYER_PHONE]')">사업장 연락처</div>
            <div class="variable-item" onclick="insertVariable('[BUSINESS_NUMBER]')">사업자번호</div>
        </div>
    </div>

    <div class="mb-3">
        <strong>계약 정보</strong>
        <div class="variable-grid">
            <div class="variable-item" onclick="insertVariable('[CONTRACT_START_DATE]')">계약 시작일</div>
            <div class="variable-item" onclick="insertVariable('[CONTRACT_END_DATE]')">계약 종료일</div>
            <div class="variable-item" onclick="insertVariable('[CONTRACT_DATE]')">계약 체결일</div>
            <div class="variable-item" onclick="insertVariable('[WORKPLACE]')">근무 장소</div>
            <div class="variable-item" onclick="insertVariable('[JOB_DESCRIPTION]')">업무 내용</div>
        </div>
    </div>

    <div class="mb-3">
        <strong>근무 조건</strong>
        <div class="variable-grid">
            <div class="variable-item" onclick="insertVariable('[WORK_START_TIME]')">근무 시작시간</div>
            <div class="variable-item" onclick="insertVariable('[WORK_END_TIME]')">근무 종료시간</div>
            <div class="variable-item" onclick="insertVariable('[BREAK_START_TIME]')">휴게 시작시간</div>
            <div class="variable-item" onclick="insertVariable('[BREAK_END_TIME]')">휴게 종료시간</div>
            <div class="variable-item" onclick="insertVariable('[WORK_DAYS]')">근무일수</div>
            <div class="variable-item" onclick="insertVariable('[HOLIDAYS]')">휴일</div>
        </div>
    </div>

    <div class="mb-3">
        <strong>임금 정보</strong>
        <div class="variable-grid">
            <div class="variable-item" onclick="insertVariable('[MONTHLY_SALARY]')">월급</div>
            <div class="variable-item" onclick="insertVariable('[HOURLY_WAGE]')">시급</div>
            <div class="variable-item" onclick="insertVariable('[BONUS]')">상여금</div>
            <div class="variable-item" onclick="insertVariable('[OTHER_ALLOWANCES]')">기타 수당</div>
            <div class="variable-item" onclick="insertVariable('[PAYMENT_DAY]')">임금 지급일</div>
            <div class="variable-item" onclick="insertVariable('[PAYMENT_METHOD]')">지급 방법</div>
        </div>
    </div>

    <div class="mb-3">
        <strong>서명</strong>
        <div class="variable-grid">
            <div class="variable-item" onclick="insertVariable('[EMPLOYER_SIGNATURE]')">사업주 서명</div>
            <div class="variable-item" onclick="insertVariable('[EMPLOYEE_SIGNATURE]')">근로자 서명</div>
            <div class="variable-item" onclick="insertVariable('[SIGNATURE_DATE]')">서명일</div>
        </div>
    </div>

    <div class="text-end mt-4">
        <button class="btn btn-secondary" onclick="closeVariableModal()">닫기</button>
    </div>
</div>

<!-- 플로팅 액션 버튼 -->
<div class="floating-actions">
    <div class="floating-btn" onclick="saveTemplate()" title="저장">
        <i class="bi bi-save"></i>
    </div>
    <div class="floating-btn" onclick="previewTemplate()" title="미리보기">
        <i class="bi bi-eye"></i>
    </div>
</div>

<!-- Hidden form for submission -->
<form id="templateForm" method="post" action="${formAction}" style="display: none;">
    <c:if test="${not empty _csrf}">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
    </c:if>
    <input type="hidden" name="title" id="formTitle">
    <input type="hidden" name="sectionsJson" id="sectionsJson">
</form>

<!-- 미리보기 모달 -->
<div class="modal fade" id="previewModal" tabindex="-1" aria-labelledby="previewModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="previewModalLabel">템플릿 미리보기</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="닫기"></button>
            </div>
            <div class="modal-body p-0">
                <iframe id="previewFrame" title="템플릿 미리보기" style="width: 100%; height: 70vh; border: 0;"></iframe>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<script>
    let clauseCounter = 0;
    let activeElement = null;
    let sections = [];

    const FRONTEND_TYPES = new Set(['text', 'clause', 'dotted', 'footer', 'signature', 'html', 'title']);

    const htmlEntityDecoder = document.createElement('textarea');

    function decodeHtmlEntities(value) {
        if (!value || typeof value !== 'string') {
            return value;
        }
        htmlEntityDecoder.innerHTML = value;
        return htmlEntityDecoder.value;
    }

    function parseSectionsPayload(raw) {
        if (!raw) {
            return [];
        }

        let parsed = raw;

        if (typeof raw === 'string') {
            const trimmed = raw.trim();
            if (!trimmed) {
                return [];
            }
            const decoded = decodeHtmlEntities(trimmed);
            try {
                parsed = JSON.parse(decoded);
            } catch (error) {
                console.warn('Failed to parse sections JSON (first pass)', error);
                return [];
            }
        }

        if (Array.isArray(parsed)) {
            return parsed;
        }

        if (parsed && typeof parsed === 'object') {
            if (Array.isArray(parsed.sections)) {
                return parsed.sections;
            }
            if (Array.isArray(parsed.data)) {
                return parsed.data;
            }
            if (Array.isArray(parsed.content)) {
                return parsed.content;
            }
            if (typeof parsed.sectionsJson === 'string') {
                return parseSectionsPayload(parsed.sectionsJson);
            }
            return [parsed];
        }

        return [];
    }

    function normalizeFrontendType(type, metadata = {}) {
        if (metadata && typeof metadata === 'object' && metadata.kind && FRONTEND_TYPES.has(String(metadata.kind).toLowerCase())) {
            return String(metadata.kind).toLowerCase();
        }
        if (!type) {
            return 'text';
        }
        const raw = String(type);
        const lower = raw.toLowerCase();
        if (FRONTEND_TYPES.has(lower)) {
            return lower;
        }
        switch (raw.toUpperCase()) {
            case 'HEADER':
                return 'title';
            case 'DOTTED_BOX':
                return 'dotted';
            case 'FOOTER':
                return 'footer';
            case 'CUSTOM':
                if (metadata && metadata.signature) {
                    return 'signature';
                }
                if (metadata && metadata.rawHtml) {
                    return 'html';
                }
                return 'text';
            case 'PARAGRAPH':
            default:
                return 'text';
        }
    }

    function ensureMetadataForType(type, metadata = {}) {
        const base = metadata && typeof metadata === 'object' ? { ...metadata } : {};
        if (type === 'html' || type === 'signature') {
            base.rawHtml = true;
        }
        if (type === 'signature' && base.signature === undefined) {
            base.signature = true;
        }
        if (type === 'title' && base.header === undefined) {
            base.header = true;
        }
        if (!base.kind) {
            base.kind = type;
        }
        return base;
    }

    function encodeMetadata(metadata = {}) {
        try {
            return encodeURIComponent(JSON.stringify(metadata));
        } catch (error) {
            console.warn('Failed to encode metadata', error);
            return encodeURIComponent('{}');
        }
    }

    function decodeMetadata(encoded) {
        if (!encoded) {
            return {};
        }
        try {
            return JSON.parse(decodeURIComponent(encoded));
        } catch (error) {
            console.warn('Failed to decode metadata', error);
            return {};
        }
    }

    function setSectionMetadata(section, metadata = {}) {
        section.dataset.metadata = encodeMetadata(metadata);
    }

    function getSectionMetadata(section) {
        if (!section || !section.dataset) {
            return {};
        }
        return decodeMetadata(section.dataset.metadata);
    }

    function coerceMetadata(metadata) {
        if (!metadata) {
            return {};
        }
        if (typeof metadata === 'string') {
            const decoded = decodeHtmlEntities(metadata);
            try {
                return JSON.parse(decoded);
            } catch (error) {
                console.warn('Failed to parse metadata string', error);
                return {};
            }
        }
        if (typeof metadata === 'object') {
            return { ...metadata };
        }
        return {};
    }

    function mapFrontendTypeToServer(type, metadata = {}) {
        switch (type) {
            case 'title':
                return 'HEADER';
            case 'clause':
            case 'text':
                return 'PARAGRAPH';
            case 'dotted':
                return 'DOTTED_BOX';
            case 'footer':
                return 'FOOTER';
            case 'html':
                return metadata && metadata.rawHtml ? 'CUSTOM' : 'PARAGRAPH';
            case 'signature':
                return 'CUSTOM';
            default:
                return 'PARAGRAPH';
        }
    }

    // 초기화
    document.addEventListener('DOMContentLoaded', function() {
        loadInitialSections();
        setupEventListeners();

        window.SignlyTemplateEditor = {
            getSectionsSnapshot() {
                updateSectionsData();
                return JSON.parse(JSON.stringify(sections));
            },
            getPreviewHtml() {
                updateSectionsData();
                return generatePreviewHtml();
            }
        };
    });

    // 이벤트 리스너 설정
    function setupEventListeners() {
        // 문서 클릭시 활성 요소 추적
        document.getElementById('documentBody').addEventListener('click', function(e) {
            const section = e.target.closest('.editable-section');
            if (section) {
                setActiveSection(section);
            }
        });

        // 콘텐츠 변경 감지
        document.getElementById('documentBody').addEventListener('input', function(e) {
            if (e.target.contentEditable === 'true') {
                updateSectionContent(e.target);
            }
        });

        // 모달 백드롭 클릭시 닫기
        document.getElementById('modalBackdrop').addEventListener('click', closeVariableModal);
    }

    // 섹션 추가
    function addSection(type, content = '', afterElement = null, metadata = {}) {
        const section = createSectionElement(type, content, metadata);
        const documentBody = document.getElementById('documentBody');

        if (afterElement) {
            afterElement.insertAdjacentElement('afterend', section);
        } else {
            // 플레이스홀더 제거
            const placeholder = documentBody.querySelector('.add-section-placeholder');
            if (placeholder && documentBody.children.length === 1) {
                placeholder.remove();
            }
            documentBody.appendChild(section);
        }

        // 새 플레이스홀더 추가
        if (!documentBody.querySelector('.add-section-placeholder')) {
            const newPlaceholder = createPlaceholder();
            documentBody.appendChild(newPlaceholder);
        }

        setActiveSection(section);
        updateSectionsData();
    }

    // 섹션 요소 생성
    function createSectionElement(type, content = '', metadata = {}) {
        const coercedMetadata = coerceMetadata(metadata);
        const normalizedType = normalizeFrontendType(type, coercedMetadata);
        const normalizedMetadata = ensureMetadataForType(normalizedType, coercedMetadata);
        const section = document.createElement('div');
        section.className = 'editable-section';
        section.dataset.type = normalizedType;
        section.dataset.id = 'section-' + Date.now();
        setSectionMetadata(section, normalizedMetadata);

        let innerHTML = '';

        switch(normalizedType) {
            case 'text':
                innerHTML = `<div contenteditable="true" class="section-text">${content || '텍스트를 입력하세요...'}</div>`;
                break;

            case 'title':
                innerHTML = `<div contenteditable="true" class="section-title">${content || '제목을 입력하세요...'}</div>`;
                break;

            case 'clause':
                clauseCounter++;
                innerHTML = `
                <div class="section-clause">
                    <span class="clause-number">${clauseCounter}.</span>
                    <span contenteditable="true">${content || '조항 내용을 입력하세요...'}</span>
                </div>`;
                break;

            case 'dotted':
                innerHTML = `<div contenteditable="true" class="section-dotted-box">${content || '점선 박스 내용을 입력하세요...'}</div>`;
                break;

            case 'footer':
                innerHTML = `<div contenteditable="true" class="section-footer">${content || '꼬릿말을 입력하세요...'}</div>`;
                break;

            case 'signature':
                innerHTML = `
                <div class="section-signature">
                    <div class="signature-grid">
                        <div class="signature-block">
                            <h6>사업주</h6>
                            <div class="signature-line">사업체명: [COMPANY_NAME]</div>
                            <div class="signature-line">주소: [EMPLOYER_ADDRESS]</div>
                            <div class="signature-line">대표자: [EMPLOYER] (인)</div>
                            <div class="signature-line">연락처: [EMPLOYER_PHONE]</div>
                        </div>
                        <div class="signature-block">
                            <h6>근로자</h6>
                            <div class="signature-line">성명: [EMPLOYEE]</div>
                            <div class="signature-line">주소: [EMPLOYEE_ADDRESS]</div>
                            <div class="signature-line">연락처: [EMPLOYEE_PHONE]</div>
                            <div class="signature-line">서명: (인)</div>
                        </div>
                    </div>
                    <div class="text-center mt-3">
                        <div contenteditable="true">[CONTRACT_DATE]</div>
                    </div>
                </div>`;
                break;

            case 'html':
                innerHTML = `
                <div class="html-section">
                    <textarea class="html-editor" placeholder="HTML 코드를 입력하세요...">${content || ''}</textarea>
                    <div class="html-preview mt-2"></div>
                </div>`;
                break;
            default:
                innerHTML = `<div contenteditable="true" class="section-text">${content || '텍스트를 입력하세요...'}</div>`;
                break;
        }

        section.innerHTML = innerHTML + createSectionControls();

        return section;
    }

    // 섹션 컨트롤 버튼 생성
    function createSectionControls() {
        return `
        <div class="section-controls">
            <button class="section-control-btn" onclick="moveSection(this, 'up')" title="위로">
                <i class="bi bi-arrow-up"></i>
            </button>
            <button class="section-control-btn" onclick="moveSection(this, 'down')" title="아래로">
                <i class="bi bi-arrow-down"></i>
            </button>
            <button class="section-control-btn" onclick="duplicateSection(this)" title="복사">
                <i class="bi bi-files"></i>
            </button>
            <button class="section-control-btn" onclick="deleteSection(this)" title="삭제">
                <i class="bi bi-trash"></i>
            </button>
        </div>`;
    }

    // 플레이스홀더 생성
    function createPlaceholder() {
        const placeholder = document.createElement('div');
        placeholder.className = 'add-section-placeholder';
        placeholder.onclick = function() { showAddSectionMenu(this); };
        placeholder.innerHTML = `
        <i class="bi bi-plus-circle"></i>
        <div>여기를 클릭하여 섹션을 추가하세요</div>`;
        return placeholder;
    }

    // 활성 섹션 설정
    function setActiveSection(section) {
        document.querySelectorAll('.editable-section').forEach(s => s.classList.remove('active'));
        section.classList.add('active');
        activeElement = section.querySelector('[contenteditable="true"], .html-editor');
    }

    // 섹션 이동
    function moveSection(button, direction) {
        const section = button.closest('.editable-section');
        if (direction === 'up' && section.previousElementSibling && !section.previousElementSibling.classList.contains('add-section-placeholder')) {
            section.parentNode.insertBefore(section, section.previousElementSibling);
        } else if (direction === 'down' && section.nextElementSibling && !section.nextElementSibling.classList.contains('add-section-placeholder')) {
            section.parentNode.insertBefore(section.nextElementSibling, section);
        }
        updateSectionsData();
    }

    // 섹션 복사
    function duplicateSection(button) {
        const section = button.closest('.editable-section');
        const clone = section.cloneNode(true);
        clone.dataset.id = 'section-' + Date.now();
        section.parentNode.insertBefore(clone, section.nextSibling);
        updateSectionsData();
    }

    // 섹션 삭제
    function deleteSection(button) {
        if (confirm('이 섹션을 삭제하시겠습니까?')) {
            const section = button.closest('.editable-section');
            section.remove();
            updateSectionsData();

            // 모든 섹션이 삭제되면 플레이스홀더 추가
            const documentBody = document.getElementById('documentBody');
            if (documentBody.children.length === 0) {
                documentBody.appendChild(createPlaceholder());
            }
        }
    }

    // 변수 모달 표시
    function showVariableModal() {
        document.getElementById('variableModal').classList.add('show');
        document.getElementById('modalBackdrop').classList.add('show');
    }

    // 변수 모달 닫기
    function closeVariableModal() {
        document.getElementById('variableModal').classList.remove('show');
        document.getElementById('modalBackdrop').classList.remove('show');
    }

    // 변수 삽입
    function insertVariable(variable) {
        if (!activeElement) {
            alert('먼저 텍스트를 입력할 위치를 클릭해주세요.');
            return;
        }

        if (activeElement.contentEditable === 'true') {
            // contenteditable 요소
            const selection = window.getSelection();
            const range = selection.getRangeAt(0);

            const varSpan = document.createElement('span');
            varSpan.className = 'template-variable';
            varSpan.textContent = variable;
            varSpan.contentEditable = 'false';

            range.insertNode(varSpan);
            range.setStartAfter(varSpan);
            range.collapse(true);
            selection.removeAllRanges();
            selection.addRange(range);
        } else if (activeElement.tagName === 'TEXTAREA') {
            // textarea 요소 (HTML 편집기)
            const start = activeElement.selectionStart;
            const end = activeElement.selectionEnd;
            const text = activeElement.value;
            activeElement.value = text.substring(0, start) + variable + text.substring(end);
            activeElement.selectionStart = activeElement.selectionEnd = start + variable.length;
        }

        closeVariableModal();
        updateSectionsData();
    }

    // 섹션 추가 메뉴 표시
    function showAddSectionMenu(placeholder) {
        const menu = `
        <div style="display: flex; gap: 10px; justify-content: center; padding: 10px;">
            <button class="toolbar-btn" onclick="addSectionFromPlaceholder('text', this)">
                <i class="bi bi-text-left"></i> 텍스트
            </button>
            <button class="toolbar-btn" onclick="addSectionFromPlaceholder('clause', this)">
                <i class="bi bi-list-ol"></i> 조항
            </button>
            <button class="toolbar-btn" onclick="addSectionFromPlaceholder('dotted', this)">
                <i class="bi bi-border-style"></i> 점선
            </button>
            <button class="toolbar-btn" onclick="addSectionFromPlaceholder('footer', this)">
                <i class="bi bi-text-center"></i> 꼬릿말
            </button>
            <button class="toolbar-btn" onclick="addSectionFromPlaceholder('signature', this)">
                <i class="bi bi-pen"></i> 서명란
            </button>
            <button class="toolbar-btn" onclick="addSectionFromPlaceholder('html', this)">
                <i class="bi bi-code-slash"></i> HTML
            </button>
        </div>
    `;
        placeholder.innerHTML = menu;
    }

    // 플레이스홀더에서 섹션 추가
    function addSectionFromPlaceholder(type, button) {
        const placeholder = button.closest('.add-section-placeholder');
        const section = createSectionElement(type);
        placeholder.replaceWith(section);

        // 새 플레이스홀더 추가
        const documentBody = document.getElementById('documentBody');
        documentBody.appendChild(createPlaceholder());

        setActiveSection(section);
        updateSectionsData();
    }

    // 섹션 내용 업데이트
    function updateSectionContent(element) {
        const section = element.closest('.editable-section');
        if (section) {
            updateSectionsData();
        }
    }

    // 섹션 데이터 업데이트
    function updateSectionsData() {
        const documentBody = document.getElementById('documentBody');
        sections = [];

        let clauseIndex = 0;

        documentBody.querySelectorAll('.editable-section').forEach((section, index) => {
            const existingMetadata = getSectionMetadata(section);
            const type = normalizeFrontendType(section.dataset.type, existingMetadata);
            const metadata = ensureMetadataForType(type, existingMetadata);
            setSectionMetadata(section, metadata);
            section.dataset.type = type;

            let content = '';

            if (type === 'html') {
                const textarea = section.querySelector('.html-editor');
                content = textarea ? textarea.value : '';

                const preview = section.querySelector('.html-preview');
                if (preview) {
                    preview.innerHTML = content;
                }
            } else if (type === 'signature') {
                const signatureElement = section.querySelector('.section-signature');
                content = signatureElement ? signatureElement.innerHTML : '';
            } else if (type === 'clause') {
                const clauseContent = section.querySelector('.section-clause span[contenteditable="true"]');
                content = clauseContent ? clauseContent.innerHTML : '';
                clauseIndex++;
                const numberElement = section.querySelector('.clause-number');
                if (numberElement) {
                    numberElement.textContent = clauseIndex + '.';
                }
            } else {
                const editableElement = section.querySelector('[contenteditable="true"]');
                if (editableElement) {
                    content = editableElement.innerHTML;
                }
            }

            sections.push({
                sectionId: section.dataset.id,
                type: type,
                order: index,
                content: content,
                metadata: metadata
            });
        });

        clauseCounter = clauseIndex;
    }

    // 초기 섹션 로드
    function loadInitialSections() {
        const scriptEl = document.getElementById('initialSections');
        if (scriptEl) {
            try {
                const rawPayload = scriptEl.textContent || '[]';
                console.debug('[TemplateEditor] raw initial sections payload:', rawPayload.substring(0, 500));

                const sectionsData = parseSectionsPayload(rawPayload);
                console.debug('[TemplateEditor] parsed sections array:', sectionsData);
                if (sectionsData.length > 0) {
                    const documentBody = document.getElementById('documentBody');
                    documentBody.innerHTML = '';

                    clauseCounter = 0;
                    [...sectionsData]
                        .sort((a, b) => {
                            const orderA = typeof a.order === 'number' ? a.order : parseInt(a.order, 10) || 0;
                            const orderB = typeof b.order === 'number' ? b.order : parseInt(b.order, 10) || 0;
                            return orderA - orderB;
                        })
                        .forEach(sectionData => {
                            if (!sectionData) {
                                return;
                            }
                            const metadata = coerceMetadata(sectionData.metadata);
                            const section = createSectionElement(sectionData.type, sectionData.content, metadata);
                            section.dataset.id = sectionData.sectionId || ('section-' + Date.now());
                            documentBody.appendChild(section);
                        });

                    // 플레이스홀더 추가
                    documentBody.appendChild(createPlaceholder());
                    updateSectionsData();
                } else {
                    const documentBody = document.getElementById('documentBody');
                    if (!documentBody.querySelector('.add-section-placeholder')) {
                        documentBody.appendChild(createPlaceholder());
                    }
                }
            } catch (e) {
                console.error('Failed to load initial sections:', e);
            }
        }
    }

    // 템플릿 미리보기
    function previewTemplate() {
        updateSectionsData();

        if (!sections.length) {
            alert('미리볼 섹션이 없습니다. 먼저 섹션을 추가해주세요.');
            return;
        }

        const previewHtml = generatePreviewHtml();
        console.debug('[TemplateEditor] sections before preview:', sections);
        console.debug('[TemplateEditor] preview HTML sample:', previewHtml.substring(0, 500));
        const previewTitle = document.getElementById('templateTitle').value || '제목 없음';
        const previewFrame = document.getElementById('previewFrame');

        if (previewFrame && previewFrame.contentWindow) {
            const iframeDocument = previewFrame.contentDocument || previewFrame.contentWindow.document;
            iframeDocument.open();
            iframeDocument.write(previewHtml);
            iframeDocument.close();

            const modalElement = document.getElementById('previewModal');
            const modalTitle = document.getElementById('previewModalLabel');
            if (modalTitle) {
                modalTitle.textContent = previewTitle + ' 미리보기';
            }

            if (modalElement && window.bootstrap && window.bootstrap.Modal) {
                const modalInstance = bootstrap.Modal.getOrCreateInstance(modalElement);
                modalInstance.show();
                return;
            }
        }

        const fallbackWindow = window.open('', 'templatePreview', 'width=900,height=700');
        if (fallbackWindow) {
            fallbackWindow.document.write(previewHtml);
            fallbackWindow.document.close();
        } else {
            alert('미리보기를 열 수 없습니다. 팝업 차단 설정을 확인해주세요.');
        }
    }

    // 미리보기 HTML 생성
    function generatePreviewHtml() {
        const title = document.getElementById('templateTitle').value || '제목 없음';

        let bodyContent = '';
        let clauseIndex = 0;

        sections.forEach(section => {
            switch (section.type) {
                case 'html':
                    bodyContent += section.content;
                    break;
                case 'signature':
                    bodyContent += `<div class="section-signature">${section.content}</div>`;
                    break;
                case 'clause':
                    clauseIndex++;
                    bodyContent += `<div class="section-clause"><span class="clause-number">${clauseIndex}.</span> <span>${section.content}</span></div>`;
                    break;
                case 'title':
                    bodyContent += `<h2 class="section-title">${section.content}</h2>`;
                    break;
                case 'dotted':
                    bodyContent += `<div class="section-dotted">${section.content}</div>`;
                    break;
                case 'footer':
                    bodyContent += `<div class="section-footer">${section.content}</div>`;
                    break;
                default:
                    bodyContent += `<div class="section-text">${section.content}</div>`;
                    break;
            }
        });

        console.debug('[TemplateEditor] body content snippet:', bodyContent.substring(0, 500));

        return `
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>${title}</title>
            <style>
                body {
                    font-family: 'Malgun Gothic', sans-serif;
                    max-width: 210mm;
                    margin: 0 auto;
                    padding: 20mm;
                    line-height: 1.6;
                    font-size: 14px;
                    background: white;
                }
                .section-text {
                    margin: 15px 0;
                    text-align: justify;
                }
                .section-clause {
                    margin: 20px 0;
                    padding-left: 20px;
                }
                .clause-number {
                    font-weight: bold;
                    display: inline-block;
                    min-width: 20px;
                }
                .section-dotted {
                    border: 0.5px dashed #666;
                    padding: 15px;
                    margin: 20px 0;
                    border-radius: 4px;
                }
                .section-title {
                    font-size: 24px;
                    font-weight: bold;
                    text-align: center;
                    margin: 30px 0;
                    letter-spacing: 2px;
                }
                .section-footer {
                    margin-top: 40px;
                    padding-top: 20px;
                    border-top: 1px solid #ddd;
                    text-align: center;
                    font-size: 13px;
                    color: #666;
                }
                .section-signature {
                    margin: 30px 0;
                    padding: 20px;
                }
                .signature-grid {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 30px;
                }
                .signature-line {
                    border-bottom: 1px solid #333;
                    margin: 15px 0;
                    padding-bottom: 5px;
                    min-height: 30px;
                }
                .template-variable {
                    background: #ffe4b5;
                    padding: 2px 6px;
                    border-radius: 3px;
                    font-weight: 600;
                    color: #d2691e;
                }
                @media print {
                    body { padding: 10mm; }
                }
            </style>
        </head>
        <body>
            <h1 style="text-align: center; margin-bottom: 40px;">${title}</h1>
            ${bodyContent}
        </body>
        </html>
    `;
    }

    // 템플릿 저장
    function saveTemplate() {
        const title = document.getElementById('templateTitle').value;

        if (!title) {
            alert('템플릿 제목을 입력해주세요.');
            document.getElementById('templateTitle').focus();
            return;
        }

        if (sections.length === 0) {
            alert('최소 하나 이상의 섹션을 추가해주세요.');
            return;
        }

        updateSectionsData();

        // 폼 데이터 설정
        document.getElementById('formTitle').value = title;

        const serializedSections = sections.map((section, index) => ({
            sectionId: section.sectionId,
            type: mapFrontendTypeToServer(section.type, section.metadata),
            order: index,
            content: section.content,
            metadata: section.metadata || {}
        }));

        document.getElementById('sectionsJson').value = JSON.stringify(serializedSections);

        // 폼 제출
        document.getElementById('templateForm').submit();
    }

    // HTML 편집기 실시간 미리보기
    document.addEventListener('input', function(e) {
        if (e.target.classList.contains('html-editor')) {
            const preview = e.target.parentElement.querySelector('.html-preview');
            if (preview) {
                preview.innerHTML = e.target.value;
            }
        }
    });

    // 키보드 단축키
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + S: 저장
        if ((e.ctrlKey || e.metaKey) && e.key === 's') {
            e.preventDefault();
            saveTemplate();
        }

        // Ctrl/Cmd + P: 미리보기
        if ((e.ctrlKey || e.metaKey) && e.key === 'p') {
            e.preventDefault();
            previewTemplate();
        }

        // Ctrl/Cmd + B: 변수 삽입
        if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
            e.preventDefault();
            showVariableModal();
        }
    });

    // 초기 섹션 데이터 (서버에서 전달된 경우)
</script>

<c:set var="sectionsJsonRaw" value="${empty template.sectionsJson ? '[]' : template.sectionsJson}" />
<c:set var="sectionsJsonSafe" value="${fn:replace(sectionsJsonRaw, '</script>', '<&#92;/script>')}" />
<script id="initialSections" type="application/json"><c:out value="${sectionsJsonSafe}" escapeXml="false" /></script>

</body>
</html>
