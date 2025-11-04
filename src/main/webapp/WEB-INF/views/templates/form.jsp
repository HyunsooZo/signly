<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<jsp:include page="../common/header.jsp">
    <jsp:param name="additionalCss" value="/css/template-builder.css" />
    <jsp:param name="additionalCss2" value="/css/modal.css" />
</jsp:include>
<body>
    <jsp:include page="../common/navbar.jsp">
        <jsp:param name="currentPage" value="templates" />
    </jsp:include>

<div class="builder-container">
    <!-- 프리셋 템플릿 선택 영역 -->
    <div class="preset-section" id="presetSection">
        <div class="preset-header">
            <h5 class="preset-title">
                <i class="bi bi-lightning-charge"></i>
                프리셋 템플릿으로 시작하기
            </h5>
            <button class="btn btn-sm btn-outline-secondary" onclick="togglePresetSection()">
                <i class="bi bi-chevron-up" id="presetToggleIcon"></i>
            </button>
        </div>
        <div class="preset-content" id="presetContent">
            <div class="preset-loading" id="presetLoading">
                <div class="spinner-border spinner-border-sm" role="status">
                    <span class="visually-hidden">로딩 중...</span>
                </div>
                프리셋 템플릿을 불러오는 중...
            </div>
            <div class="preset-grid" id="presetGrid" style="display: none;">
                <!-- 프리셋 카드들이 여기에 동적으로 추가됩니다 -->
            </div>
        </div>
    </div>

    <!-- 툴바 -->
    <div class="toolbar">
        <div class="d-flex align-items-center flex-wrap gap-2">
            <strong class="me-2 toolbar-label">변수 추가하기:</strong>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[EMPLOYER]')" title="사업주명">
                <i class="bi bi-person-badge"></i> 사업주명
            </button>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[COMPANY_NAME]')" title="사업체명">
                <i class="bi bi-building"></i> 회사명
            </button>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[EMPLOYEE]')" title="근로자명">
                <i class="bi bi-person"></i> 근로자명
            </button>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[CONTRACT_DATE]')" title="계약 체결일">
                <i class="bi bi-calendar-event"></i> 계약일
            </button>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[WORKPLACE]')" title="근무 장소">
                <i class="bi bi-geo-alt"></i> 근무지
            </button>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[JOB_DESCRIPTION]')" title="업무 내용">
                <i class="bi bi-briefcase"></i> 업무
            </button>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[MONTHLY_SALARY]')" title="월급">
                <i class="bi bi-cash"></i> 월급
            </button>
            <button class="toolbar-btn toolbar-btn-sm ms-auto" onclick="showVariableModal()">
                <i class="bi bi-braces"></i> 더보기
            </button>
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
<c:set var="formAction" value="${not empty templateId && templateId ne 'new' ? '/templates/'.concat(templateId) : '/templates'}" />
<form id="templateForm" method="post" action="${formAction}" class="template-form-hidden">
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
                <iframe id="previewFrame" title="템플릿 미리보기" class="preview-iframe"></iframe>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>

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
        const temp = document.createElement('div');
        temp.innerHTML = value;
        return temp.textContent || temp.innerText || value;
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
            try {
                parsed = JSON.parse(trimmed);
            } catch (error) {
                console.warn('Failed to parse sections JSON', error);
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

    function normalizeFrontendType(type, metadata) {
        metadata = metadata || {};
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

    function ensureMetadataForType(type, metadata) {
        metadata = metadata || {};
        const base = metadata && typeof metadata === 'object' ? Object.assign({}, metadata) : {};
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

    function encodeMetadata(metadata) {
        metadata = metadata || {};
        try {
            return encodeURIComponent(JSON.stringify(metadata));
        } catch (error) {
            console.warn('Failed to encode metadata', error);
            return encodeURIComponent('{}');
        }
    }

    function ensureString(value) {
        if (value === null || value === undefined) {
            return '';
        }
        return typeof value === 'string' ? value : String(value);
    }

    // 변수명 한글 매핑
    const VARIABLE_DISPLAY_NAMES = {
        'EMPLOYER': '사업주',
        'EMPLOYEE': '근로자',
        'WORKPLACE': '근무장소',
        'CONTRACT_START_DATE': '시작일',
        'CONTRACT_END_DATE': '종료일',
        'JOB_DESCRIPTION': '업무내용',
        'WORK_START_TIME': '근무시작',
        'WORK_END_TIME': '근무종료',
        'BREAK_START_TIME': '휴게시작',
        'BREAK_END_TIME': '휴게종료',
        'WORK_DAYS': '근무일수',
        'HOLIDAYS': '휴일',
        'MONTHLY_SALARY': '월급',
        'BONUS': '상여금',
        'OTHER_ALLOWANCES': '기타수당',
        'PAYMENT_DAY': '지급일',
        'PAYMENT_METHOD': '지급방법',
        'CONTRACT_DATE': '계약일',
        'EMPLOYEE_ADDRESS': '근로자주소',
        'EMPLOYEE_PHONE': '근로자연락처',
        'COMPANY_NAME': '회사명',
        'EMPLOYER_ADDRESS': '사업주주소',
        'EMPLOYER_PHONE': '사업주전화',
        'EMPLOYEE_SIGNATURE_IMAGE': '근로자서명',
        'EMPLOYER_SIGNATURE_IMAGE': '사업주서명'
    };

    function getDisplayName(varName) {
        return VARIABLE_DISPLAY_NAMES[varName] || varName;
    }

    function convertVariablesToBrackets(html) {
        if (!html) return '';
        return html.replace(/<span class="template-variable"[^>]*data-var-name="([^"]+)"[^>]*>[\s\S]*?<\/span>/g, '[$1]');
    }

    function convertBracketsToVariables(html) {
        if (!html) return '';
        return html.replace(/\[([A-Z_]+)\]/g, function(match, varName) {
            const displayName = getDisplayName(varName);
            return '<span class="template-variable" contenteditable="false" data-var-name="' + varName + '">' +
                   '<span>' + displayName + '</span>' +
                   '<span class="template-variable-remove"></span>' +
                   '</span>';
        });
    }

    function normalizePreviewContent(type, rawContent) {
        console.debug('[normalizePreviewContent] input:', type, JSON.stringify(rawContent));
        if (rawContent === null || rawContent === undefined) {
            console.debug('[normalizePreviewContent] null/undefined -> empty string');
            return '';
        }

        let value = ensureString(rawContent);
        console.debug('[normalizePreviewContent] after ensureString:', JSON.stringify(value));

        if (type === 'signature' || type === 'html') {
            console.debug('[normalizePreviewContent] signature/html - keeping HTML as is');
            value = value.replace(/<span class="template-variable"[^>]*>[\s\S]*?<\/span>/g, function(match) {
                return '<span class="blank-line"></span>';
            });
            value = value.replace(/\[[\w_]+\]/g, '<span class="blank-line"></span>');
            return value;
        }

        const BLANK_MARKER = '___BLANK_LINE___';
        value = value.replace(/<span class="template-variable"[^>]*>[\s\S]*?<\/span>\s*<span class="template-variable-remove"[^>]*>[\s\S]*?<\/span>/g, function(match) {
            return BLANK_MARKER;
        });
        value = value.replace(/<span class="template-variable"[^>]*>[\s\S]*?<\/span>/g, function(match) {
            return BLANK_MARKER;
        });
        value = value.replace(/<span class="template-variable-remove"[^>]*>[\s\S]*?<\/span>/g, '');

        value = decodeHtmlEntities(value);
        console.debug('[normalizePreviewContent] after decodeHtmlEntities:', JSON.stringify(value));

        value = value.replace(new RegExp(BLANK_MARKER, 'g'), '<span class="blank-line"></span>');

        return value;
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

    function setSectionMetadata(section, metadata) {
        metadata = metadata || {};
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
            return Object.assign({}, metadata);
        }
        return {};
    }

    function mapFrontendTypeToServer(type, metadata) {
        metadata = metadata || {};
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

    document.addEventListener('DOMContentLoaded', function() {
        loadInitialSections();
        setupEventListeners();
        loadPresetTemplates();

        window.SignlyTemplateEditor = {
            getSectionsSnapshot: function() {
                updateSectionsData();
                return JSON.parse(JSON.stringify(sections));
            },
            getPreviewHtml: function() {
                updateSectionsData();
                return generatePreviewHtml();
            }
        };
    });

    function setupEventListeners() {
        document.getElementById('documentBody').addEventListener('click', function(e) {
            const section = e.target.closest('.editable-section');
            if (section) {
                setActiveSection(section);
            }
        });

        document.getElementById('documentBody').addEventListener('input', function(e) {
            if (e.target.contentEditable === 'true') {
                updateSectionContent(e.target);
            }
        });

        document.getElementById('modalBackdrop').addEventListener('click', closeVariableModal);
    }

    function addSection(type, content, afterElement, metadata) {
        content = content || '';
        metadata = metadata || {};
        const section = createSectionElement(type, content, metadata);
        const documentBody = document.getElementById('documentBody');

        if (afterElement) {
            afterElement.insertAdjacentElement('afterend', section);
        } else {
            const placeholder = documentBody.querySelector('.add-section-placeholder');
            if (placeholder) {
                documentBody.insertBefore(section, placeholder);
            } else {
                documentBody.appendChild(section);
            }
        }

        setActiveSection(section);
        updateSectionsData();
    }

    function createSectionElement(type, content, metadata) {
        content = content || '';
        metadata = metadata || {};
        const coercedMetadata = coerceMetadata(metadata);
        const normalizedType = normalizeFrontendType(type, coercedMetadata);
        const normalizedMetadata = ensureMetadataForType(normalizedType, coercedMetadata);
        const section = document.createElement('div');
        section.className = 'editable-section';
        section.dataset.type = normalizedType;
        section.dataset.id = 'section-' + Date.now();
        setSectionMetadata(section, normalizedMetadata);

        let processedContent = content;
        if (normalizedType !== 'html' && normalizedType !== 'signature') {
            // processedContent = convertBracketsToVariables(content || '');
        } else if (normalizedType === 'signature') {
            if (content) {
                processedContent = convertBracketsToVariables(content);
            }
        }

        let innerHTML = '';

        switch(normalizedType) {
            case 'text':
                innerHTML = '<div contenteditable="true" class="section-text" data-placeholder="텍스트를 입력하세요...">' + (processedContent || '') + '</div>';
                break;

            case 'title':
                innerHTML = '<div contenteditable="true" class="section-title" data-placeholder="제목을 입력하세요...">' + (processedContent || '') + '</div>';
                break;

            case 'clause':
                clauseCounter++;
                innerHTML = '<div class="section-clause">' +
                    '<span class="clause-number">' + clauseCounter + '.</span>' +
                    '<span contenteditable="true" data-placeholder="조항 내용을 입력하세요...">' + (processedContent || '') + '</span>' +
                    '</div>';
                break;

            case 'dotted':
                innerHTML = '<div contenteditable="true" class="section-dotted-box" data-placeholder="점선 박스 내용을 입력하세요...">' + (processedContent || '') + '</div>';
                break;

            case 'footer':
                innerHTML = '<div contenteditable="true" class="section-footer" data-placeholder="꼬릿말을 입력하세요...">' + (processedContent || '') + '</div>';
                break;

            case 'signature':
                if (content && processedContent) {
                    innerHTML = '<div class="section-signature" contenteditable="true">' + processedContent + '</div>';
                } else {
                    innerHTML = '<div class="section-signature" contenteditable="true">' +
                        '<div class="signature-section">' +
                            '<div class="signature-block signature-block--employee">' +
                                '<div class="signature-line">(근로자) 주소: <span class="template-variable" contenteditable="false"><span>EMPLOYEE_ADDRESS</span><span class="template-variable-remove"></span></span></div>' +
                                '<div class="signature-line">연락처: <span class="template-variable" contenteditable="false"><span>EMPLOYEE_PHONE</span><span class="template-variable-remove"></span></span></div>' +
                                '<div class="signature-line">성명: <span class="template-variable" contenteditable="false"><span>EMPLOYEE</span><span class="template-variable-remove"></span></span> (인)</div>' +
                            '</div>' +
                            '<div class="signature-block signature-block--employer">' +
                                '<div class="signature-line">(사업주) 사업체명: <span class="template-variable" contenteditable="false"><span>COMPANY_NAME</span><span class="template-variable-remove"></span></span></div>' +
                                '<div class="signature-line signature-line-indent">주소: <span class="template-variable" contenteditable="false"><span>EMPLOYER_ADDRESS</span><span class="template-variable-remove"></span></span></div>' +
                                '<div class="signature-line signature-line--seal signature-line-indent">' +
                                    '대표자: <span class="template-variable" contenteditable="false"><span>EMPLOYER</span><span class="template-variable-remove"></span></span> ' +
                                    '<span class="signature-stamp-label">(인)' +
                                        '<span class="signature-stamp-wrapper"><span class="template-variable" contenteditable="false"><span>EMPLOYER_SIGNATURE_IMAGE</span><span class="template-variable-remove"></span></span></span>' +
                                    '</span>' +
                                '</div>' +
                                '<div class="signature-line">(전화: <span class="template-variable" contenteditable="false"><span>EMPLOYER_PHONE</span><span class="template-variable-remove"></span></span>)</div>' +
                            '</div>' +
                        '</div>' +
                    '</div>';
                }
                break;

            case 'html':
                innerHTML = '<div class="html-section">' +
                    '<textarea class="html-editor" placeholder="HTML 코드를 입력하세요...">' + (content || '') + '</textarea>' +
                    '<div class="html-preview mt-2"></div>' +
                    '</div>';
                break;
            default:
                innerHTML = '<div contenteditable="true" class="section-text" data-placeholder="텍스트를 입력하세요...">' + (processedContent || '') + '</div>';
                break;
        }

        section.innerHTML = innerHTML + createSectionControls();

        section.querySelectorAll('.template-variable-remove').forEach(function(btn) {
            btn.onclick = function(e) {
                e.stopPropagation();
                btn.parentElement.remove();
                updateSectionsData();
            };
        });

        return section;
    }

    function createSectionControls() {
        return '<div class="section-controls">' +
            '<button class="section-control-btn" onclick="moveSection(this, \'up\')" title="위로">' +
                '<i class="bi bi-arrow-up"></i>' +
            '</button>' +
            '<button class="section-control-btn" onclick="moveSection(this, \'down\')" title="아래로">' +
                '<i class="bi bi-arrow-down"></i>' +
            '</button>' +
            '<button class="section-control-btn" onclick="duplicateSection(this)" title="복사">' +
                '<i class="bi bi-files"></i>' +
            '</button>' +
            '<button class="section-control-btn" onclick="deleteSection(this)" title="삭제">' +
                '<i class="bi bi-trash"></i>' +
            '</button>' +
        '</div>';
    }

    function createPlaceholder() {
        const placeholder = document.createElement('div');
        placeholder.className = 'add-section-placeholder';
        placeholder.onclick = function() { showAddSectionMenu(this); };
        placeholder.innerHTML = '<i class="bi bi-plus-circle"></i><div>여기를 클릭하여 섹션을 추가하세요</div>';
        return placeholder;
    }

    function setActiveSection(section) {
        document.querySelectorAll('.editable-section').forEach(function(s) { s.classList.remove('active'); });
        section.classList.add('active');
        activeElement = section.querySelector('[contenteditable="true"], .html-editor');
    }

    function moveSection(button, direction) {
        const section = button.closest('.editable-section');
        if (direction === 'up' && section.previousElementSibling && !section.previousElementSibling.classList.contains('add-section-placeholder')) {
            section.parentNode.insertBefore(section, section.previousElementSibling);
        } else if (direction === 'down' && section.nextElementSibling && !section.nextElementSibling.classList.contains('add-section-placeholder')) {
            section.parentNode.insertBefore(section.nextElementSibling, section);
        }
        updateSectionsData();
    }

    function duplicateSection(button) {
        const section = button.closest('.editable-section');
        const clone = section.cloneNode(true);
        clone.dataset.id = 'section-' + Date.now();
        section.parentNode.insertBefore(clone, section.nextSibling);
        updateSectionsData();
    }

    function deleteSection(button) {
        showConfirmModal(
            '이 섹션을 삭제하시겠습니까?',
            function() {
                const section = button.closest('.editable-section');
                section.remove();
                updateSectionsData();

                const documentBody = document.getElementById('documentBody');
                if (documentBody.children.length === 0) {
                    documentBody.appendChild(createPlaceholder());
                }
            },
            '삭제',
            '취소',
            'btn-danger'
        );
    }

    function showVariableModal() {
        document.getElementById('variableModal').classList.add('show');
        document.getElementById('modalBackdrop').classList.add('show');
    }

    function closeVariableModal() {
        document.getElementById('variableModal').classList.remove('show');
        document.getElementById('modalBackdrop').classList.remove('show');
    }

    function insertVariable(variable) {
        if (!activeElement) {
            showAlertModal('먼저 텍스트를 입력할 위치를 클릭해주세요.');
            return;
        }

        if (activeElement.contentEditable === 'true') {
            const selection = window.getSelection();
            let range;

            if (selection.rangeCount === 0 || !activeElement.contains(selection.anchorNode)) {
                range = document.createRange();
                range.selectNodeContents(activeElement);
                range.collapse(false);
                selection.removeAllRanges();
                selection.addRange(range);
            } else {
                range = selection.getRangeAt(0);
            }

            const varSpan = document.createElement('span');
            varSpan.className = 'template-variable';
            varSpan.contentEditable = 'false';
            varSpan.setAttribute('data-var-name', variable);

            const varText = document.createElement('span');
            varText.textContent = getDisplayName(variable);

            const removeBtn = document.createElement('span');
            removeBtn.className = 'template-variable-remove';
            removeBtn.onclick = function(e) {
                e.stopPropagation();
                varSpan.remove();
                updateSectionsData();
            };

            varSpan.appendChild(varText);
            varSpan.appendChild(removeBtn);

            range.deleteContents();
            range.insertNode(varSpan);

            const space = document.createTextNode(' ');
            range.setStartAfter(varSpan);
            range.insertNode(space);
            range.setStartAfter(space);
            range.collapse(true);

            selection.removeAllRanges();
            selection.addRange(range);

            activeElement.focus();
        } else if (activeElement.tagName === 'TEXTAREA') {
            const start = activeElement.selectionStart;
            const end = activeElement.selectionEnd;
            const text = activeElement.value;
            activeElement.value = text.substring(0, start) + variable + text.substring(end);
            activeElement.selectionStart = activeElement.selectionEnd = start + variable.length;
            activeElement.focus();
        }

        closeVariableModal();
        updateSectionsData();
    }

    function showAddSectionMenu(placeholder) {
        const menu = '<div class="add-section-menu">' +
            '<button class="toolbar-btn toolbar-btn-sm" onclick="addSectionFromPlaceholder(\'text\', this)">' +
                '<i class="bi bi-text-left"></i> 텍스트' +
            '</button>' +
            '<button class="toolbar-btn toolbar-btn-sm" onclick="addSectionFromPlaceholder(\'title\', this)">' +
                '<i class="bi bi-type"></i> 타이틀' +
            '</button>' +
            '<button class="toolbar-btn toolbar-btn-sm" onclick="addSectionFromPlaceholder(\'clause\', this)">' +
                '<i class="bi bi-list-ol"></i> 조항' +
            '</button>' +
            '<button class="toolbar-btn toolbar-btn-sm" onclick="addSectionFromPlaceholder(\'dotted\', this)">' +
                '<i class="bi bi-border-style"></i> 점선' +
            '</button>' +
            '<button class="toolbar-btn toolbar-btn-sm" onclick="addSectionFromPlaceholder(\'footer\', this)">' +
                '<i class="bi bi-text-center"></i> 꼬릿말' +
            '</button>' +
            '<button class="toolbar-btn toolbar-btn-sm" onclick="addSectionFromPlaceholder(\'signature\', this)">' +
                '<i class="bi bi-pen"></i> 서명란' +
            '</button>' +
            '<button class="toolbar-btn toolbar-btn-sm" onclick="addSectionFromPlaceholder(\'html\', this)">' +
                '<i class="bi bi-code-slash"></i> HTML' +
            '</button>' +
        '</div>';
        placeholder.innerHTML = menu;
    }

    function addSectionFromPlaceholder(type, button) {
        const placeholder = button.closest('.add-section-placeholder');
        const section = createSectionElement(type);
        placeholder.insertAdjacentElement('beforebegin', section);

        setActiveSection(section);
        updateSectionsData();
    }

    function updateSectionContent(element) {
        const section = element.closest('.editable-section');
        if (section) {
            updateSectionsData();
        }
    }

    function updateSectionsData() {
        const documentBody = document.getElementById('documentBody');
        sections = [];

        let clauseIndex = 0;

        documentBody.querySelectorAll('.editable-section').forEach(function(section, index) {
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
                    preview.innerHTML = normalizePreviewContent(type, content);
                }
            } else if (type === 'signature') {
                const signatureElement = section.querySelector('.section-signature');
                content = signatureElement ? convertVariablesToBrackets(signatureElement.innerHTML) : '';
            } else if (type === 'clause') {
                clauseIndex++;
                const numberElement = section.querySelector('.clause-number');
                if (numberElement) {
                    numberElement.textContent = clauseIndex + '.';
                }
                const clauseContentElement = section.querySelector('.section-clause > [contenteditable="true"]');
                content = clauseContentElement ? clauseContentElement.innerHTML : '';
            } else {
                const editableElement = section.querySelector('[contenteditable="true"]');
                content = editableElement ? editableElement.innerHTML : '';
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

    function loadInitialSections() {
        const scriptEl = document.getElementById('initialSections');
        if (scriptEl) {
            try {
                const rawPayload = scriptEl.textContent || '[]';
                console.debug('[TemplateEditor] raw initial sections payload:', rawPayload.substring(0, 500));

                const sectionsData = parseSectionsPayload(rawPayload);
                console.debug('[TemplateEditor] parsed sections array:', sectionsData);
                const documentBody = document.getElementById('documentBody');

                if (sectionsData.length > 0) {
                    documentBody.innerHTML = '';

                    clauseCounter = 0;
                    sectionsData.slice().sort(function(a, b) {
                            const orderA = typeof a.order === 'number' ? a.order : parseInt(a.order, 10) || 0;
                            const orderB = typeof b.order === 'number' ? b.order : parseInt(b.order, 10) || 0;
                            return orderA - orderB;
                        })
                        .forEach(function(sectionData) {
                            if (!sectionData) {
                                return;
                            }
                            const metadata = coerceMetadata(sectionData.metadata);
                            const section = createSectionElement(sectionData.type, sectionData.content, metadata);
                            section.dataset.id = sectionData.sectionId || ('section-' + Date.now());
                            documentBody.appendChild(section);
                        });

                    documentBody.appendChild(createPlaceholder());
                    updateSectionsData();
                } else {
                    if (!documentBody.querySelector('.add-section-placeholder')) {
                        documentBody.appendChild(createPlaceholder());
                    }
                }
            } catch (e) {
                console.error('Failed to load initial sections:', e);
            }
        }
    }

    function previewTemplate() {
        updateSectionsData();

        if (sections.length === 0) {
            showAlertModal('미리볼 섹션이 없습니다. 먼저 섹션을 추가해주세요.');
            return;
        }

        const previewHtml = generatePreviewHtml(sections);
        const previewTitle = document.getElementById('templateTitle').value || '제목 없음';
        const previewFrame = document.getElementById('previewFrame');

        if (previewFrame) {
            previewFrame.srcdoc = previewHtml;

            const modalElement = document.getElementById('previewModal');
            const modalTitle = document.getElementById('previewModalLabel');
            if (modalTitle) {
                modalTitle.textContent = previewTitle + ' 미리보기';
            }

            if (modalElement) {
                const modal = new bootstrap.Modal(modalElement);
                modal.show();
            }
        }
    }

    function collectSectionsForPreview() {
        const documentBody = document.getElementById('documentBody');
        const sectionElements = Array.from(documentBody.querySelectorAll('.editable-section'))
            .filter(function(section) { return !section.classList.contains('add-section-placeholder'); });

        return sectionElements.map(function(section, index) {
            const existingMetadata = getSectionMetadata(section);
            const type = normalizeFrontendType(section.dataset.type, existingMetadata);
            let content = '';

            if (type === 'html') {
                const textarea = section.querySelector('.html-editor');
                content = normalizePreviewContent(type, textarea ? textarea.value : '');
            } else if (type === 'signature') {
                const signatureElement = section.querySelector('.section-signature');
                content = normalizePreviewContent(type, signatureElement ? signatureElement.innerHTML : '');
            } else if (type === 'clause') {
                const clauseContent = section.querySelector('.section-clause span[contenteditable="true"]');
                content = normalizePreviewContent(type, clauseContent ? clauseContent.innerHTML : '');
            } else {
                const editableElement = section.querySelector('[contenteditable="true"]');
                content = normalizePreviewContent(type, editableElement ? editableElement.innerHTML : '');
            }

            return {
                sectionId: section.dataset.id,
                type: type,
                order: index,
                content: content,
                metadata: existingMetadata
            };
        });
    }

    function generatePreviewHtml(previewSections) {
        previewSections = previewSections || sections;
        const title = document.getElementById('templateTitle').value || '제목 없음';

        let bodyContent = '';
        let clauseIndex = 0;

        previewSections.forEach(function(section) {
            let rawContent = normalizePreviewContent(section.type, section.content);
            const codePoints = rawContent ? Array.from(rawContent).map(function(ch) { return ch.charCodeAt(0).toString(16); }) : [];
            console.debug('[TemplateEditor] rendering section type:', section.type, 'content length:', rawContent ? rawContent.length : 0, 'value:', JSON.stringify(rawContent), 'codes:', codePoints);
            switch (section.type) {
                case 'html':
                    bodyContent += rawContent;
                    console.debug('[TemplateEditor] appended markup:', rawContent && rawContent.substring ? rawContent.substring(0, 200) : rawContent);
                    break;
                case 'signature':
                    const signatureMarkup = '<div class="section-signature">' + rawContent + '</div>';
                    console.debug('[TemplateEditor] appended markup:', signatureMarkup.substring(0, 200));
                    bodyContent += signatureMarkup;
                    break;
                case 'clause':
                    clauseIndex++;
                    const clauseMarkup = '<div class="section-clause"><span class="clause-number">' + clauseIndex + '.</span> <span>' + rawContent + '</span></div>';
                    console.debug('[TemplateEditor] appended markup:', clauseMarkup.substring(0, 200));
                    bodyContent += clauseMarkup;
                    break;
                case 'title':
                    const titleMarkup = '<h2 class="section-title">' + rawContent + '</h2>';
                    console.debug('[TemplateEditor] appended markup:', titleMarkup.substring(0, 200));
                    bodyContent += titleMarkup;
                    break;
                case 'dotted':
                    const dottedMarkup = '<div class="section-dotted">' + rawContent + '</div>';
                    console.debug('[TemplateEditor] appended markup:', dottedMarkup.substring(0, 200));
                    bodyContent += dottedMarkup;
                    break;
                case 'footer':
                    const footerMarkup = '<div class="section-footer">' + rawContent + '</div>';
                    console.debug('[TemplateEditor] appended markup:', footerMarkup.substring(0, 200));
                    bodyContent += footerMarkup;
                    break;
                default:
                    console.debug('[TemplateEditor] default case - rawContent before markup:', JSON.stringify(rawContent), 'type:', typeof rawContent, 'length:', rawContent ? rawContent.length : 'null');
                    const textMarkup = '<div class="section-text">' + rawContent + '</div>';
                    console.debug('[TemplateEditor] default case - textMarkup:', textMarkup.substring(0, 200));
                    console.debug('[TemplateEditor] appended markup:', textMarkup.substring(0, 200));
                    bodyContent += textMarkup;
                    break;
            }
        });

        console.debug('[TemplateEditor] body content snippet:', bodyContent.substring(0, 500));

        return '<!DOCTYPE html>' +
        '<html lang="ko">' +
        '<head>' +
            '<meta charset="UTF-8">' +
            '<meta name="viewport" content="width=device-width, initial-scale=1.0">' +
            '<title>' + title + '</title>' +
            '<link rel="stylesheet" href="/css/contract-common.css">' +
        '</head>' +
        '<body>' +
            bodyContent +
        '</body>' +
        '</html>';
    }

    function saveTemplate() {
        const title = document.getElementById('templateTitle').value;

        if (!title) {
            showAlertModal('템플릿 제목을 입력해주세요.');
            document.getElementById('templateTitle').focus();
            return;
        }

        if (sections.length === 0) {
            showAlertModal('최소 하나 이상의 섹션을 추가해주세요.');
            return;
        }

        updateSectionsData();

        document.getElementById('formTitle').value = title;

        const serializedSections = sections.map(function(section, index) {
            return {
                sectionId: section.sectionId,
                type: mapFrontendTypeToServer(section.type, section.metadata),
                order: index,
                content: section.content,
                metadata: section.metadata || {}
            };
        });

        const templateData = {
            version: '1.0',
            metadata: {
                title: title,
                description: '',
                createdBy: '',
                variables: {}
            },
            sections: serializedSections
        };

        document.getElementById('sectionsJson').value = JSON.stringify(templateData);

        document.getElementById('templateForm').submit();
    }

    // 프리셋 템플릿 관련 함수들
    function loadPresetTemplates() {
        fetch('/templates/presets')
            .then(response => response.json())
            .then(presets => {
                displayPresetTemplates(presets);
            })
            .catch(error => {
                console.error('Failed to load preset templates:', error);
                displayPresetError();
            });
    }

    function displayPresetTemplates(presets) {
        const loading = document.getElementById('presetLoading');
        const grid = document.getElementById('presetGrid');
        
        loading.style.display = 'none';
        grid.style.display = 'grid';
        
        if (presets.length === 0) {
            grid.innerHTML = '<div class="preset-empty">' +
                '<i class="bi bi-inbox"></i>' +
                '<div>사용 가능한 프리셋 템플릿이 없습니다</div>' +
                '</div>';
            return;
        }
        
        grid.innerHTML = presets.map(preset => createPresetCard(preset)).join('');
    }

    function displayPresetError() {
        const loading = document.getElementById('presetLoading');
        const grid = document.getElementById('presetGrid');
        
        loading.style.display = 'none';
        grid.style.display = 'grid';
        grid.innerHTML = '<div class="preset-empty">' +
            '<i class="bi bi-exclamation-triangle"></i>' +
            '<div>프리셋 템플릿을 불러오는 중 오류가 발생했습니다</div>' +
            '</div>';
    }

    function createPresetCard(preset) {
        return '<div class="preset-card" data-preset-id="' + preset.id + '">' +
            '<div class="preset-card-header">' +
                '<div class="preset-card-icon">' +
                    '<i class="bi bi-file-text"></i>' +
                '</div>' +
                '<h6 class="preset-card-title">' + preset.name + '</h6>' +
            '</div>' +
            '<div class="preset-card-description">' +
                '미리 만들어진 템플릿을 기반으로 빠르게 시작할 수 있습니다' +
            '</div>' +
            '<div class="preset-card-action">' +
                '<span class="preset-card-preview">클릭하여 미리보기</span>' +
                '<button class="preset-card-button" onclick="loadPresetTemplate(\'' + preset.id + '\', \'' + preset.name + '\')">' +
                    '선택하여 시작하기' +
                '</button>' +
            '</div>' +
        '</div>';
    }

    function loadPresetTemplate(presetId, presetName) {
        // 현재 작업 내용이 있는지 확인
        if (sections.length > 0 || document.getElementById('templateTitle').value.trim()) {
            showConfirmModal(
                '현재 작업 중인 내용이 있습니다. 프리셋 템플릿을 로드하면 현재 내용이 초기화됩니다. 계속하시겠습니까?',
                function() {
                    performPresetLoad(presetId, presetName);
                },
                '로드',
                '취소',
                'btn-primary'
            );
        } else {
            performPresetLoad(presetId, presetName);
        }
    }

    function performPresetLoad(presetId, presetName) {
        fetch('/templates/presets/' + presetId + '/sections')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load preset template');
                }
                return response.json();
            })
            .then(data => {
                loadPresetSections(data.sections, presetName);
            })
            .catch(error => {
                console.error('Failed to load preset sections:', error);
                showAlertModal('프리셋 템플릿을 불러오는 중 오류가 발생했습니다.');
            });
    }

    function loadPresetSections(presetSections, presetName) {
        // clauseCounter 초기화
        clauseCounter = 0;
        
        // 현재 섹션들 모두 제거
        const documentBody = document.getElementById('documentBody');
        documentBody.innerHTML = '';
        
        // 템플릿 제목 설정
        document.getElementById('templateTitle').value = presetName;
        
        // 프리셋 섹션들을 빌더에 추가
        presetSections.forEach(function(sectionData) {
            const metadata = coerceMetadata(sectionData.metadata);
            
            // 변수들을 템플릿 변수 형식으로 변환
            let content = sectionData.content;
            if (sectionData.type !== 'html' && sectionData.type !== 'signature') {
                content = convertBracketsToVariables(content);
            }
            
            const section = createSectionElement(sectionData.type, content, metadata);
            section.dataset.id = sectionData.sectionId || ('section-' + Date.now() + '-' + Math.random());
            documentBody.appendChild(section);
        });
        
        // 플레이스홀더 추가
        documentBody.appendChild(createPlaceholder());
        
        // 섹션 데이터 업데이트
        updateSectionsData();
        
        // 성공 메시지 표시
        showAlertModal('프리셋 템플릿이 성공적으로 로드되었습니다. 필요에 맞게 수정하여 사용하세요.');
        
        // 프리셋 섹션 접기
        collapsePresetSection();
    }

    function togglePresetSection() {
        const content = document.getElementById('presetContent');
        const icon = document.getElementById('presetToggleIcon');
        
        if (content.classList.contains('collapsed')) {
            content.classList.remove('collapsed');
            icon.className = 'bi bi-chevron-up';
        } else {
            content.classList.add('collapsed');
            icon.className = 'bi bi-chevron-down';
        }
    }

    function collapsePresetSection() {
        const content = document.getElementById('presetContent');
        const icon = document.getElementById('presetToggleIcon');
        content.classList.add('collapsed');
        icon.className = 'bi bi-chevron-down';
    }

    document.addEventListener('input', function(e) {
        if (e.target.classList.contains('html-editor')) {
            const preview = e.target.parentElement.querySelector('.html-preview');
            if (preview) {
                preview.innerHTML = e.target.value;
            }
        }
    });

    document.addEventListener('keydown', function(e) {
        if ((e.ctrlKey || e.metaKey) && e.key === 's') {
            e.preventDefault();
            saveTemplate();
        }

        if ((e.ctrlKey || e.metaKey) && e.key === 'p') {
            e.preventDefault();
            previewTemplate();
        }

        if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
            e.preventDefault();
            showVariableModal();
        }
    });

</script>

<c:set var="sectionsJsonRaw" value="${empty template.sectionsJson ? '[]' : template.sectionsJson}" />
<c:set var="sectionsJsonSafe" value="${fn:replace(sectionsJsonRaw, '</script>', '<&#92;/script>')}" />
<script id="initialSections" type="application/json"><c:out value="${sectionsJsonSafe}" escapeXml="false" /></script>

<jsp:include page="../common/footer.jsp" />
</body>
</html>
