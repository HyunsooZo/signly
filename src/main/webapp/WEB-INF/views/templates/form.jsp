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
                        <c:if test="${not empty presets}">
                            <div class="mb-3">
                                <label for="presetSelect" class="form-label">
                                    <i class="bi bi-journal-richtext me-2"></i>표준 양식 불러오기
                                </label>
                                <select class="form-select" id="presetSelect">
                                    <option value="">표준 양식을 선택하세요</option>
                                    <c:forEach var="preset" items="${presets}">
                                        <option value="${preset.id}" data-name="${preset.name}">
                                            ${preset.name} - ${preset.description}
                                        </option>
                                    </c:forEach>
                                </select>
                                <div class="form-text">선택하면 해당 양식이 본문 섹션에 자동으로 적용됩니다.</div>
                            </div>
                        </c:if>
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
                        <!-- 섹션 카드 렌더링 영역 -->
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

<!-- 미리보기 모달 -->
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
    let presetFormData = {}; // 프리셋 폼 데이터 보존용

    const sectionListEl = document.getElementById('sectionList');
    const previewEl = document.getElementById('previewSurface');
    const presetSelect = document.getElementById('presetSelect');
    const templateTitleInput = document.getElementById('title');

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

    // 프리셋 폼 데이터 저장
    function savePresetFormData() {
        const formFields = document.querySelectorAll('.preset-form-fields input, .preset-form-fields select, .preset-form-fields textarea');
        formFields.forEach(field => {
            const fieldName = field.dataset.field;
            if (fieldName) {
                if (field.type === 'radio') {
                    if (field.checked) {
                        presetFormData[fieldName] = field.value;
                    }
                } else {
                    presetFormData[fieldName] = field.value;
                }
            }
        });
    }

    // 프리셋 폼 데이터 복원
    function restorePresetFormData() {
        const formFields = document.querySelectorAll('.preset-form-fields input, .preset-form-fields select, .preset-form-fields textarea');
        formFields.forEach(field => {
            const fieldName = field.dataset.field;
            if (fieldName && presetFormData[fieldName] !== undefined) {
                if (field.type === 'radio') {
                    field.checked = field.value === presetFormData[fieldName];
                } else {
                    field.value = presetFormData[fieldName];
                }
            }
        });
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

        // 변수 패널 표시/숨김 처리
        const variablePanel = document.getElementById('variablePanel');
        const hasPresetSections = sections.some(s => s.metadata?.rawHtml);
        if (hasPresetSections && variablePanel) {
            variablePanel.style.display = 'none';
        } else if (variablePanel) {
            variablePanel.style.display = 'block';
        }
    }

    function renderSections() {
        console.log('[DEBUG] renderSections 시작, sections 개수:', sections.length);
        console.log('[DEBUG] sectionListEl:', sectionListEl);

        // 기존 프리셋 폼 데이터 저장
        savePresetFormData();

        sectionListEl.innerHTML = '';
        sections.forEach((section, index) => {
            console.log('[DEBUG] 섹션 렌더링:', index, section);
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
            const textarea = inputElement; // 기존 코드와의 호환성을 위해
            const isRawHtml = metadata.rawHtml === true;
            if (isRawHtml) {
                select.setAttribute('disabled', 'disabled');
                textarea.style.display = 'none'; // HTML 코드 숨기기

                // 사용자 친화적 폼 필드들을 만들 예정
                const formFieldsContainer = document.createElement('div');
                formFieldsContainer.className = 'preset-form-fields';
                formFieldsContainer.innerHTML = `
                    <div class="alert alert-info">
                        <i class="bi bi-info-circle me-2"></i>
                        표준 근로계약서 양식이 로드되었습니다. 아래 필드들을 채워주세요.
                    </div>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">사업주명</label>
                            <input type="text" class="form-control" data-field="employer" placeholder="회사명 또는 사업주명">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">근로자명</label>
                            <input type="text" class="form-control" data-field="employee" placeholder="근로자 성명">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">계약 시작일</label>
                            <input type="date" class="form-control" data-field="contractStartDate">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">계약 종료일 (선택사항)</label>
                            <input type="date" class="form-control" data-field="contractEndDate">
                        </div>
                        <div class="col-md-12">
                            <label class="form-label">근무장소</label>
                            <input type="text" class="form-control" data-field="workplace" placeholder="근무지를 입력하세요">
                        </div>
                        <div class="col-md-12">
                            <label class="form-label">업무내용</label>
                            <textarea class="form-control" data-field="jobDescription" rows="3" placeholder="담당 업무를 입력하세요"></textarea>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">근무 시작시간</label>
                            <input type="time" class="form-control" data-field="workStartTime">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">근무 종료시간</label>
                            <input type="time" class="form-control" data-field="workEndTime">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">휴게 시작시간</label>
                            <input type="time" class="form-control" data-field="breakStartTime">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">휴게 종료시간</label>
                            <input type="time" class="form-control" data-field="breakEndTime">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">주 근무일수</label>
                            <input type="number" class="form-control" data-field="workDays" placeholder="5" min="1" max="7">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">휴일</label>
                            <input type="text" class="form-control" data-field="holidays" placeholder="토, 일">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">월급 (원)</label>
                            <input type="number" class="form-control" data-field="monthlySalary" placeholder="3000000" min="0">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">상여금 (원)</label>
                            <input type="number" class="form-control" data-field="bonus" placeholder="0" min="0">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">급여 지급일</label>
                            <input type="number" class="form-control" data-field="paymentDay" placeholder="25" min="1" max="31">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">지급방법</label>
                            <select class="form-select" data-field="paymentMethod">
                                <option value="">선택하세요</option>
                                <option value="direct">직접 지급</option>
                                <option value="bank">계좌 입금</option>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">계약일</label>
                            <input type="date" class="form-control" data-field="contractDate">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">사업체명</label>
                            <input type="text" class="form-control" data-field="companyName" placeholder="회사명">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">사업주 주소</label>
                            <input type="text" class="form-control" data-field="employerAddress" placeholder="주소를 입력하세요">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">사업주 전화번호</label>
                            <input type="tel" class="form-control" data-field="employerPhone" placeholder="010-0000-0000">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">근로자 주소</label>
                            <input type="text" class="form-control" data-field="employeeAddress" placeholder="주소를 입력하세요">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">근로자 연락처</label>
                            <input type="tel" class="form-control" data-field="employeePhone" placeholder="010-0000-0000">
                        </div>
                    </div>
                `;
                contentGroup.appendChild(formFieldsContainer);

                // 폼 필드 변경 시 HTML 업데이트
                const formFields = formFieldsContainer.querySelectorAll('[data-field]');
                formFields.forEach(field => {
                    // 다양한 입력 타입에 대한 이벤트 설정
                    const events = [];

                    if (field.type === 'radio' || field.type === 'checkbox') {
                        events.push('change');
                    } else if (field.type === 'number' || field.type === 'date' || field.type === 'time') {
                        events.push('input', 'change');
                    } else if (field.tagName === 'SELECT') {
                        events.push('change');
                    } else {
                        events.push('input', 'change', 'blur');
                    }

                    events.forEach(eventType => {
                        field.addEventListener(eventType, () => {
                            // 약간의 지연을 두어 입력 완료 후 업데이트
                            clearTimeout(field.updateTimer);
                            field.updateTimer = setTimeout(() => {
                                savePresetFormData(); // 데이터 저장
                                renderPreview(); // 미리보기 업데이트
                            }, field.type === 'number' || field.type === 'date' || field.type === 'time' ? 100 : 0);
                        });
                    });

                    // 숫자, 날짜, 시간 필드에 대한 추가 키보드 이벤트
                    if (field.type === 'number' || field.type === 'date' || field.type === 'time') {
                        field.addEventListener('keyup', () => {
                            clearTimeout(field.updateTimer);
                            field.updateTimer = setTimeout(() => {
                                savePresetFormData();
                                renderPreview();
                            }, 200);
                        });

                        // 마우스 휠로 숫자 변경 시에도 반응
                        if (field.type === 'number') {
                            field.addEventListener('wheel', () => {
                                setTimeout(() => {
                                    savePresetFormData();
                                    renderPreview();
                                }, 100);
                            });
                        }
                    }
                });

                // 저장된 데이터 복원 후 미리보기 업데이트
                setTimeout(() => {
                    restorePresetFormData();
                    renderPreview(); // 복원 후 미리보기 업데이트
                }, 0);
            }
            if (!isRawHtml) {
                contentGroup.appendChild(contentLabel);
                contentGroup.appendChild(inputElement);
            } else {
                // HTML 프리셋일 때는 원본 텍스트 영역을 완전히 숨김
                const helper = document.createElement('div');
                helper.className = 'form-text mt-2';
                helper.innerHTML = '<small class="text-muted">💡 위 필드들을 채우면 표준 양식이 자동 완성됩니다.</small>';
                contentGroup.appendChild(helper);
            }

            body.appendChild(typeGroup);
            body.appendChild(contentGroup);

            card.appendChild(header);
            card.appendChild(body);

            sectionListEl.appendChild(card);
            console.log('[DEBUG] 섹션 카드 추가됨:', index);
        });

        if (sections.length === 0) {
            console.log('[DEBUG] 섹션이 없어서 빈 메시지 표시');
            sectionListEl.innerHTML = '<div class="text-center text-muted py-5">섹션을 추가해 계약서를 구성하세요.</div>';
        }
        console.log('[DEBUG] renderSections 완료, DOM 상태:', sectionListEl.children.length);

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

        const hasRawHtml = sections.some(s => s.metadata?.rawHtml);
        if (hasRawHtml) {
            const rawSection = sections.find(s => s.metadata?.rawHtml);
            if (rawSection && rawSection.content) {
                // CSS 스타일 추출
                const styleMatches = rawSection.content.match(/<style[^>]*>([\s\S]*?)<\/style>/gi);
                let extractedStyles = '';
                if (styleMatches) {
                    extractedStyles = styleMatches.map(match =>
                        match.replace(/<\/?style[^>]*>/gi, '')
                    ).join('\n');
                }

                // HTML 정리 (스타일은 보존)
                let cleanedHtml = rawSection.content.replace(/<script[^>]*>[\s\S]*?<\/script>/gi, '');
                cleanedHtml = cleanedHtml.replace(/<!DOCTYPE[^>]*>/gi, '');
                cleanedHtml = cleanedHtml.replace(/<html[^>]*>/gi, '');
                cleanedHtml = cleanedHtml.replace(/<\/html>/gi, '');
                cleanedHtml = cleanedHtml.replace(/<head[^>]*>[\s\S]*?<\/head>/gi, '');
                cleanedHtml = cleanedHtml.replace(/<body[^>]*>/gi, '');
                cleanedHtml = cleanedHtml.replace(/<\/body>/gi, '');
                cleanedHtml = cleanedHtml.replace(/<style[^>]*>[\s\S]*?<\/style>/gi, '');

                // 폼 필드 값을 HTML에 반영
                let updatedHtml = cleanedHtml;
                console.log('[DEBUG] 원본 HTML 전체:', cleanedHtml); // 전체 HTML 확인용
                console.log('[DEBUG] 원본 HTML에서 [EMPLOYER] 찾기:', cleanedHtml.indexOf('[EMPLOYER]'));
                console.log('[DEBUG] 원본 HTML에서 blank-line 찾기:', cleanedHtml.indexOf('blank-line'));
                const formFields = document.querySelectorAll('.preset-form-fields input, .preset-form-fields select, .preset-form-fields textarea');
                formFields.forEach(field => {
                    const fieldName = field.dataset.field;
                    let value = '';

                    if (field.type === 'radio') {
                        if (field.checked) {
                            value = field.value;
                        } else {
                            return; // 체크되지 않은 라디오 버튼은 건너뛰기
                        }
                    } else {
                        value = field.value || ''; // trim() 제거하고 빈 값도 허용
                    }

                    console.log('[DEBUG] 필드 처리:', fieldName, '값:', value); // 디버깅용

                    if (fieldName) { // value 조건 제거
                        switch(fieldName) {
                            case 'employer':
                                updatedHtml = updatedHtml.replace(/\[EMPLOYER\]/g, value || '');
                                console.log('[DEBUG] employer 치환:', value);
                                break;
                            case 'employee':
                                updatedHtml = updatedHtml.replace(/\[EMPLOYEE\]/g, value || '');
                                console.log('[DEBUG] employee 치환:', value);
                                break;
                            case 'contractStartDate':
                                if (value) {
                                    const startDate = new Date(value);
                                    if (!isNaN(startDate)) {
                                        const year = startDate.getFullYear();
                                        const month = String(startDate.getMonth() + 1).padStart(2, '0');
                                        const day = String(startDate.getDate()).padStart(2, '0');
                                        const formattedDate = `${year}년 ${month}월 ${day}일`;
                                        updatedHtml = updatedHtml.replace(/\[CONTRACT_START_DATE\]/g, formattedDate);
                                        console.log('[DEBUG] contractStartDate 치환 성공:', formattedDate);
                                    }
                                } else {
                                    updatedHtml = updatedHtml.replace(/\[CONTRACT_START_DATE\]/g, '');
                                }
                                break;
                            case 'contractEndDate':
                                if (value) {
                                    const endDate = new Date(value);
                                    if (!isNaN(endDate)) {
                                        const year = endDate.getFullYear();
                                        const month = String(endDate.getMonth() + 1).padStart(2, '0');
                                        const day = String(endDate.getDate()).padStart(2, '0');
                                        const formattedDate = `${year}년 ${month}월 ${day}일`;
                                        updatedHtml = updatedHtml.replace(/\[CONTRACT_END_DATE\]/g, formattedDate);
                                        console.log('[DEBUG] contractEndDate 치환 성공:', formattedDate);
                                    }
                                } else {
                                    updatedHtml = updatedHtml.replace(/\[CONTRACT_END_DATE\]/g, '');
                                }
                                break;
                            case 'workplace':
                                updatedHtml = updatedHtml.replace(/\[WORKPLACE\]/g, value || '');
                                console.log('[DEBUG] workplace 치환:', value);
                                break;
                            case 'jobDescription':
                                updatedHtml = updatedHtml.replace(/\[JOB_DESCRIPTION\]/g, value || '');
                                console.log('[DEBUG] jobDescription 치환:', value);
                                break;
                            case 'workStartTime':
                                if (value && value.includes(':')) {
                                    const [startHour, startMin] = value.split(':');
                                    const formattedTime = `${startHour}시 ${startMin}분`;
                                    updatedHtml = updatedHtml.replace(/\[WORK_START_TIME\]/g, formattedTime);
                                } else {
                                    updatedHtml = updatedHtml.replace(/\[WORK_START_TIME\]/g, '');
                                }
                                console.log('[DEBUG] workStartTime 치환:', value);
                                break;
                            case 'workEndTime':
                                if (value && value.includes(':')) {
                                    const [endHour, endMin] = value.split(':');
                                    const formattedTime = `${endHour}시 ${endMin}분`;
                                    updatedHtml = updatedHtml.replace(/\[WORK_END_TIME\]/g, formattedTime);
                                } else {
                                    updatedHtml = updatedHtml.replace(/\[WORK_END_TIME\]/g, '');
                                }
                                console.log('[DEBUG] workEndTime 치환:', value);
                                break;
                            case 'breakStartTime':
                                if (value && value.includes(':')) {
                                    const [breakStartH, breakStartM] = value.split(':');
                                    const formattedTime = `${breakStartH}시 ${breakStartM}분`;
                                    updatedHtml = updatedHtml.replace(/\[BREAK_START_TIME\]/g, formattedTime);
                                } else {
                                    updatedHtml = updatedHtml.replace(/\[BREAK_START_TIME\]/g, '');
                                }
                                console.log('[DEBUG] breakStartTime 치환:', value);
                                break;
                            case 'breakEndTime':
                                if (value && value.includes(':')) {
                                    const [breakEndH, breakEndM] = value.split(':');
                                    const formattedTime = `${breakEndH}시 ${breakEndM}분`;
                                    updatedHtml = updatedHtml.replace(/\[BREAK_END_TIME\]/g, formattedTime);
                                } else {
                                    updatedHtml = updatedHtml.replace(/\[BREAK_END_TIME\]/g, '');
                                }
                                console.log('[DEBUG] breakEndTime 치환:', value);
                                break;
                            case 'workDays':
                                updatedHtml = updatedHtml.replace(/\[WORK_DAYS\]/g, value || '');
                                console.log('[DEBUG] workDays 치환:', value);
                                break;
                            case 'holidays':
                                updatedHtml = updatedHtml.replace(/\[HOLIDAYS\]/g, value || '');
                                console.log('[DEBUG] holidays 치환:', value);
                                break;
                            case 'monthlySalary':
                                if (value) {
                                    const monthlySalaryAmount = parseInt(value);
                                    if (!isNaN(monthlySalaryAmount) && monthlySalaryAmount > 0) {
                                        updatedHtml = updatedHtml.replace(/\[MONTHLY_SALARY\]/g, `${monthlySalaryAmount.toLocaleString()}`);
                                    } else {
                                        updatedHtml = updatedHtml.replace(/\[MONTHLY_SALARY\]/g, '');
                                    }
                                } else {
                                    updatedHtml = updatedHtml.replace(/\[MONTHLY_SALARY\]/g, '');
                                }
                                console.log('[DEBUG] monthlySalary 치환:', value);
                                break;
                            case 'bonus':
                                if (value) {
                                    const bonusAmount = parseInt(value);
                                    if (!isNaN(bonusAmount) && bonusAmount > 0) {
                                        updatedHtml = updatedHtml.replace(/\[BONUS\]/g, `있음 (${bonusAmount.toLocaleString()}원)`);
                                    } else {
                                        updatedHtml = updatedHtml.replace(/\[BONUS\]/g, '없음');
                                    }
                                } else {
                                    updatedHtml = updatedHtml.replace(/\[BONUS\]/g, '없음');
                                }
                                console.log('[DEBUG] bonus 치환:', value);
                                break;
                            case 'paymentDay':
                                updatedHtml = updatedHtml.replace(/\[PAYMENT_DAY\]/g, value || '');
                                console.log('[DEBUG] paymentDay 치환:', value);
                                break;
                            case 'paymentMethod':
                                let paymentMethodText = '';
                                if (value === 'direct') {
                                    paymentMethodText = '근로자에게 직접 지급';
                                } else if (value === 'bank') {
                                    paymentMethodText = '근로자 명의 예금통장에 입금';
                                }
                                updatedHtml = updatedHtml.replace(/\[PAYMENT_METHOD\]/g, paymentMethodText);
                                console.log('[DEBUG] paymentMethod 치환:', value);
                                break;
                            case 'contractDate':
                                if (value) {
                                    const contractDate = new Date(value);
                                    if (!isNaN(contractDate)) {
                                        const year = contractDate.getFullYear();
                                        const month = String(contractDate.getMonth() + 1).padStart(2, '0');
                                        const day = String(contractDate.getDate()).padStart(2, '0');
                                        const formattedDate = `${year}년 ${month}월 ${day}일`;
                                        updatedHtml = updatedHtml.replace(/\[CONTRACT_DATE\]/g, formattedDate);
                                    }
                                } else {
                                    updatedHtml = updatedHtml.replace(/\[CONTRACT_DATE\]/g, '');
                                }
                                console.log('[DEBUG] contractDate 치환:', value);
                                break;
                            case 'companyName':
                                updatedHtml = updatedHtml.replace(/\[COMPANY_NAME\]/g, value || '');
                                console.log('[DEBUG] companyName 치환:', value);
                                break;
                            case 'employerAddress':
                                updatedHtml = updatedHtml.replace(/\[EMPLOYER_ADDRESS\]/g, value || '');
                                console.log('[DEBUG] employerAddress 치환:', value);
                                break;
                            case 'employerPhone':
                                updatedHtml = updatedHtml.replace(/\[EMPLOYER_PHONE\]/g, value || '');
                                console.log('[DEBUG] employerPhone 치환:', value);
                                break;
                            case 'employeeAddress':
                                updatedHtml = updatedHtml.replace(/\[EMPLOYEE_ADDRESS\]/g, value || '');
                                console.log('[DEBUG] employeeAddress 치환:', value);
                                break;
                            case 'employeePhone':
                                updatedHtml = updatedHtml.replace(/\[EMPLOYEE_PHONE\]/g, value || '');
                                console.log('[DEBUG] employeePhone 치환:', value);
                                break;
                            default:
                                console.log('[DEBUG] 처리되지 않은 필드:', fieldName, '값:', value);
                        }
                    }
                });

                // 기존 스타일 태그 제거
                const existingStyleId = 'preset-styles';
                const existingStyle = document.getElementById(existingStyleId);
                if (existingStyle) {
                    existingStyle.remove();
                }

                // 새 스타일 적용 (미리보기 영역에만 적용)
                if (extractedStyles) {
                    const scopedStyles = extractedStyles.replace(/body\s*\{/g, '.preview-surface {')
                                                      .replace(/\.title/g, '.preview-surface .title')
                                                      .replace(/\.section/g, '.preview-surface .section')
                                                      .replace(/\.blank-line/g, '.preview-surface .blank-line')
                                                      .replace(/\.section-number/g, '.preview-surface .section-number')
                                                      .replace(/\.contract-intro/g, '.preview-surface .contract-intro')
                                                      .replace(/\.wage-section/g, '.preview-surface .wage-section')
                                                      .replace(/\.wage-item/g, '.preview-surface .wage-item')
                                                      .replace(/\.indent/g, '.preview-surface .indent')
                                                      .replace(/\.note/g, '.preview-surface .note')
                                                      .replace(/\.signature-section/g, '.preview-surface .signature-section');

                    const styleTag = document.createElement('style');
                    styleTag.id = existingStyleId;
                    styleTag.textContent = scopedStyles;
                    document.head.appendChild(styleTag);
                }

                console.log('[DEBUG] 최종 HTML 일부:', updatedHtml.substring(0, 500)); // 디버깅용
                previewEl.innerHTML = updatedHtml;
            }
        } else {
            previewEl.innerHTML = sections.map(sectionToHtml).join('');
        }
    }

    function sectionToHtml(section) {
        const safe = escapeHtml(section.content || '').replace(/\n/g, '<br>');
        const fallbacks = {
            HEADER: '머릿말을 입력하세요',
            DOTTED_BOX: '점선 박스 내용을 입력하세요',
            FOOTER: '꼬릿말을 입력하세요',
            PARAGRAPH: '본문 내용을 입력하세요'
        };
        if (section.type === 'CUSTOM') {
            const metadata = section.metadata || {};
            if (metadata.rawHtml) {
                // 표준 양식 HTML을 iframe으로 격리해서 렌더링
                const content = section.content || '';
                // style 태그 제거해서 CSS 영향 차단
                const cleanContent = content.replace(/<style[^>]*>[\s\S]*?<\/style>/gi, '')
                                          .replace(/<head[^>]*>[\s\S]*?<\/head>/gi, '')
                                          .replace(/<!DOCTYPE[^>]*>/gi, '')
                                          .replace(/<html[^>]*>/gi, '')
                                          .replace(/<\/html>/gi, '')
                                          .replace(/<body[^>]*>/gi, '')
                                          .replace(/<\/body>/gi, '');

                return
                    '<div class="alert alert-info mb-3" style="font-size: 0.9rem;">' +
                        '<strong>📄 표준 근로계약서</strong> - 좌측 폼을 작성하면 실제 값이 반영됩니다.' +
                    '</div>' +
                    '<div style="border: 1px solid #ddd; border-radius: 8px; padding: 1rem; background: #fafafa; font-size: 0.8rem; max-height: 300px; overflow-y: auto;">' +
                        cleanContent +
                    '</div>';
            }
            return '<section class="template-custom">' + (safe || fallbacks.PARAGRAPH) + '</section>';
        }
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


    sectionListEl.addEventListener('change', (event) => {
        const card = event.target.closest('.card');
        if (!card) return;
        const id = card.dataset.sectionId;
        const section = sections.find(s => s.sectionId === id);
        if (!section) return;
        if (event.target.dataset.field === 'type') {
            section.type = event.target.value;
            section.metadata = section.metadata || {};
            if (section.metadata.rawHtml && section.type !== 'CUSTOM') {
                delete section.metadata.rawHtml;
            }
        }
        activeTextareaId = id;
        renderSections();
        renderPreview();
    });

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

    if (presetSelect) {
        presetSelect.addEventListener('change', async (event) => {
            const presetId = event.target.value;
            console.log('[DEBUG] 프리셋 선택됨:', presetId);
            console.log('[DEBUG] presetId 타입:', typeof presetId);
            console.log('[DEBUG] presetId 길이:', presetId ? presetId.length : 'null');
            if (!presetId) {
                console.log('[DEBUG] 빈 프리셋 ID, 리턴');
                return;
            }
            try {
                const apiUrl = '/templates/presets/' + presetId;
                console.log('[DEBUG] 구성된 API URL:', apiUrl);
                const response = await fetch(apiUrl, {
                    headers: { 'Accept': 'application/json' }
                });
                console.log('[DEBUG] API 응답 상태:', response.status, response.ok);
                if (!response.ok) {
                    alert('표준 양식을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
                    return;
                }
                const preset = await response.json();
                console.log('[DEBUG] 프리셋 데이터:', preset);
                console.log('[DEBUG] 기존 sections:', sections);

                // 응답이 배열이면 프리셋 목록 API를 잘못 호출한 것
                if (Array.isArray(preset)) {
                    console.error('[ERROR] 잘못된 API 응답: 배열이 반환됨. 개별 프리셋이 아닌 목록 API가 호출됨');
                    alert('프리셋 로딩 중 오류가 발생했습니다.');
                    return;
                }

                // 안전하게 섹션 배열 추출
                const presetSections = preset.sections || [];
                console.log('[DEBUG] 프리셋 섹션들:', presetSections);

                if (presetSections.length === 0) {
                    console.warn('[WARN] 프리셋에 섹션이 없습니다');
                    alert('이 프리셋은 섹션 데이터가 없습니다.');
                    return;
                }

                // 백엔드 API에서 템플릿 HTML 가져오기
                let templateHtml;
                try {
                    const response = await fetch('/templates/presets/standard-employment-contract');
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    const presetData = await response.json();
                    console.log('[DEBUG] API 응답 데이터:', presetData);

                    // 프리셋 데이터에서 HTML 템플릿 추출
                    if (presetData.sections && presetData.sections.length > 0) {
                        templateHtml = presetData.sections[0].content;
                        console.log('[DEBUG] API에서 받은 템플릿 HTML:', templateHtml.substring(0, 200) + '...');
                    } else {
                        throw new Error('프리셋 데이터에 섹션이 없습니다.');
                    }
                } catch (error) {
                    console.error('[ERROR] 템플릿 로딩 실패:', error);
                    alert('템플릿을 불러오는 중 오류가 발생했습니다: ' + error.message);
                    return;
                }

                sections = [
                    // 백엔드에서 가져온 HTML 템플릿으로 섹션 생성
                    {
                        sectionId: 'standard-contract-template',
                        type: 'CUSTOM',
                        order: 0,
                        content: templateHtml,
                        metadata: { rawHtml: true }
                    }
                ];

                console.log('[DEBUG] 테스트 sections 생성:', sections);
                console.log('[DEBUG] 새 sections:', sections);
                activeTextareaId = null;
                console.log('[DEBUG] renderSections 호출 전');
                try {
                    renderSections();
                    console.log('[DEBUG] renderSections 호출 후');
                } catch (error) {
                    console.error('[ERROR] renderSections 실패:', error);
                    alert('렌더링 중 오류가 발생했습니다: ' + error.message);
                    return;
                }

                console.log('[DEBUG] renderPreview 호출 전');
                try {
                    renderPreview();
                    console.log('[DEBUG] renderPreview 호출 후');
                } catch (error) {
                    console.error('[ERROR] renderPreview 실패:', error);
                    alert('미리보기 렌더링 중 오류가 발생했습니다: ' + error.message);
                    return;
                }
                if (templateTitleInput && templateTitleInput.value.trim().length === 0 && preset.name) {
                    templateTitleInput.value = preset.name;
                }

                // 표준 양식일 때는 변수 패널 숨기기
                const variablePanel = document.getElementById('variablePanel');
                const hasPresetSections = sections.some(s => s.metadata?.rawHtml);
                if (hasPresetSections && variablePanel) {
                    variablePanel.style.display = 'none';
                    console.log('[DEBUG] 변수 패널 숨김');
                } else if (variablePanel) {
                    variablePanel.style.display = 'block';
                }

                document.getElementById('sectionsJson').value = JSON.stringify(sections);
                presetSelect.value = '';
                console.log('[DEBUG] 프리셋 로딩 완료');
            } catch (error) {
                console.error('[ERROR] 프리셋 로딩 실패:', error);
                alert('표준 양식을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
            }
        });
    }

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

    // 프리셋 폼 필드 이벤트 리스너 초기화
    function initPresetFormListeners() {
        const presetFormContainer = document.querySelector('.preset-form-fields');
        if (presetFormContainer) {
            // 이벤트 위임을 사용하여 모든 프리셋 폼 필드의 변경사항을 감지
            presetFormContainer.addEventListener('input', function(event) {
                const field = event.target;
                if (field.dataset.field) {
                    console.log('[DEBUG] 프리셋 필드 변경:', field.dataset.field, '값:', field.value);
                    // 디바운싱을 위한 타이머 설정
                    clearTimeout(field.updateTimer);
                    field.updateTimer = setTimeout(() => {
                        savePresetFormData();
                        renderPreview();
                    }, 300); // 300ms 후에 미리보기 업데이트
                }
            });

            // change 이벤트도 처리 (select, radio 등을 위해)
            presetFormContainer.addEventListener('change', function(event) {
                const field = event.target;
                if (field.dataset.field) {
                    console.log('[DEBUG] 프리셋 필드 변경(change):', field.dataset.field, '값:', field.value);
                    savePresetFormData();
                    renderPreview();
                }
            });

            console.log('[SUCCESS] 프리셋 폼 필드 이벤트 리스너 초기화 완료');
        }
    }

    loadInitialSections();
    initPreviewZoom();
    initFloatingPreview();
    initPresetFormListeners();
</script>


</body>
</html>
