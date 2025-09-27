<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
            grid-template-columns: minmax(0, 1fr) 360px;
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
        .preview-surface {
            border: 1px solid #dfe3eb;
            border-radius: 12px;
            background: #fff;
            padding: 1.5rem;
            max-height: 70vh;
            overflow: auto;
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
                <input type="hidden" name="sectionsJson" id="sectionsJson" value="${template.sectionsJson}">
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
                        <!-- 섹션 카드 렌더링 영역 -->
                    </div>
                </div>

                <div class="card mb-4">
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
            <div class="card mb-3">
                <div class="card-header">
                    <h5 class="card-title mb-0">
                        <i class="bi bi-display me-2"></i>실시간 미리보기
                    </h5>
                </div>
                <div class="card-body">
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

<div id="initialSections" data-sections='${template.sectionsJson}' hidden></div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const sectionTypes = {
        HEADER: { label: '머릿말', css: 'template-header', icon: 'bi bi-layout-text-sidebar-reverse' },
        PARAGRAPH: { label: '본문', css: 'template-paragraph', icon: 'bi bi-text-paragraph' },
        DOTTED_BOX: { label: '점선 박스', css: 'template-dotted', icon: 'bi bi-bounding-box' },
        FOOTER: { label: '꼬릿말', css: 'template-footer', icon: 'bi bi-pen' }
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
            content: ''
        };
    }

    function loadInitialSections() {
        try {
            const data = document.getElementById('initialSections').dataset.sections || '[]';
            const parsed = JSON.parse(data);
            sections = Array.isArray(parsed) ? parsed.map((s, idx) => ({
                sectionId: s.sectionId || (s.id || 'sec-' + idx),
                type: s.type || 'PARAGRAPH',
                order: idx,
                content: s.content || ''
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
            card.className = 'card mb-3 section-card ' + (section.type === 'DOTTED_BOX' ? 'dotted' : section.type === 'FOOTER' ? 'footer' : '');
            card.dataset.sectionId = section.sectionId;
            card.innerHTML = `
                <div class="card-header d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center gap-2">
                        <span class="badge bg-primary-subtle text-primary">
                            <i class="${sectionTypes[section.type].icon}"></i>
                        </span>
                        <strong>${sectionTypes[section.type].label}</strong>
                    </div>
                    <div class="section-actions btn-group btn-group-sm">
                        <button type="button" class="btn btn-outline-secondary" data-action="moveUp" ${index === 0 ? 'disabled' : ''}><i class="bi bi-arrow-up"></i></button>
                        <button type="button" class="btn btn-outline-secondary" data-action="moveDown" ${index === sections.length - 1 ? 'disabled' : ''}><i class="bi bi-arrow-down"></i></button>
                        <button type="button" class="btn btn-outline-danger" data-action="remove"><i class="bi bi-x-lg"></i></button>
                    </div>
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <label class="form-label">섹션 종류</label>
                        <select class="form-select form-select-sm" data-field="type">
                            ${Object.keys(sectionTypes).map(key => `<option value="${key}" ${key === section.type ? 'selected' : ''}>${sectionTypes[key].label}</option>`).join('')}
                        </select>
                    </div>
                    <div>
                        <label class="form-label">내용</label>
                        <textarea class="form-control" rows="4" data-field="content" data-section="${section.sectionId}" placeholder="내용을 입력하세요.">${section.content || ''}</textarea>
                    </div>
                </div>
            `;
            sectionListEl.appendChild(card);
        });
        if (sections.length === 0) {
            sectionListEl.innerHTML = '<div class="text-center text-muted py-5">섹션을 추가해 계약서를 구성하세요.</div>';
        }
    }

    function renderPreview() {
        if (!sections.length) {
            previewEl.innerHTML = '<div class="text-muted text-center py-5"><i class="bi bi-eye display-6 d-block mb-3"></i>섹션을 추가하면 미리보기가 표시됩니다.</div>';
            return;
        }
        previewEl.innerHTML = sections
            .map(sectionToHtml)
            .join('');
    }

    function sectionToHtml(section) {
        const safe = escapeHtml(section.content || '').replace(/\n/g, '<br>');
        switch (section.type) {
            case 'HEADER':
                return `<section class="template-header"><h2 class="mb-0">${safe || '머릿말을 입력하세요'}</h2></section>`;
            case 'DOTTED_BOX':
                return `<section class="template-dotted"><div>${safe || '점선 박스 내용을 입력하세요'}</div></section>`;
            case 'FOOTER':
                return `<section class="template-footer">${safe || '꼬릿말을 입력하세요'}</section>`;
            default:
                return `<section class="template-paragraph"><p>${safe || '본문 내용을 입력하세요'}</p></section>`;
        }
    }

    function escapeHtml(text) {
        const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
        return String(text || '').replace(/[&<>"']/g, m => map[m]);
    }

    sectionListEl.addEventListener('change', (event) => {
        const card = event.target.closest('.card');
        if (!card) return;
        const id = card.dataset.sectionId;
        const section = sections.find(s => s.sectionId === id);
        if (!section) return;
        if (event.target.dataset.field === 'type') {
            section.type = event.target.value;
        }
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
        const action = event.target.closest('button')?.dataset.action;
        if (!action) return;
        const card = event.target.closest('.card');
        if (!card) return;
        const id = card.dataset.sectionId;
        const index = sections.findIndex(s => s.sectionId === id);
        if (index === -1) return;

        if (action === 'remove') {
            sections.splice(index, 1);
        } else if (action === 'moveUp' && index > 0) {
            [sections[index - 1], sections[index]] = [sections[index], sections[index - 1]];
        } else if (action === 'moveDown' && index < sections.length - 1) {
            [sections[index + 1], sections[index]] = [sections[index], sections[index + 1]];
        }
        renderSections();
        renderPreview();
    });

    document.querySelectorAll('[data-add]').forEach(button => {
        button.addEventListener('click', () => {
            sections.push(newSection(button.dataset.add));
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
        const textarea = sectionListEl.querySelector(`textarea[data-section="${activeTextareaId}"]`);
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
            content: section.content
        }));
        document.getElementById('sectionsJson').value = JSON.stringify(payload);
    });

    document.getElementById('previewButton').addEventListener('click', () => {
        const modalBody = document.getElementById('modalPreview');
        modalBody.innerHTML = sections.length ? sections.map(sectionToHtml).join('') : '<div class="text-muted text-center py-5">섹션을 추가하면 미리보기가 표시됩니다.</div>';
        const modal = new bootstrap.Modal(document.getElementById('previewModal'));
        modal.show();
    });

    loadInitialSections();
</script>
</body>
</html>
