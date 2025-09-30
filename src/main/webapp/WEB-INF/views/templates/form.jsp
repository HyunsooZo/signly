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
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/templates.css" rel="stylesheet">
    <style>
        .builder-wrap {
            display: grid;
            grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
            gap: 1.5rem;
        }
        @media (max-width: 992px) {
            .builder-wrap {
                grid-template-columns: minmax(0, 1fr);
            }
        }
        .section-card {
            border-left: 4px solid var(--primary-color);
        }
        .section-card.dotted {
            border-left-color: #6c757d;
        }
        .section-card.footer {
            border-left-color: #0d6efd;
        }
        .preview-container {
            position: sticky;
            top: 20px;
            align-self: flex-start;
            transition: transform 0.2s ease;
            z-index: 10;
        }
        .preview-surface {
            background: #fff;
            padding: 1.5rem;
            max-height: calc(100vh - 120px);
            overflow: auto;
            min-height: 500px;
            word-wrap: break-word;
            word-break: break-word;
            font-size: 0.9rem;
            transition: all 0.3s ease;
            border-radius: 0 0 12px 12px;
        }
        .preview-surface .title {
            font-size: 1.1rem !important;
        }
        .preview-surface .section {
            margin: 8px 0 !important;
        }
        .preview-surface .signature-section {
            margin-top: 15px !important;
        }
        .preview-surface .template-header {
            border-bottom: 2px solid #0d6efd;
            padding-bottom: 1rem;
            margin-bottom: 1.5rem;
        }
        .preview-surface .template-paragraph {
            margin-bottom: 1.25rem;
        }
        .preview-surface .template-dotted {
            border: 2px dashed #adb5bd;
            border-radius: 12px;
            padding: 1rem 1.25rem;
            margin-bottom: 1.25rem;
            background-color: #f8f9fa;
        }
        .preview-surface .template-footer {
            border-top: 1px solid #dee2e6;
            margin-top: 2rem;
            padding-top: 1rem;
            text-align: right;
            color: #495057;
        }
        .section-actions button {
            min-width: 36px;
        }

        /* 미리보기 확대 기능 스타일 */
        .preview-surface {
            cursor: pointer;
        }

        /* 확대 모달 스타일 */
        .preview-modal {
            display: none;
            position: fixed;
            z-index: 9999;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.8);
            overflow: auto;
        }
        .preview-modal-content {
            background-color: white;
            margin: 2% auto;
            padding: 40px;
            border-radius: 8px;
            width: 90%;
            max-width: 900px;
            max-height: 90%;
            overflow-y: auto;
            position: relative;
            box-shadow: 0 20px 40px rgba(0,0,0,0.3);
        }
        .preview-modal-close {
            position: absolute;
            top: 15px;
            right: 20px;
            color: #666;
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            background: #f8f9fa;
            transition: all 0.2s ease;
        }
        .preview-modal-close:hover {
            color: #000;
            background: #e9ecef;
        }
        .preview-modal-header {
            border-bottom: 2px solid #0d6efd;
            padding-bottom: 15px;
            margin-bottom: 30px;
        }
        .preview-modal-title {
            margin: 0;
            color: #0d6efd;
            font-size: 1.5rem;
        }
        .variable-tag {
            display: inline-block;
            background-color: #e9f2ff;
            color: #1d4ed8;
            padding: 0.35rem 0.6rem;
            border-radius: 6px;
            margin: 0.15rem;
            font-size: 0.85rem;
            cursor: pointer;
        }
    </style>
</head>
<body>
<c:set var="formAction" value="/templates" />
<c:if test="${not empty templateId}">
    <c:set var="formAction" value="/templates/${templateId}" />
</c:if>
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
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

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="mb-2">
                <i class="bi bi-layout-text-window-reverse text-primary me-2"></i>
                ${pageTitle}
            </h2>
            <p class="text-muted mb-0">머릿말 · 본문 · 점선박스 · 꼬릿말 등 섹션을 추가하며 계약서 양식을 구성하세요.</p>
        </div>
        <a href="/templates" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left me-2"></i>목록으로
        </a>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle me-2"></i>${errorMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="builder-wrap">
        <div>
            <form method="post" id="templateForm" action="${formAction}">
                <c:if test="${not empty _csrf}">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                </c:if>
                <input type="hidden" name="sectionsJson" id="sectionsJson" value="${fn:escapeXml(template.sectionsJson)}">
                <div class="card mb-4">
                    <div class="card-body">
                        <div class="mb-3">
                            <label for="title" class="form-label">
                                <i class="bi bi-type me-2"></i>템플릿 제목 <span class="text-danger">*</span>
                            </label>
                            <input type="text"
                                   class="form-control form-control-lg"
                                   id="title"
                                   name="title"
                                   value="${template.title}"
                                   placeholder="예: 용역계약서, 임대차계약서, 매매계약서 등"
                                   required maxlength="255">
                        </div>
                    </div>
                </div>

                <div class="card mb-3">
                    <div class="card-header d-flex align-items-center justify-content-between">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-layout-text-sidebar me-2"></i>섹션 구성
                        </h5>
                        <div class="btn-group">
                            <button type="button" class="btn btn-outline-primary btn-sm" data-add="HEADER">
                                <i class="bi bi-layout-text-sidebar-reverse me-1"></i>머릿말
                            </button>
                            <button type="button" class="btn btn-outline-primary btn-sm" data-add="PARAGRAPH">
                                <i class="bi bi-text-paragraph me-1"></i>본문
                            </button>
                            <button type="button" class="btn btn-outline-primary btn-sm" data-add="DOTTED_BOX">
                                <i class="bi bi-bounding-box me-1"></i>점선 박스
                            </button>
                            <button type="button" class="btn btn-outline-primary btn-sm" data-add="FOOTER">
                                <i class="bi bi-pen me-1"></i>꼬릿말
                            </button>
                        </div>
                    </div>
                    <div class="card-body" id="sectionList">
                    </div>
                </div>

                <div class="card mb-4" id="variablePanel">
                    <div class="card-header">
                        <h6 class="card-title mb-0">
                            <i class="bi bi-info-circle me-2"></i>사용 가능한 변수
                        </h6>
                    </div>
                    <div class="card-body">
                        <p class="small text-muted mb-2">현재 커서가 있는 섹션에 변수를 삽입합니다.</p>
                        <div class="mb-2">
                            <strong>당사자 정보:</strong>
                            <span class="variable-tag" data-variable="{PARTY_A_NAME}">{PARTY_A_NAME}</span>
                            <span class="variable-tag" data-variable="{PARTY_A_ADDRESS}">{PARTY_A_ADDRESS}</span>
                            <span class="variable-tag" data-variable="{PARTY_B_NAME}">{PARTY_B_NAME}</span>
                            <span class="variable-tag" data-variable="{PARTY_B_ADDRESS}">{PARTY_B_ADDRESS}</span>
                        </div>
                        <div class="mb-2">
                            <strong>계약 정보:</strong>
                            <span class="variable-tag" data-variable="{CONTRACT_TITLE}">{CONTRACT_TITLE}</span>
                            <span class="variable-tag" data-variable="{CONTRACT_DATE}">{CONTRACT_DATE}</span>
                            <span class="variable-tag" data-variable="{CONTRACT_AMOUNT}">{CONTRACT_AMOUNT}</span>
                            <span class="variable-tag" data-variable="{START_DATE}">{START_DATE}</span>
                            <span class="variable-tag" data-variable="{END_DATE}">{END_DATE}</span>
                        </div>
                        <div>
                            <strong>서명 정보:</strong>
                            <span class="variable-tag" data-variable="{SIGNATURE_A}">{SIGNATURE_A}</span>
                            <span class="variable-tag" data-variable="{SIGNATURE_B}">{SIGNATURE_B}</span>
                            <span class="variable-tag" data-variable="{SIGNATURE_DATE}">{SIGNATURE_DATE}</span>
                        </div>
                    </div>
                </div>

                <div class="d-flex justify-content-between mb-5">
                    <a href="/templates" class="btn btn-secondary">
                        <i class="bi bi-x-circle me-2"></i>취소
                    </a>
                    <div>
                        <button type="button" class="btn btn-outline-primary me-2" id="previewButton">
                            <i class="bi bi-eye me-2"></i>미리보기
                        </button>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-check-circle me-2"></i>${not empty templateId ? '수정' : '생성'}
                        </button>
                    </div>
                </div>
            </form>
        </div>

        <div>
            <div class="preview-container">
                <div class="card mb-3">
                    <div class="card-header">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-display me-2"></i>실시간 미리보기
                            <small class="text-muted ms-2">
                                <i class="bi bi-zoom-in me-1"></i>클릭하여 확대보기
                            </small>
                        </h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="preview-surface" id="previewSurface">
                            <div class="text-muted text-center py-5">
                                <i class="bi bi-eye display-6 d-block mb-3"></i>
                                좌측에서 섹션을 추가하면 즉시 미리보기가 표시됩니다.
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="previewModal" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="bi bi-eye me-2"></i>템플릿 미리보기</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="preview-surface" id="modalPreview"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>

<script id="initialSections" type="application/json">${fn:escapeXml(template.sectionsJson)}</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const sectionTypes = {
        HEADER: { label: '머릿말', css: 'template-header', icon: 'bi bi-layout-text-sidebar-reverse' },
        PARAGRAPH: { label: '본문', css: 'template-paragraph', icon: 'bi bi-text-paragraph' },
        DOTTED_BOX: { label: '점선 박스', css: 'template-dotted', icon: 'bi bi-bounding-box' },
        FOOTER: { label: '꼬릿말', css: 'template-footer', icon: 'bi bi-pen' },
        CUSTOM: { label: 'HTML 블록', css: 'template-custom', icon: 'bi bi-code-square' },
        TEXT: { label: '텍스트 입력', css: 'template-text', icon: 'bi bi-input-cursor-text' },
        TEXTAREA: { label: '긴 텍스트', css: 'template-textarea', icon: 'bi bi-textarea-t' },
        DATE: { label: '날짜', css: 'template-date', icon: 'bi bi-calendar-date' },
        TIME: { label: '시간', css: 'template-time', icon: 'bi bi-clock' },
        NUMBER: { label: '숫자', css: 'template-number', icon: 'bi bi-123' },
        SELECT: { label: '선택', css: 'template-select', icon: 'bi bi-list-ul' }
    };

    let sections = [];
    let activeTextareaId = null;

    const sectionListEl = document.getElementById('sectionList');
    const previewEl = document.getElementById('previewSurface');

    function newSection(type) {
        return {
            sectionId: crypto.randomUUID ? crypto.randomUUID() : 'sec-' + Date.now(),
            type: type || 'PARAGRAPH',
            order: sections.length,
            content: '',
            metadata: {}
        };
    }

    function decodeHtmlEntities(str) {
        const textarea = document.createElement('textarea');
        textarea.innerHTML = str;
        return textarea.value;
    }

    function loadInitialSections() {
        try {
            const scriptEl = document.getElementById('initialSections');
            const raw = scriptEl ? scriptEl.textContent : '[]';
            const decoded = decodeHtmlEntities(raw);
            const parsed = JSON.parse(decoded || '[]');
            sections = Array.isArray(parsed) ? parsed.map((s, idx) => ({
                sectionId: s.sectionId || (s.id || 'sec-' + idx),
                type: s.type || 'PARAGRAPH',
                order: idx,
                content: s.content || '',
                metadata: s.metadata || {}
            })) : [];
        } catch (e) {
            sections = [];
        }
        if (sections.length === 0) {
            sections.push(newSection('HEADER'));
            sections.push(newSection('PARAGRAPH'));
        }
        renderSections();
        renderPreview();
    }

    function renderSections() {
        sectionListEl.innerHTML = '';
        sections.forEach((section, index) => {
            section.order = index;

            const card = document.createElement('div');
            card.className = 'card mb-3 section-card';
            if (section.type === 'DOTTED_BOX') {
                card.classList.add('dotted');
            } else if (section.type === 'FOOTER') {
                card.classList.add('footer');
            }
            card.dataset.sectionId = section.sectionId;

            const header = document.createElement('div');
            header.className = 'card-header d-flex justify-content-between align-items-center';

            const headerLeft = document.createElement('div');
            headerLeft.className = 'd-flex align-items-center gap-2';
            const badge = document.createElement('span');
            badge.className = 'badge bg-primary-subtle text-primary';
            const icon = document.createElement('i');
            const typeMeta = sectionTypes[section.type] || sectionTypes.PARAGRAPH;
            icon.className = typeMeta.icon;
            badge.appendChild(icon);
            const title = document.createElement('strong');
            title.textContent = typeMeta.label;
            headerLeft.appendChild(badge);
            headerLeft.appendChild(title);

            const actionGroup = document.createElement('div');
            actionGroup.className = 'section-actions btn-group btn-group-sm';

            const moveUp = document.createElement('button');
            moveUp.type = 'button';
            moveUp.className = 'btn btn-outline-secondary';
            moveUp.dataset.action = 'moveUp';
            moveUp.innerHTML = '<i class="bi bi-arrow-up"></i>';
            if (index === 0) {
                moveUp.setAttribute('disabled', 'disabled');
            }

            const moveDown = document.createElement('button');
            moveDown.type = 'button';
            moveDown.className = 'btn btn-outline-secondary';
            moveDown.dataset.action = 'moveDown';
            moveDown.innerHTML = '<i class="bi bi-arrow-down"></i>';
            if (index === sections.length - 1) {
                moveDown.setAttribute('disabled', 'disabled');
            }

            const removeBtn = document.createElement('button');
            removeBtn.type = 'button';
            removeBtn.className = 'btn btn-outline-danger';
            removeBtn.dataset.action = 'remove';
            removeBtn.innerHTML = '<i class="bi bi-x-lg"></i>';

            actionGroup.appendChild(moveUp);
            actionGroup.appendChild(moveDown);
            actionGroup.appendChild(removeBtn);

            header.appendChild(headerLeft);
            header.appendChild(actionGroup);

            const body = document.createElement('div');
            body.className = 'card-body';

            const typeGroup = document.createElement('div');
            typeGroup.className = 'mb-3';
            const typeLabel = document.createElement('label');
            typeLabel.className = 'form-label';
            typeLabel.textContent = '섹션 종류';
            const select = document.createElement('select');
            select.className = 'form-select form-select-sm';
            select.dataset.field = 'type';
            Object.keys(sectionTypes)
                .filter((key) => key !== 'CUSTOM' || section.type === 'CUSTOM')
                .forEach((key) => {
                    const option = document.createElement('option');
                    option.value = key;
                    option.textContent = sectionTypes[key].label;
                    if (key === section.type) {
                        option.selected = true;
                    }
                    select.appendChild(option);
                });
            typeGroup.appendChild(typeLabel);
            typeGroup.appendChild(select);

            const contentGroup = document.createElement('div');
            const contentLabel = document.createElement('label');
            contentLabel.className = 'form-label';
            const metadata = section.metadata || {};
            contentLabel.textContent = metadata.label || '내용';

            let inputElement;
            switch (section.type) {
                case 'TEXT':
                    inputElement = document.createElement('input');
                    inputElement.type = 'text';
                    inputElement.className = 'form-control';
                    inputElement.placeholder = metadata.placeholder || '텍스트를 입력하세요';
                    break;
                case 'TEXTAREA':
                    inputElement = document.createElement('textarea');
                    inputElement.className = 'form-control';
                    inputElement.rows = 3;
                    inputElement.placeholder = metadata.placeholder || '내용을 입력하세요';
                    break;
                case 'DATE':
                    inputElement = document.createElement('input');
                    inputElement.type = 'date';
                    inputElement.className = 'form-control';
                    break;
                case 'TIME':
                    inputElement = document.createElement('input');
                    inputElement.type = 'time';
                    inputElement.className = 'form-control';
                    break;
                case 'NUMBER':
                    inputElement = document.createElement('input');
                    inputElement.type = 'number';
                    inputElement.className = 'form-control';
                    inputElement.placeholder = metadata.placeholder || '숫자를 입력하세요';
                    break;
                case 'SELECT':
                    inputElement = document.createElement('select');
                    inputElement.className = 'form-select';
                    const defaultOption = document.createElement('option');
                    defaultOption.value = '';
                    defaultOption.textContent = '선택하세요';
                    inputElement.appendChild(defaultOption);
                    if (metadata.options) {
                        metadata.options.forEach(option => {
                            const optionElement = document.createElement('option');
                            optionElement.value = option.value;
                            optionElement.textContent = option.label;
                            inputElement.appendChild(optionElement);
                        });
                    }
                    break;
                default:
                    inputElement = document.createElement('textarea');
                    inputElement.className = 'form-control';
                    inputElement.rows = 4;
                    inputElement.placeholder = '내용을 입력하세요';
            }

            inputElement.dataset.field = 'content';
            inputElement.dataset.section = section.sectionId;
            inputElement.value = section.content || '';

            contentGroup.appendChild(contentLabel);
            contentGroup.appendChild(inputElement);

            body.appendChild(typeGroup);
            body.appendChild(contentGroup);

            card.appendChild(header);
            card.appendChild(body);

            sectionListEl.appendChild(card);
        });

        if (sections.length === 0) {
            sectionListEl.innerHTML = '<div class="text-center text-muted py-5">섹션을 추가해 계약서를 구성하세요.</div>';
        }

        if (activeTextareaId) {
            const activeField = sectionListEl.querySelector('textarea[data-section="' + activeTextareaId + '"]');
            if (activeField) {
                const caret = activeField.value.length;
                activeField.focus();
                if (typeof activeField.setSelectionRange === 'function') {
                    activeField.setSelectionRange(caret, caret);
                }
            }
        }
    }

    function renderPreview() {
        if (!sections.length) {
            previewEl.innerHTML = '<div class="text-muted text-center py-5"><i class="bi bi-eye display-6 d-block mb-3"></i>섹션을 추가하면 미리보기가 표시됩니다.</div>';
            return;
        }

        previewEl.innerHTML = sections.map(sectionToHtml).join('');
    }

    function sectionToHtml(section) {
        const safe = escapeHtml(section.content || '').replace(/\n/g, '<br>');
        const fallbacks = {
            HEADER: '머릿말을 입력하세요',
            DOTTED_BOX: '점선 박스 내용을 입력하세요',
            FOOTER: '꼬릿말을 입력하세요',
            PARAGRAPH: '본문 내용을 입력하세요'
        };
        if (section.type === 'HEADER') {
            return '<section class="template-header"><h2 class="mb-0">' + (safe || fallbacks.HEADER) + '</h2></section>';
        }
        if (section.type === 'DOTTED_BOX') {
            return '<section class="template-dotted"><div>' + (safe || fallbacks.DOTTED_BOX) + '</div></section>';
        }
        if (section.type === 'FOOTER') {
            return '<section class="template-footer">' + (safe || fallbacks.FOOTER) + '</section>';
        }
        return '<section class="template-paragraph"><p>' + (safe || fallbacks.PARAGRAPH) + '</p></section>';
    }

    function escapeHtml(text) {
        const map = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        };
        return String(text || '').replace(/[&<>"']/g, (match) => map[match]);
    }

    // =================================================================
    // =========== THIS IS THE CORRECTED CODE BLOCK ====================
    // =================================================================
    sectionListEl.addEventListener('change', (event) => {
        const card = event.target.closest('.card');
        if (!card) return;

        // 이 리스너는 UI 재빌드가 필요한 '섹션 타입' 변경만 처리해야 합니다.
        // 날짜, 드롭다운 등 다른 폼 필드 변경은 다른 리스너가 처리하도록 하여
        // 불필요한 UI 리셋을 방지합니다.
        if (event.target.dataset.field === 'type') {
            const id = card.dataset.sectionId;
            const section = sections.find(s => s.sectionId === id);
            if (section) {
                section.type = event.target.value;
                section.metadata = section.metadata || {};

                // 만약 프리셋 양식이었다가 다른 타입으로 변경되면, 프리셋 관련 메타데이터를 제거합니다.
                if (section.metadata.rawHtml && section.type !== 'CUSTOM') {
                    delete section.metadata.rawHtml;
                }

                activeTextareaId = id;
                // 섹션의 구조 자체가 바뀌었으므로 이 때만 renderSections()를 호출합니다.
                renderSections();
                renderPreview();
            }
        }
    });
    // =================================================================
    // ================= END OF CORRECTED CODE BLOCK ===================
    // =================================================================

    sectionListEl.addEventListener('input', (event) => {
        if (event.target.dataset.field === 'content') {
            const id = event.target.dataset.section;
            const section = sections.find(s => s.sectionId === id);
            if (section) {
                section.content = event.target.value;
            }
            activeTextareaId = id;
            renderPreview();
        }
    });

    sectionListEl.addEventListener('focusin', (event) => {
        if (event.target.dataset.field === 'content') {
            activeTextareaId = event.target.dataset.section;
        }
    });

    sectionListEl.addEventListener('click', (event) => {
        const button = event.target.closest('button');
        if (!button) {
            return;
        }
        const action = button.dataset.action;
        if (!action) {
            return;
        }
        const card = event.target.closest('.card');
        if (!card) return;
        const id = card.dataset.sectionId;
        const index = sections.findIndex(s => s.sectionId === id);
        if (index === -1) return;

        if (action === 'remove') {
            sections.splice(index, 1);
            if (activeTextareaId === id) {
                activeTextareaId = null;
            }
        } else if (action === 'moveUp' && index > 0) {
            [sections[index - 1], sections[index]] = [sections[index], sections[index - 1]];
            activeTextareaId = id;
        } else if (action === 'moveDown' && index < sections.length - 1) {
            [sections[index + 1], sections[index]] = [sections[index], sections[index + 1]];
            activeTextareaId = id;
        }
        renderSections();
        renderPreview();
    });

    document.querySelectorAll('[data-add]').forEach(button => {
        button.addEventListener('click', () => {
            const newItem = newSection(button.dataset.add);
            sections.push(newItem);
            activeTextareaId = newItem.sectionId;
            renderSections();
            renderPreview();
        });
    });

    document.querySelectorAll('.variable-tag').forEach(tag => {
        tag.addEventListener('click', () => insertVariable(tag.dataset.variable));
    });

    function insertVariable(variable) {
        if (!activeTextareaId) {
            alert('변수를 삽입할 섹션을 먼저 선택해주세요.');
            return;
        }
        const textareaSelector = 'textarea[data-section="' + activeTextareaId + '"]';
        const textarea = sectionListEl.querySelector(textareaSelector);
        if (!textarea) return;
        const start = textarea.selectionStart || 0;
        const end = textarea.selectionEnd || 0;
        const value = textarea.value;
        textarea.value = value.slice(0, start) + variable + value.slice(end);
        textarea.focus();
        textarea.selectionStart = textarea.selectionEnd = start + variable.length;
        const section = sections.find(s => s.sectionId === activeTextareaId);
        if (section) {
            section.content = textarea.value;
        }
        renderPreview();
    }

    document.getElementById('templateForm').addEventListener('submit', (event) => {
        if (!sections.length) {
            event.preventDefault();
            alert('최소 한 개 이상의 섹션을 추가해주세요.');
            return;
        }
        const payload = sections.map((section, idx) => ({
            sectionId: section.sectionId,
            type: section.type,
            order: idx,
            content: section.content,
            metadata: section.metadata || {}
        }));
        document.getElementById('sectionsJson').value = JSON.stringify(payload);
    });

    document.getElementById('previewButton').addEventListener('click', () => {
        const modalBody = document.getElementById('modalPreview');
        modalBody.innerHTML = sections.length ? sections.map(sectionToHtml).join('') : '<div class="text-muted text-center py-5">섹션을 추가하면 미리보기가 표시됩니다.</div>';
        const modal = new bootstrap.Modal(document.getElementById('previewModal'));
        modal.show();
    });


    // 미리보기 확대 기능
    function initPreviewZoom() {
        // DOM 로드 대기
        setTimeout(() => {
            try {
                const previewSurface = document.getElementById('previewSurface');
                const modalElement = document.getElementById('previewModal');
                const modalPreview = document.getElementById('modalPreview');

                console.log('[DEBUG] initPreviewZoom 요소 확인:');
                console.log('- previewSurface:', previewSurface);
                console.log('- modalElement:', modalElement);
                console.log('- modalPreview:', modalPreview);

                if (!previewSurface) {
                    console.warn('[WARNING] previewSurface 요소를 찾을 수 없습니다');
                    return;
                }

                if (!modalElement) {
                    console.warn('[WARNING] previewModal 요소를 찾을 수 없습니다');
                    return;
                }

                if (!modalPreview) {
                    console.warn('[WARNING] modalPreview 요소를 찾을 수 없습니다');
                    return;
                }

                // Bootstrap 모달 확인
                if (typeof bootstrap !== 'undefined') {
                    // Bootstrap 모달 인스턴스 생성
                    const modal = new bootstrap.Modal(modalElement);

                    // 미리보기 클릭 시 확대
                    previewSurface.addEventListener('click', () => {
                        try {
                            if (sections && sections.length > 0) {
                                modalPreview.innerHTML = previewSurface.innerHTML;
                                modal.show();
                                console.log('[DEBUG] Bootstrap 모달 열기 성공');
                            }
                        } catch (err) {
                            console.error('[ERROR] Bootstrap 모달 표시 중 오류:', err);
                        }
                    });
                } else {
                    // Bootstrap이 없는 경우 간단한 모달 로직
                    let isModalOpen = false;

                    // 미리보기 클릭 시 확대
                    previewSurface.addEventListener('click', () => {
                        try {
                            if (!isModalOpen && sections && sections.length > 0) {
                                modalPreview.innerHTML = previewSurface.innerHTML;
                                modalElement.style.display = 'block';
                                modalElement.classList.add('show');
                                isModalOpen = true;
                                console.log('[DEBUG] 간단한 모달 열기 성공');
                            }
                        } catch (err) {
                            console.error('[ERROR] 간단한 모달 표시 중 오류:', err);
                        }
                    });

                    // 모달 닫기 이벤트
                    const closeButton = modalElement.querySelector('.btn-close');
                    if (closeButton) {
                        closeButton.addEventListener('click', () => {
                            modalElement.style.display = 'none';
                            modalElement.classList.remove('show');
                            isModalOpen = false;
                        });
                    }

                    // ESC 키로 모달 닫기
                    document.addEventListener('keydown', (e) => {
                        if (e.key === 'Escape' && isModalOpen) {
                            modalElement.style.display = 'none';
                            modalElement.classList.remove('show');
                            isModalOpen = false;
                        }
                    });
                }

                console.log('[SUCCESS] 미리보기 확대 기능 초기화 완료');

            } catch (error) {
                console.error('[ERROR] initPreviewZoom 전체 실패:', error);
            }
        }, 500); // 500ms 후 실행으로 DOM 로드 보장
    }

    // 플로팅 미리보기 효과 초기화
    function initFloatingPreview() {
        const previewContainer = document.querySelector('.preview-container');
        const previewSurface = document.querySelector('.preview-surface');

        if (!previewContainer || !previewSurface) return;

        let isMouseNear = false;
        let mouseX = 0;
        let mouseY = 0;

        // 마우스 움직임 추적
        document.addEventListener('mousemove', (e) => {
            mouseX = e.clientX;
            mouseY = e.clientY;

            const previewRect = previewSurface.getBoundingClientRect();
            const distance = Math.sqrt(
                Math.pow(mouseX - (previewRect.left + previewRect.width / 2), 2) +
                Math.pow(mouseY - (previewRect.top + previewRect.height / 2), 2)
            );

            const threshold = 200; // 200px 이내에서 반응
            const wasMouseNear = isMouseNear;
            isMouseNear = distance < threshold;

            if (isMouseNear !== wasMouseNear) {
                if (isMouseNear) {
                    previewSurface.style.transform = 'scale(1.02)';
                    previewSurface.style.boxShadow = '0 8px 25px rgba(0,0,0,0.15)';
                } else {
                    previewSurface.style.transform = 'scale(1)';
                    previewSurface.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
                }
            }
        });

        // 스크롤 이벤트로 sticky 효과 강화
        let lastScrollTop = 0;
        window.addEventListener('scroll', () => {
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            const scrollDirection = scrollTop > lastScrollTop ? 'down' : 'up';

            // 스크롤 방향에 따라 약간의 애니메이션 효과
            if (scrollDirection === 'down') {
                previewContainer.style.transform = 'translateY(2px)';
            } else {
                previewContainer.style.transform = 'translateY(-2px)';
            }

            // 0.1초 후 원래대로
            setTimeout(() => {
                previewContainer.style.transform = 'translateY(0)';
            }, 100);

            lastScrollTop = scrollTop <= 0 ? 0 : scrollTop;
        }, { passive: true });

        // 창 크기 변경 시 위치 재조정
        window.addEventListener('resize', () => {
            previewContainer.style.top = '20px';
        });
    }

    loadInitialSections();
    initPreviewZoom();
    initFloatingPreview();
</script>


</body>
</html>