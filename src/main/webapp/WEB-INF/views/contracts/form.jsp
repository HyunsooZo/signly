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

            <!-- 프리셋 레이아웃 -->
            <div id="presetLayout" style="display: none;">
                <div class="row">
                    <div class="col-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <h5 class="card-title mb-0">
                                    <i class="bi bi-file-earmark-text me-2"></i><span id="presetLayoutTitle">표준 근로계약서</span>
                                </h5>
                            </div>
                            <div class="card-body" style="background-color: #fff; padding: 3rem;">
                                <!-- HTML 렌더링 영역 (변수는 입력 필드로 교체됨) -->
                                <div id="presetHtmlContainer"></div>
                            </div>
                        </div>

                        <!-- 근로자 정보 카드 -->
                        <div class="card mb-4">
                            <div class="card-header">
                                <h5 class="card-title mb-0">
                                    <i class="bi bi-people me-2"></i>근로자 정보
                                </h5>
                            </div>
                            <div class="card-body">
                                <div class="mb-3">
                                    <label for="presetSecondPartyEmail" class="form-label">근로자 이메일 <span class="text-danger">*</span></label>
                                    <input type="email" class="form-control" id="presetSecondPartyEmail" name="secondPartyEmail" required maxlength="200" placeholder="example@domain.com">
                                </div>
                            </div>
                        </div>

                        <!-- Hidden fields -->
                        <textarea id="presetContentHidden" name="content" style="display: none;" required></textarea>
                        <input type="hidden" id="presetTitleHidden" name="title" required>
                        <input type="hidden" id="presetFirstPartyName" name="firstPartyName" required>
                        <input type="hidden" id="presetFirstPartyEmail" name="firstPartyEmail" required>
                        <input type="hidden" id="presetFirstPartyAddress" name="firstPartyAddress">
                        <input type="hidden" id="presetSecondPartyName" name="secondPartyName" required>
                    </div>
                </div>
            </div>

            <!-- 일반 레이아웃 -->
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

        function readOwnerInfo() {
            try {
                const raw = localStorage.getItem('signly_user_info');
                if (!raw) {
                    return null;
                }
                const parsed = JSON.parse(raw);
                if (!parsed || typeof parsed !== 'object') {
                    return null;
                }
                return {
                    name: parsed.name || '',
                    email: parsed.email || '',
                    userId: parsed.userId || '',
                    companyName: parsed.companyName || ''
                };
            } catch (error) {
                console.warn('[WARN] 사용자 정보를 불러올 수 없습니다:', error);
                return null;
            }
        }

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

        function applyOwnerInfoToNormalForm() {
            if (!ownerInfo) {
                return;
            }
            // 일반 폼에 사업주 정보 자동 입력
            const firstPartyNameField = document.getElementById('firstPartyName');
            const firstPartyEmailField = document.getElementById('firstPartyEmail');
            const firstPartyAddressField = document.getElementById('firstPartyAddress');

            if (firstPartyNameField && !firstPartyNameField.value && ownerInfo.name) {
                firstPartyNameField.value = ownerInfo.name;
            }
            if (firstPartyEmailField && !firstPartyEmailField.value && ownerInfo.email) {
                firstPartyEmailField.value = ownerInfo.email;
            }
            if (firstPartyAddressField && !firstPartyAddressField.value && ownerInfo.companyName) {
                firstPartyAddressField.value = ownerInfo.companyName;
            }
        }

        function detectCustomVariables() {
            if (!contractContentTextarea) {
                return;
            }
            const content = contractContentTextarea.value || '';
            const detected = new Set();
            forEachPlaceholder(content, (name) => {
                if (!IGNORED_PLACEHOLDERS.has(name)) {
                    detected.add(name);
                }
            });
            customVariables = Array.from(detected);
            renderCustomVariableInputs();
        }

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

        // 프리셋 모드로 전환
        function switchToPresetMode() {
            document.getElementById('normalLayout').style.display = 'none';
            document.getElementById('presetLayout').style.display = 'block';
        }

        // 일반 모드로 전환
        function switchToNormalMode() {
            document.getElementById('normalLayout').style.display = 'block';
            document.getElementById('presetLayout').style.display = 'none';
        }

        // 프리셋 HTML 렌더링 및 변수를 입력 필드로 교체
        function renderPresetHtml(html, presetName) {
            const container = document.getElementById('presetHtmlContainer');
            if (!container) return;

            // <style> 태그 추출 및 스코프 적용
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = html;

            // 기존 프리셋 스타일 제거
            const oldPresetStyle = document.getElementById('presetCustomStyle');
            if (oldPresetStyle) {
                oldPresetStyle.remove();
            }

            // style 태그 추출
            const styleTags = tempDiv.querySelectorAll('style');
            let combinedCss = '';
            styleTags.forEach(styleTag => {
                const cssText = styleTag.textContent || '';
                // #presetHtmlContainer 스코프로 제한
                const scopedCss = scopeCssToContainer(cssText, '#presetHtmlContainer');
                combinedCss += scopedCss + '\n';
                styleTag.remove();
            });

            // 스코프 적용된 스타일을 head에 추가
            if (combinedCss.trim()) {
                const styleElement = document.createElement('style');
                styleElement.id = 'presetCustomStyle';
                styleElement.textContent = combinedCss;
                document.head.appendChild(styleElement);
            }

            // script 태그 제거
            tempDiv.querySelectorAll('script').forEach(el => el.remove());

            // body 태그 내용만 추출
            const bodyTag = tempDiv.querySelector('body');
            const contentHtml = bodyTag ? bodyTag.innerHTML : tempDiv.innerHTML;

            // 컨테이너에 HTML 삽입
            container.innerHTML = contentHtml;

            // 변수를 입력 필드로 교체
            replaceVariablesWithInputs(container);

            // 제목 설정
            document.getElementById('presetLayoutTitle').textContent = presetName || '표준 근로계약서';
            document.getElementById('presetTitleHidden').value = presetName || '표준 근로계약서';
        }

        // CSS를 컨테이너 스코프로 제한
        function scopeCssToContainer(cssText, scopeSelector) {
            if (!cssText || !cssText.trim()) return '';

            // body 선택자를 컨테이너로 교체
            cssText = cssText.replace(/\bbody\b/g, scopeSelector);

            // 각 CSS 규칙에 스코프 추가
            const lines = cssText.split('}').filter(line => line.trim());
            const scopedRules = lines.map(rule => {
                if (!rule.trim()) return '';

                const parts = rule.split('{');
                if (parts.length < 2) return rule + '}';

                let selectors = parts[0].trim();
                const declarations = parts[1].trim();

                // @-rules는 그대로 유지
                if (selectors.startsWith('@')) {
                    return rule + '}';
                }

                // 이미 스코프가 있는 경우 건너뛰기
                if (selectors.includes(scopeSelector)) {
                    return rule + '}';
                }

                // 여러 선택자 처리
                const selectorList = selectors.split(',').map(s => s.trim());
                const scopedSelectors = selectorList.map(selector => {
                    // 이미 #presetHtmlContainer로 시작하면 그대로
                    if (selector.startsWith(scopeSelector)) {
                        return selector;
                    }
                    return `${scopeSelector} ${selector}`;
                }).join(', ');

                return `${scopedSelectors} { ${declarations} }`;
            });

            return scopedRules.join('\n');
        }

        // 변수를 입력 필드로 교체하는 함수
        function replaceVariablesWithInputs(container) {
            const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT);
            const textNodes = [];

            while (walker.nextNode()) {
                const node = walker.currentNode;
                if (node.nodeValue && (node.nodeValue.includes('[') || node.nodeValue.includes('{'))) {
                    textNodes.push(node);
                }
            }

            textNodes.forEach(node => {
                const text = node.nodeValue;
                const parent = node.parentNode;
                if (!parent) return;

                const fragment = document.createDocumentFragment();
                let lastIndex = 0;
                const regex = /\[([^\]]+)\]|\{([^}]+)\}/g;
                let match;

                while ((match = regex.exec(text)) !== null) {
                    // 매치 전 텍스트 추가
                    if (match.index > lastIndex) {
                        fragment.appendChild(document.createTextNode(text.substring(lastIndex, match.index)));
                    }

                    const varName = match[1] || match[2];

                    // 서명 이미지는 제외
                    if (varName === 'EMPLOYER_SIGNATURE_IMAGE') {
                        fragment.appendChild(document.createTextNode(match[0]));
                    } else {
                        // 입력 필드 생성
                        const input = createVariableInput(varName);
                        fragment.appendChild(input);
                    }

                    lastIndex = regex.lastIndex;
                }

                // 나머지 텍스트 추가
                if (lastIndex < text.length) {
                    fragment.appendChild(document.createTextNode(text.substring(lastIndex)));
                }

                parent.replaceChild(fragment, node);
            });

            // 사업주 정보 자동 입력
            applyOwnerInfoToPresetForm();
        }

        // 변수 입력 필드 생성
        function createVariableInput(varName) {
            const wrapper = document.createElement('span');
            wrapper.style.cssText = 'display: inline-block; border-bottom: 1px solid #dee2e6; min-width: 100px; padding: 0 4px;';

            const input = document.createElement('input');
            input.type = 'text';
            input.className = 'form-control-plaintext d-inline';
            input.style.cssText = 'border: none; padding: 0; margin: 0; height: auto; width: auto; min-width: 80px; display: inline;';
            input.setAttribute('data-variable-name', varName);
            input.placeholder = `[${varName}]`;

            // 자동 값 설정
            const value = getDefaultValueForVariable(varName);
            if (value) {
                input.value = value;
            }

            input.addEventListener('input', updatePresetContent);

            wrapper.appendChild(input);
            return wrapper;
        }

        // 변수의 기본값 가져오기
        function getDefaultValueForVariable(varName) {
            if (!ownerInfo) return '';

            const upper = varName.toUpperCase();
            if (upper === 'EMPLOYER' || upper === 'EMPLOYER_NAME') {
                return ownerInfo.name || '';
            }
            if (upper === 'EMPLOYER_EMAIL') {
                return ownerInfo.email || '';
            }
            if (upper === 'COMPANY' || upper === 'COMPANY_NAME') {
                return ownerInfo.companyName || '';
            }
            if (upper === 'EMPLOYEE' || upper === 'EMPLOYEE_NAME') {
                return '';
            }
            if (upper === 'START_DATE' || upper === 'CONTRACT_START_DATE') {
                return new Date().toISOString().split('T')[0];
            }
            return '';
        }

        // 프리셋 폼에 사업주 정보 적용
        function applyOwnerInfoToPresetForm() {
            if (!ownerInfo) return;

            document.getElementById('presetFirstPartyName').value = ownerInfo.name || '';
            document.getElementById('presetFirstPartyEmail').value = ownerInfo.email || '';
            document.getElementById('presetFirstPartyAddress').value = ownerInfo.companyName || '';
        }

        // 프리셋 내용 업데이트 (폼 제출용)
        function updatePresetContent() {
            const container = document.getElementById('presetHtmlContainer');
            if (!container) return;

            // 모든 입력 필드의 값을 변수로 치환한 HTML 생성
            const clone = container.cloneNode(true);
            const inputs = clone.querySelectorAll('input[data-variable-name]');

            inputs.forEach(input => {
                const varName = input.getAttribute('data-variable-name');
                const value = input.value || '';
                const textNode = document.createTextNode(value);
                input.parentNode.replaceChild(textNode, input);

                // secondPartyName hidden 필드 업데이트
                if (varName.toUpperCase() === 'EMPLOYEE' || varName.toUpperCase() === 'EMPLOYEE_NAME') {
                    document.getElementById('presetSecondPartyName').value = value;
                }
            });

            // Hidden textarea에 HTML 저장
            document.getElementById('presetContentHidden').value = clone.innerHTML;
        }

        // 프리셋 선택 이벤트 핸들러
        async function loadPresetById(presetId) {
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

                // 프리셋 모드로 전환
                switchToPresetMode();

                // HTML 렌더링
                renderPresetHtml(rendered, preset.name);

            } catch (error) {
                console.error('프리셋 로딩 실패:', error);
                alert('표준 양식을 불러오지 못했습니다.');
            }
        }

        const presetSelect = document.getElementById('presetSelect');
        if (presetSelect && contractContentTextarea) {
            presetSelect.addEventListener('change', async (event) => {
                const presetId = event.target.value;
                if (!presetId) {
                    return;
                }
                await loadPresetById(presetId);
                presetSelect.value = '';
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
                        // 프리셋 모드인 경우
                        const presetLayout = document.getElementById('presetLayout');
                        if (presetLayout && presetLayout.style.display !== 'none') {
                            // 프리셋 콘텐츠 업데이트
                            updatePresetContent();

                            // normalLayout의 필드 비활성화
                            const normalLayout = document.getElementById('normalLayout');
                            if (normalLayout) {
                                const normalInputs = normalLayout.querySelectorAll('input, textarea, select');
                                normalInputs.forEach(field => {
                                    field.disabled = true;
                                    field.required = false;
                                });
                            }
                        } else {
                            // 일반 모드인 경우
                            if (contractContentTextarea) {
                                const resolvedContent = applyOwnerSignature(applyCustomVariablesToContent(contractContentTextarea.value || ''));
                                contractContentTextarea.value = resolvedContent;
                            }

                            // 프리셋 레이아웃의 필드 비활성화
                            const presetInputs = presetLayout.querySelectorAll('input, textarea, select');
                            presetInputs.forEach(field => {
                                field.disabled = true;
                                field.required = false;
                            });
                        }

                        const firstEmail = document.getElementById('firstPartyEmail')?.value ||
                                          document.getElementById('presetFirstPartyEmail')?.value;
                        const secondEmail = document.getElementById('secondPartyEmail')?.value ||
                                           document.getElementById('presetSecondPartyEmail')?.value;

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
        document.addEventListener('DOMContentLoaded', function() {
            loadPresetById('${selectedPreset}');
        });
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
