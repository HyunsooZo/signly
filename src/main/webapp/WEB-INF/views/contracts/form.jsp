<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
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
    <link href="/css/contracts.css" rel="stylesheet">
    <style>
        .contract-builder-wrap {
            display: grid;
            grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
            gap: 1.5rem;
        }
        @media (max-width: 992px) {
            .contract-builder-wrap {
                grid-template-columns: minmax(0, 1fr);
            }
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
        .custom-variable-inline {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            background-color: rgba(255, 243, 205, 0.8);
            border: 1px dashed #f0ad4e;
            border-radius: 6px;
            padding: 2px 6px;
            margin: 0 2px;
        }
        .custom-variable-inline-label {
            font-size: 0.75rem;
            color: #b58105;
            font-weight: 600;
        }
        .custom-variable-inline-input {
            width: auto;
            min-width: 60px;
            border: none;
            background: transparent;
            border-bottom: 1px solid #f0ad4e;
            padding: 0 2px;
            font-size: 0.85rem;
        }
        .custom-variable-inline-input:focus {
            outline: none;
            border-bottom: 2px solid #f0ad4e;
            background-color: rgba(255, 243, 205, 0.4);
        }
    </style>
</head>
<body <c:if test="${not empty currentUserId}">data-current-user-id="${currentUserId}"</c:if>>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container">
            <a class="navbar-brand" href="/home">
                <i class="bi bi-file-earmark-text me-2"></i>Signly
            </a>
            <div class="navbar-nav ms-auto">
                <a class="nav-link" href="/home">대시보드</a>
                <a class="nav-link" href="/templates">템플릿</a>
                <a class="nav-link active" href="/contracts">계약서</a>
                <a class="nav-link" href="/profile/signature">서명 관리</a>
                <a class="nav-link" href="/logout">로그아웃</a>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <div class="row">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h2 class="mb-2">
                            <i class="bi bi-file-earmark-plus text-primary me-2"></i>
                            ${pageTitle}
                        </h2>
                        <p class="text-muted mb-0">계약서 정보와 당사자 정보를 입력하세요</p>
                    </div>
                    <a href="/contracts" class="btn btn-outline-secondary">
                        <i class="bi bi-arrow-left me-2"></i>목록으로
                    </a>
                </div>

                <!-- 알림 메시지 -->
                <c:if test="${not empty successMessage}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <i class="bi bi-check-circle me-2"></i>${successMessage}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="bi bi-exclamation-triangle me-2"></i>${errorMessage}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>
            </div>
        </div>

        <form method="post" action="${not empty contractId ? '/contracts/'.concat(contractId) : '/contracts'}" class="contract-form">
            <c:if test="${not empty _csrf}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            </c:if>
            <c:if test="${not empty selectedPreset}">
                <input type="hidden" name="selectedPreset" value="${selectedPreset}" />
            </c:if>

            <div id="normalLayout">
                <div class="row">
                    <!-- 계약서 기본 정보 -->
                    <div class="col-lg-8" id="mainFormCol">
                    <div class="card mb-4">
                        <div class="card-header">
                            <h5 class="card-title mb-0">
                                <i class="bi bi-file-earmark-text me-2"></i>계약서 기본 정보
                            </h5>
                        </div>
                        <div class="card-body">
                            <!-- 템플릿 선택 (새 계약서인 경우만) -->
                            <c:if test="${empty contractId}">
                                <div class="mb-3">
                                    <label for="templateId" class="form-label">템플릿 선택</label>
                                    <select class="form-select" id="templateId" name="templateId" onchange="loadTemplate()">
                                        <option value="">직접 작성</option>
                                        <c:forEach var="template" items="${templates}">
                                            <option value="${template.templateId}"
                                                    data-title="${template.title}"
                                                    data-content="${fn:escapeXml(template.renderedHtml)}"
                                                    <c:if test="${contract.templateId == template.templateId}">selected</c:if>>
                                                ${template.title}
                                            </option>
                                        </c:forEach>
                                    </select>
                                    <div class="form-text">기존 템플릿을 선택하거나 직접 작성하세요.</div>
                                </div>
                            </c:if>

                            <!-- 숨겨진 프리셋 select (selectedPreset으로 넘어온 경우를 위해) -->
                            <select id="presetSelect" style="display: none;">
                                <option value="">표준 양식을 선택하세요</option>
                                <c:forEach var="preset" items="${presets}">
                                    <option value="${preset.id}" data-name="${preset.name}">
                                        ${preset.name} - ${preset.description}
                                    </option>
                                </c:forEach>
                            </select>

                            <div class="mb-3">
                                <label for="title" class="form-label">계약서 제목 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control form-control-lg" id="title" name="title"
                                       value="${contract.title}" required maxlength="200"
                                       placeholder="계약서 제목을 입력하세요">
                            </div>

                            <div class="mb-3" id="contentSection">
                                <label for="content" class="form-label">계약서 내용 <span class="text-danger">*</span></label>
                                <textarea class="form-control content-editor" id="content" name="content"
                                          rows="15" required placeholder="계약서 내용을 입력하세요...">${contract.content}</textarea>
                                <div class="form-text">계약서의 전체 내용을 입력하세요. 변수를 사용하여 동적 값을 설정할 수 있습니다.</div>

                                <div class="mt-3" id="customVariablesContainer" style="display: none;">
                                    <label class="form-label d-flex align-items-center gap-2">
                                        <i class="bi bi-sliders2-vertical"></i> 변수 값 입력
                                    </label>
                                    <div class="row g-2" id="customVariableFields"></div>
                                    <div class="form-text">`{변수명}` 형식의 변수가 감지되면 해당 값을 아래에서 입력할 수 있습니다.</div>
                                </div>

                                <div class="mt-3" id="customContentPreviewWrapper" style="display: none;">
                                    <div class="card">
                                        <div class="card-header">
                                            <h5 class="card-title mb-0">
                                                <i class="bi bi-eye"></i> 실시간 미리보기
                                            </h5>
                                        </div>
                                        <div class="card-body" id="customContentPreview" style="min-height: 200px; background-color: #f8f9fa; overflow-x: auto;"></div>
                                    </div>
                                </div>

                                <!-- 프리셋 폼 필드 컨테이너 (동적으로 생성됨) -->
                                <div id="presetFormFields" style="display: none;"></div>
                            </div>

                            <div class="mb-3">
                                <label for="expiresAt" class="form-label">만료일</label>
                                <input type="datetime-local" class="form-control" id="expiresAt" name="expiresAt"
                                       value="${contract.expiresAtInputValue}">
                                <div class="form-text">계약서의 서명 만료일을 설정하세요 (선택사항).</div>
                            </div>
                        </div>
                    </div>

                </div>

                <!-- 당사자 정보 -->
                <div class="col-lg-4" id="partyInfoCol">
                    <div class="card mb-4" id="partyInfoCard">
                        <div class="card-header">
                            <h5 class="card-title mb-0">
                                <i class="bi bi-people me-2"></i>당사자 정보
                            </h5>
                        </div>
                        <div class="card-body">
                            <!-- 갑 (첫 번째 당사자) -->
                            <h6 class="text-primary mb-3">갑 (첫 번째 당사자)</h6>
                            <div class="mb-3">
                                <label for="firstPartyName" class="form-label">이름 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="firstPartyName" name="firstPartyName"
                                       value="${contract.firstPartyName}" required maxlength="100"
                                       placeholder="성명을 입력하세요">
                            </div>
                            <div class="mb-3">
                                <label for="firstPartyEmail" class="form-label">이메일 <span class="text-danger">*</span></label>
                                <input type="email" class="form-control" id="firstPartyEmail" name="firstPartyEmail"
                                       value="${contract.firstPartyEmail}" required maxlength="200"
                                       placeholder="example@domain.com">
                            </div>
                            <div class="mb-4">
                                <label for="firstPartyAddress" class="form-label">회사/조직명</label>
                                <textarea class="form-control" id="firstPartyAddress" name="firstPartyAddress"
                                          rows="3" maxlength="500" placeholder="회사 또는 조직명을 입력하세요">${contract.firstPartyAddress}</textarea>
                            </div>

                            <!-- 을 (두 번째 당사자) -->
                            <h6 class="text-success mb-3">을 (두 번째 당사자)</h6>
                            <div class="mb-3">
                                <label for="secondPartyName" class="form-label">이름 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="secondPartyName" name="secondPartyName"
                                       value="${contract.secondPartyName}" required maxlength="100"
                                       placeholder="성명을 입력하세요">
                            </div>
                            <div class="mb-3">
                                <label for="secondPartyEmail" class="form-label">이메일 <span class="text-danger">*</span></label>
                                <input type="email" class="form-control" id="secondPartyEmail" name="secondPartyEmail"
                                       value="${contract.secondPartyEmail}" required maxlength="200"
                                       placeholder="example@domain.com">
                            </div>
                            <div class="mb-4">
                                <label for="secondPartyAddress" class="form-label">회사/조직명</label>
                                <textarea class="form-control" id="secondPartyAddress" name="secondPartyAddress"
                                          rows="3" maxlength="500" placeholder="회사 또는 조직명을 입력하세요">${contract.secondPartyAddress}</textarea>
                            </div>
                        </div>
                    </div>
                </div>

                </div>
            </div>

            <!-- 액션 버튼 (별도 row) -->
            <div class="row mt-4" id="actionButtonRow">
                <div class="col-12">
                    <div class="d-flex justify-content-between">
                        <a href="/contracts" class="btn btn-secondary">
                            <i class="bi bi-x me-2"></i>취소
                        </a>
                        <div>
                            <button type="button" class="btn btn-outline-primary me-2" onclick="previewContract()">
                                <i class="bi bi-eye me-2"></i>미리보기
                            </button>
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-check-circle me-2"></i>
                                ${empty contractId ? '계약서 생성' : '수정 완료'}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <!-- 미리보기 모달 -->
    <div class="modal fade" id="previewModal" tabindex="-1">
        <div class="modal-dialog modal-xl">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="bi bi-eye me-2"></i>계약서 미리보기
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="alert alert-info">
                        <i class="bi bi-info-circle me-2"></i>
                        아래는 현재 입력된 정보로 생성될 계약서의 미리보기입니다.
                    </div>
                    <div class="contract-preview border rounded p-4" style="background-color: #f8f9fa; min-height: 400px; white-space: pre-wrap; font-family: 'Malgun Gothic', sans-serif; line-height: 1.4; font-size: 13px;" id="previewContent">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const contractContentTextarea = document.getElementById('content');
        const currentUserId = document.body?.dataset?.currentUserId || '';
        const ownerInfo = readOwnerInfo();
        const ownerSignatureInfo = readOwnerSignature();
        let ownerSignatureDataUrl = ownerSignatureInfo.dataUrl || '';
        let ownerSignatureUpdatedAt = ownerSignatureInfo.updatedAt || '';

        const PLACEHOLDER_REGEX = /\{([^{}]+)\}|\[([^\[\]]+)\]/g;
        const IGNORED_PLACEHOLDERS = new Set(['EMPLOYER_SIGNATURE_IMAGE']);

        function forEachPlaceholder(text, callback) {
            if (!text) {
                return;
            }
            PLACEHOLDER_REGEX.lastIndex = 0;
            let match;
            while ((match = PLACEHOLDER_REGEX.exec(text)) !== null) {
                const name = (match[1] ? match[1].trim() : (match[2] ? match[2].trim() : ''));
                if (!name) {
                    continue;
                }
                callback(name, match[0], match.index);
            }
        }
        const customVariableContainer = document.getElementById('customVariablesContainer');
        const customVariableFieldsWrapper = document.getElementById('customVariableFields');
        const customContentPreviewWrapper = document.getElementById('customContentPreviewWrapper');
        const customContentPreview = document.getElementById('customContentPreview');
        const customVariableValues = {};
        let customVariables = [];

        applyOwnerInfoToNormalForm();
        initializeOwnerSignature();
        detectCustomVariables();

        if (contractContentTextarea) {
            contractContentTextarea.addEventListener('input', () => {
                detectCustomVariables();
                updateDirectPreview();
            });
        }

        function loadTemplate() {
            const select = document.getElementById('templateId');
            const selectedOption = select.options[select.selectedIndex];

            if (selectedOption.value) {
                document.getElementById('title').value = selectedOption.dataset.title || '';
                const raw = selectedOption.getAttribute('data-content') || '';
                const decoded = decodeHtmlEntities(raw);
                document.getElementById('content').value = decoded;
                detectCustomVariables();
                updateDirectPreview();
            }
        }

        // 프리셋 선택 이벤트 핸들러
        const presetSelect = document.getElementById('presetSelect');
        if (presetSelect && contractContentTextarea) {
            presetSelect.addEventListener('change', async (event) => {
                const presetId = event.target.value;
                if (!presetId) {
                    return;
                }

                try {
                    const response = await fetch('/templates/presets/' + presetId, {
                        headers: { 'Accept': 'application/json' }
                    });

                    if (!response.ok) {
                        alert('표준 양식을 불러오지 못했습니다.');
                        return;
                    }

                    const preset = await response.json();

                    const sectionHtml = Array.isArray(preset.sections)
                        ? preset.sections.map(section => section.content || '').join('\n')
                        : '';
                    const rendered = decodeHtmlEntities(preset.renderedHtml || sectionHtml || '');

                    contractContentTextarea.style.display = 'none';
                    contractContentTextarea.required = false;
                    contractContentTextarea.value = rendered;

                    const titleInput = document.getElementById('title');
                    if (titleInput && (!titleInput.value || !titleInput.value.trim()) && preset.name) {
                        titleInput.value = preset.name;
                    }

                    detectCustomVariables();
                    updateDirectPreview();
                } catch (error) {
                    console.error('프리셋 로딩 실패:', error);
                    alert('표준 양식을 불러오지 못했습니다.');
                } finally {
                    presetSelect.value = '';
                }
            });
        }

        function renderCustomVariableInputs() {
            if (!customVariableContainer || !customVariableFieldsWrapper) {
                return;
            }

            customVariableFieldsWrapper.innerHTML = '';

            if (!customVariables.length) {
                customVariableContainer.style.display = 'none';
                updateDirectPreview();
                return;
            }

            customVariableContainer.style.display = '';

            customVariables.forEach(variableName => {
                if (!(variableName in customVariableValues)) {
                    customVariableValues[variableName] = suggestDefaultValue(variableName) || '';
                }

                const sanitizedId = 'variable-' + variableName.replace(/[^a-zA-Z0-9_-]+/g, '-');
                const wrapper = document.createElement('div');
                wrapper.className = 'col-md-6';

                const label = document.createElement('label');
                label.className = 'form-label';
                label.setAttribute('for', sanitizedId);
                label.textContent = variableName;

                const input = document.createElement('input');
                input.type = 'text';
                input.className = 'form-control';
                input.id = sanitizedId;
                input.setAttribute('data-variable-field', variableName);
                input.value = customVariableValues[variableName] || '';
                input.placeholder = variableName;

                input.addEventListener('input', (event) => {
                    customVariableValues[variableName] = event.target.value;
                    updateInlineVariableDisplays(variableName, event.target.value);
                });

                wrapper.appendChild(label);
                wrapper.appendChild(input);
                customVariableFieldsWrapper.appendChild(wrapper);
            });

            updateDirectPreview();
        }

        function suggestDefaultValue(variableName) {
            if (!ownerInfo) {
                return '';
            }
            const upper = variableName.toUpperCase();
            if (upper === 'EMPLOYER' || upper === 'EMPLOYER_NAME' || upper === 'OWNER_NAME' || upper === 'BUSINESS_OWNER') {
                return ownerInfo.name || '';
            }
            if (upper === 'EMPLOYER_EMAIL' || upper === 'OWNER_EMAIL') {
                return ownerInfo.email || '';
            }
            if (upper === 'COMPANY' || upper === 'COMPANY_NAME' || upper === 'ORGANIZATION') {
                return ownerInfo.companyName || '';
            }
            return '';
        }

        function readOwnerSignature() {
            try {
                const raw = localStorage.getItem('signly_owner_signature');
                if (!raw) {
                    return {};
                }
                const parsed = JSON.parse(raw);
                if (!parsed || typeof parsed !== 'object') {
                    return {};
                }
                return {
                    dataUrl: typeof parsed.dataUrl === 'string' ? parsed.dataUrl : '',
                    updatedAt: typeof parsed.updatedAt === 'string' ? parsed.updatedAt : ''
                };
            } catch (error) {
                console.warn('[WARN] 사업주 서명 데이터를 불러올 수 없습니다:', error);
                return {};
            }
        }

        function applyOwnerSignature(html) {
            if (!html || typeof html !== 'string') {
                return html;
            }
            if (!html.includes('[EMPLOYER_SIGNATURE_IMAGE]')) {
                return html;
            }

            const signatureMarkup = ownerSignatureDataUrl
                ? '<img src="' + ownerSignatureDataUrl + '" alt="사업주 서명" class="signature-stamp-image-element">'
                : '';

            return html.replace(/\[EMPLOYER_SIGNATURE_IMAGE\]/g, signatureMarkup);
        }

        function applyCustomVariablesToContent(rawContent) {
            if (!rawContent || typeof rawContent !== 'string') {
                return rawContent;
            }

            return rawContent.replace(PLACEHOLDER_REGEX, (match, curly, bracket) => {
                const variableName = (curly ? curly.trim() : (bracket ? bracket.trim() : ''));
                if (!variableName || IGNORED_PLACEHOLDERS.has(variableName)) {
                    return match;
                }
                return customVariableValues[variableName] || '';
            });
        }

        function escapeRegExp(text) {
            return text.replace(/[.*+?^$()|[\]{}\\]/g, '\\$&');
        }

        function decodeHtmlEntities(value) {
            if (!value) {
                return '';
            }
            const textarea = document.createElement('textarea');
            textarea.innerHTML = value;
            return textarea.value;
        }

        function sanitizeHtml(html) {
            if (!html) {
                return '';
            }
            const template = document.createElement('template');
            template.innerHTML = html;
            template.content.querySelectorAll('script').forEach(node => node.remove());
            return template.innerHTML;
        }

        function updateDirectPreview() {
            if (!customContentPreviewWrapper || !customContentPreview) {
                return;
            }

            if (!contractContentTextarea) {
                customContentPreviewWrapper.style.display = 'none';
                return;
            }

            const raw = contractContentTextarea.value || '';
            if (!raw.trim()) {
                customContentPreviewWrapper.style.display = 'none';
                customContentPreview.innerHTML = '';
                return;
            }

            customContentPreviewWrapper.style.display = '';
            const withVariables = applyCustomVariablesToContent(raw);
            const withSignature = applyOwnerSignature(withVariables);
            const sanitized = sanitizeHtml(withSignature);
            const template = document.createElement('template');
            template.innerHTML = sanitized;
            transformPlaceholdersForInlineEditing(template.content);
            customContentPreview.innerHTML = '';
            customContentPreview.appendChild(template.content);
        }

        async function initializeOwnerSignature() {
            if (ownerSignatureDataUrl) {
                return;
            }

            if (!currentUserId) {
                return;
            }

            try {
                const response = await fetch('/api/first-party-signature/me', {
                    method: 'GET',
                    headers: {
                        'Accept': 'application/json',
                        'X-User-Id': currentUserId
                    }
                });

                if (response.status === 204) {
                    localStorage.removeItem('signly_owner_signature');
                    return;
                }

                if (!response.ok) {
                    console.warn('[WARN] 사업주 서명 정보를 불러오지 못했습니다. 상태:', response.status);
                    return;
                }

                const payload = await response.json();
                if (payload && typeof payload === 'object' && payload.dataUrl) {
                    ownerSignatureDataUrl = payload.dataUrl;
                    ownerSignatureUpdatedAt = payload.updatedAt || '';
                    persistOwnerSignature(ownerSignatureDataUrl, ownerSignatureUpdatedAt);
                    reapplyOwnerSignature();
                }
            } catch (error) {
                console.warn('[WARN] 사업주 서명 정보를 가져오는 중 오류 발생:', error);
            }
        }

        function persistOwnerSignature(dataUrl, updatedAt) {
            if (!dataUrl) {
                localStorage.removeItem('signly_owner_signature');
                return;
            }

            try {
                const payload = {
                    dataUrl: dataUrl,
                    updatedAt: updatedAt || new Date().toISOString()
                };
                localStorage.setItem('signly_owner_signature', JSON.stringify(payload));
            } catch (error) {
                console.warn('[WARN] 사업주 서명 정보를 localStorage에 저장할 수 없습니다:', error);
            }
        }

        function reapplyOwnerSignature() {
            if (contractContentTextarea && contractContentTextarea.value) {
                contractContentTextarea.value = applyOwnerSignature(contractContentTextarea.value);
                updateInlineVariableDisplays(null, null);
                updateDirectPreview();
            }
        }

        function transformPlaceholdersForInlineEditing(root) {
            if (!root) {
                return;
            }

            const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null);
            const textNodes = [];
            while (walker.nextNode()) {
                const node = walker.currentNode;
                if (node.nodeValue && (node.nodeValue.includes('{') || node.nodeValue.includes('['))) {
                    textNodes.push(node);
                }
            }

            textNodes.forEach(node => replaceTextNodeWithInputs(node));
        }

        function replaceTextNodeWithInputs(textNode) {
            const text = textNode.nodeValue;
            PLACEHOLDER_REGEX.lastIndex = 0;
            if (!PLACEHOLDER_REGEX.test(text)) {
                return;
            }
            PLACEHOLDER_REGEX.lastIndex = 0;
            const parent = textNode.parentNode;
            if (!parent) {
                return;
            }

            const fragment = document.createDocumentFragment();
            let lastIndex = 0;
            let match;
            while ((match = PLACEHOLDER_REGEX.exec(text)) !== null) {
                const fullMatch = match[0];
                const varName = match[1] ? match[1].trim() : (match[2] ? match[2].trim() : '');
                if (match.index > lastIndex) {
                    fragment.appendChild(document.createTextNode(text.slice(lastIndex, match.index)));
                }
                if (varName && !IGNORED_PLACEHOLDERS.has(varName)) {
                    fragment.appendChild(createInlineVariableElement(varName));
                } else {
                    fragment.appendChild(document.createTextNode(fullMatch));
                }
                lastIndex = PLACEHOLDER_REGEX.lastIndex;
            }

            if (lastIndex < text.length) {
                fragment.appendChild(document.createTextNode(text.slice(lastIndex)));
            }

            parent.replaceChild(fragment, textNode);
        }

        function createInlineVariableElement(variableName) {
            const wrapper = document.createElement('span');
            wrapper.className = 'custom-variable-inline';

            const label = document.createElement('span');
            label.className = 'custom-variable-inline-label';
            label.textContent = `{${variableName}}`;

            const input = document.createElement('input');
            input.type = 'text';
            input.className = 'custom-variable-inline-input';
            input.setAttribute('data-variable-name', variableName);
            if (!(variableName in customVariableValues)) {
                customVariableValues[variableName] = suggestDefaultValue(variableName) || '';
            }
            input.value = customVariableValues[variableName] || '';

            input.addEventListener('input', (event) => {
                const newValue = event.target.value;
                customVariableValues[variableName] = newValue;
                updateVariablePanelField(variableName, newValue, event.target);
                updateInlineVariableDisplays(variableName, newValue, event.target);
            });

            wrapper.appendChild(label);
            wrapper.appendChild(input);
            return wrapper;
        }

        function updateInlineVariableDisplays(variableName, value, sourceElement) {
            if (!variableName) {
                return;
            }
            const selector = `.custom-variable-inline-input[data-variable-name="${variableName}"]`;
            const inputs = customContentPreview?.querySelectorAll(selector) || [];
            inputs.forEach(input => {
                if (input === sourceElement) {
                    return;
                }
                if (input.value !== value) {
                    input.value = value || '';
                }
            });
        }

        function updateVariablePanelField(variableName, value, sourceElement) {
            if (!customVariableFieldsWrapper) {
                return;
            }
            const selector = `[data-variable-field="${variableName}"]`;
            const field = customVariableFieldsWrapper.querySelector(selector);
            if (field && field !== sourceElement && field.value !== value) {
                field.value = value || '';
            }
        }

        function insertVariable(variable) {
            const textarea = document.getElementById('content');
            const start = textarea.selectionStart;
            const end = textarea.selectionEnd;
            const text = textarea.value;

            textarea.value = text.substring(0, start) + variable + text.substring(end);
            textarea.selectionStart = textarea.selectionEnd = start + variable.length;
            textarea.focus();
        }

        function previewContract() {
            if (!contractContentTextarea) {
                return;
            }

            const raw = contractContentTextarea.value || '';
            const withVariables = applyCustomVariablesToContent(raw);
            const withSignature = applyOwnerSignature(withVariables);
            const sanitized = sanitizeHtml(withSignature);

            const previewContent = document.getElementById('previewContent');
            if (previewContent) {
                previewContent.innerHTML = sanitized;
            }
            new bootstrap.Modal(document.getElementById('previewModal')).show();
        }

        // 폼 유효성 검사
        (function() {
            'use strict';
            window.addEventListener('load', function() {
                var forms = document.getElementsByClassName('contract-form');
                var validation = Array.prototype.filter.call(forms, function(form) {
                    form.addEventListener('submit', function(event) {
                        if (contractContentTextarea) {
                            const resolvedContent = applyOwnerSignature(applyCustomVariablesToContent(contractContentTextarea.value || ''));
                            contractContentTextarea.value = resolvedContent;
                        }

                        const firstEmail = document.getElementById('firstPartyEmail')?.value;
                        const secondEmail = document.getElementById('secondPartyEmail')?.value;

                        if (firstEmail && secondEmail && firstEmail === secondEmail) {
                            event.preventDefault();
                            event.stopPropagation();
                            alert('갑과 을의 이메일 주소는 달라야 합니다.');
                            return false;
                        }

                        if (form.checkValidity() === false) {
                            event.preventDefault();
                            event.stopPropagation();
                        }
                        form.classList.add('was-validated');
                    }, false);
                });
            }, false);
        })();

        // 스티키 미리보기를 근로자 정보 카드 하단에 맞춰 멈추도록 처리
        

        // 스크롤 이벤트 리스너 등록
                
        // 페이지 로드 시 selectedPreset이 있으면 자동으로 로드
        <c:if test="${not empty selectedPreset}">
        (function() {
            console.log('[INFO] selectedPreset 감지:', '${selectedPreset}');
            const isEditMode = ${not empty contractId};
            console.log('[INFO] 수정 모드:', isEditMode);

            // DOM이 로드되면 즉시 실행
            document.addEventListener('DOMContentLoaded', function() {
                console.log('[INFO] DOMContentLoaded - 프리셋 로드 시작');

                // normalLayout의 모든 필드를 미리 비활성화
                const normalLayout = document.getElementById('normalLayout');
                if (normalLayout) {
                    const allInputs = normalLayout.querySelectorAll('input, select, textarea');
                    console.log('[INFO] normalLayout 필드 개수:', allInputs.length);
                    allInputs.forEach(field => {
                        if (field.id !== 'content' && field.id !== 'title') {
                            field.required = false;
                            field.disabled = true;
                            console.log('[INFO] 필드 비활성화:', field.id || field.name);
                        }
                    });
                }

                // 프리셋 로드
                const presetSelect = document.getElementById('presetSelect');
                if (presetSelect) {
                    console.log('[INFO] presetSelect 찾음, 값 설정 중...');
                    presetSelect.value = '${selectedPreset}';
                    const event = new Event('change');
                    presetSelect.dispatchEvent(event);

                    // 수정 모드인 경우 기존 값들을 복원
                    if (isEditMode) {
                        console.log('[INFO] 수정 모드 - 기존 값 복원 대기');
                        // presetSelect change 이벤트 처리 후 값 복원을 위해 약간 지연
                        setTimeout(function() {
                            const secondPartyEmailInput = document.getElementById('presetSecondPartyEmail');
                            if (secondPartyEmailInput) {
                                const existingSecondEmail = '${contract.secondPartyEmail}';
                                if (existingSecondEmail) {
                                    secondPartyEmailInput.value = existingSecondEmail;
                                    console.log('[INFO] 근로자 이메일 복원:', existingSecondEmail);
                                }
                            }
                        }, 100);
                    }
                } else {
                    console.error('[ERROR] presetSelect를 찾을 수 없음!');
                }
            });
        })();
        </c:if>

        // 폼 제출 직전 상태 확인
        document.addEventListener('DOMContentLoaded', function() {
            const form = document.querySelector('.contract-form');
            if (form) {
                form.addEventListener('submit', function(e) {
                    console.log('[SUBMIT] 폼 제출 시작');

                    // 모든 required 필드 확인
                    const requiredFields = document.querySelectorAll('[required]');
                    console.log('[SUBMIT] required 필드 개수:', requiredFields.length);
                    requiredFields.forEach(field => {
                        console.log('[SUBMIT] required 필드:', field.id || field.name, 'value:', field.value, 'disabled:', field.disabled);
                    });
                }, true); // capture phase에서 먼저 실행
            }
        });
    </script>
</body>
</html>
