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
    <link href="/css/contract-common.css" rel="stylesheet">
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
                    <div class="alert alert-success alert-dismissible fade show" role="alert" data-auto-dismiss="true">
                        <i class="bi bi-check-circle me-2"></i>${successMessage}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert" data-auto-dismiss="true">
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
            <input type="hidden" id="templateId" name="templateId" value="${contract.templateId}">
            <c:if test="${not empty selectedTemplate}">
                <script type="application/json" id="selectedTemplateData">${selectedTemplateContent}</script>
            </c:if>
            <c:if test="${not empty existingContractJson}">
                <script type="application/json" id="existingContractData">${existingContractJson}</script>
            </c:if>

            <!-- 프리셋 레이아웃 -->
            <div id="presetLayout">
                <div class="row">
                    <div class="col-12">
                        <div class="card mb-4">
                            <div class="card-header">
                                <h5 class="card-title mb-0">
                                    <i class="bi bi-file-earmark-text me-2"></i><span id="presetLayoutTitle">표준 근로계약서</span>
                                </h5>
                            </div>
                            <div class="card-body preset-preview-body">
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

                        <div class="card mb-4" id="presetExpirationCard">
                            <div class="card-header">
                                <h5 class="card-title mb-0">
                                    <i class="bi bi-hourglass-split me-2"></i>서명 만료 설정
                                </h5>
                            </div>
                            <div class="card-body">
                                <p class="text-muted small mb-3">만료일을 비우면 계약서는 생성 시점 기준 24시간 동안 유효합니다.</p>
                                <div class="mb-0">
                                    <label for="presetExpiresAt" class="form-label">만료일</label>
                                    <input type="datetime-local" class="form-control" id="presetExpiresAt" name="expiresAt"
                                           value="${contract.expiresAtInputValue}">
                                    <div class="form-text">필요 시 서명 마감 일시를 지정하세요.</div>
                                </div>
                            </div>
                        </div>

                        <!-- Hidden fields -->
                        <textarea id="presetContentHidden" name="content" hidden required></textarea>
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
                                <c:choose>
                                    <c:when test="${empty contractId}">
                                        <p class="text-muted">사용할 계약서 템플릿을 선택하면 아래 프리뷰에서 내용을 확인하고 변수 값을 입력할 수 있습니다.</p>

                                        <c:choose>
                                            <c:when test="${empty templates}">
                                                <div class="text-center text-muted py-5">
                                                    <i class="bi bi-collection mx-auto d-block display-5 mb-3"></i>
                                                    <p class="mb-2">사용 가능한 템플릿이 없습니다.</p>
                                                    <p class="small">먼저 템플릿을 생성한 후 계약서를 만들 수 있습니다.</p>
                                                    <a href="/templates/new" class="btn btn-primary mt-3">
                                                        <i class="bi bi-plus-circle me-2"></i>새 템플릿 만들기
                                                    </a>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="row g-3" id="templateCardContainer">
                                                    <div class="col-md-6 col-lg-4">
                                                        <div class="card h-100 template-option-card border border-dashed">
                                                            <div class="card-body d-flex flex-column justify-content-center text-center">
                                                                <i class="bi bi-plus-circle display-6 text-primary mb-3"></i>
                                                                <p class="text-muted mb-3">새 템플릿을 만들어 계약서를 준비하세요.</p>
                                                                <a href="/templates/new" class="btn btn-primary">
                                                                    템플릿 만들기
                                                                </a>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <c:forEach var="template" items="${templates}">
                                                        <c:set var="renderedHtml" value="${empty template.renderedHtml ? '' : template.renderedHtml}" />
                                                        <c:set var="previewText" value="${empty template.previewText ? '' : template.previewText}" />
                                                        <c:set var="isSelectedTemplate" value="${contract.templateId == template.templateId}" />
                                                        <div class="col-md-6 col-lg-4">
                                                            <div class="card h-100 template-option-card border" data-template-card data-template-id="${template.templateId}" data-template-selected="${isSelectedTemplate}">
                                                                <div class="card-body d-flex flex-column">
                                                                    <h6 class="card-title mb-2"><c:out value="${template.title}" /></h6>
                                                                    <p class="text-muted small flex-grow-1 mb-3">
                                                                        <c:choose>
                                                                            <c:when test="${empty previewText}">
                                                                                등록된 미리보기가 없습니다.
                                                                            </c:when>
                                                                            <c:when test="${fn:length(previewText) > 120}">
                                                                                <c:out value="${fn:substring(previewText, 0, 120)}" />...
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                <c:out value="${previewText}" />
                                                                            </c:otherwise>
                                                                        </c:choose>
                                                                    </p>
                                                                    <button type="button"
                                                                            class="btn btn-outline-primary w-100 mt-auto"
                                                                            data-template-button="${template.templateId}"
                                                                            data-template-id="${template.templateId}"
                                                                            data-template-title="${fn:escapeXml(template.title)}"
                                                                            data-template-content="${fn:escapeXml(renderedHtml)}"
                                                                            onclick="handleTemplateSelection(this)">
                                                                        템플릿 불러오기
                                                                    </button>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </c:forEach>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <!-- 숨겨진 프리셋 select (selectedPreset으로 넘어온 경우를 위해) -->
                                        <select id="presetSelect" class="d-none">
                                            <option value="">표준 양식을 선택하세요</option>
                                            <c:forEach var="preset" items="${presets}">
                                                <option value="${preset.id}" data-name="${preset.name}">
                                                    ${preset.name} - ${preset.description}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </c:when>
                                    <c:otherwise>
                                        <!-- 수정 모드: 템플릿 기반 계약서는 작성 화면과 동일하게 템플릿 렌더링 -->
                                        <c:choose>
                                            <c:when test="${not empty selectedTemplate}">
                                                <!-- 템플릿이 있는 경우: JavaScript에서 selectedTemplateData를 읽어 템플릿 렌더링 -->
                                                <p class="text-muted">선택된 템플릿의 내용을 확인하고 변수 값을 수정할 수 있습니다.</p>
                                            </c:when>
                                            <c:otherwise>
                                                <!-- 템플릿 없이 직접 작성한 계약서인 경우 -->
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

                                            <div class="mt-3" id="customVariablesContainer">
                                                <label class="form-label d-flex align-items-center gap-2">
                                                    <i class="bi bi-sliders2-vertical"></i> 변수 값 입력
                                                </label>
                                                <div class="row g-2" id="customVariableFields"></div>
                                                <div class="form-text">`{변수명}` 형식의 변수가 감지되면 해당 값을 아래에서 입력할 수 있습니다.</div>
                                            </div>

                                            <div class="mt-3" id="customContentPreviewWrapper">
                                                <div class="card">
                                                    <div class="card-header">
                                                        <h5 class="card-title mb-0">
                                                            <i class="bi bi-eye"></i> 실시간 미리보기
                                                        </h5>
                                                    </div>
                                                    <div class="card-body custom-content-preview" id="customContentPreview"></div>
                                                </div>
                                            </div>

                                                    <!-- 프리셋 폼 필드 컨테이너 (동적으로 생성됨) -->
                                                    <div id="presetFormFields"></div>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
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

                    <div class="card mb-4" id="expirationCard">
                        <div class="card-header">
                            <h5 class="card-title mb-0">
                                <i class="bi bi-hourglass-split me-2"></i>서명 만료 설정
                            </h5>
                        </div>
                        <div class="card-body">
                            <p class="text-muted small mb-3">만료일을 비우면 계약서는 생성 시점 기준 24시간 동안 유효합니다.</p>
                            <div class="mb-0">
                                <label for="expiresAt" class="form-label">만료일</label>
                                <input type="datetime-local" class="form-control" id="expiresAt" name="expiresAt"
                                       value="${contract.expiresAtInputValue}">
                                <div class="form-text">필요 시 서명 마감 일시를 지정하세요.</div>
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
                    <div class="border rounded p-4 contract-preview-panel" id="previewContent">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="/js/alerts.js"></script>
    <script>
        const contractContentTextarea = document.getElementById('content');
        const currentUserId = document.body?.dataset?.currentUserId || '';
        const existingContractDataElement = document.getElementById('existingContractData');
        let existingContractData = null;
        if (existingContractDataElement) {
            try {
                existingContractData = JSON.parse(existingContractDataElement.textContent);
            } catch (error) {
                console.error('[ERROR] 기존 계약 데이터 파싱 실패:', error);
            }
        }
        const selectedTemplateDataElement = document.getElementById('selectedTemplateData');
        let selectedTemplateData = null;
        if (selectedTemplateDataElement) {
            try {
                selectedTemplateData = JSON.parse(selectedTemplateDataElement.textContent);
            } catch (error) {
                console.error('[ERROR] 선택된 템플릿 데이터 파싱 실패:', error);
            }
        }
        const hasSelectedPreset = ${not empty selectedPreset ? 'true' : 'false'};

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
                    companyName: parsed.companyName || '',
                    businessPhone: parsed.businessPhone || '',
                    businessAddress: parsed.businessAddress || ''
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

        function toLocalDateTimeValue(date) {
            const pad = (value) => String(value).padStart(2, '0');
            return [
                date.getFullYear(),
                '-', pad(date.getMonth() + 1),
                '-', pad(date.getDate()),
                'T', pad(date.getHours()),
                ':', pad(date.getMinutes())
            ].join('');
        }

        function updateExpirationMinAttributes() {
            const minValue = toLocalDateTimeValue(new Date());
            const normal = document.getElementById('expiresAt');
            const preset = document.getElementById('presetExpiresAt');
            if (normal) {
                normal.min = minValue;
            }
            if (preset) {
                preset.min = minValue;
            }
        }

        function syncExpirationField(sourceField) {
            const normal = document.getElementById('expiresAt');
            const preset = document.getElementById('presetExpiresAt');
            if (!sourceField) {
                return;
            }
            if (sourceField === normal && preset) {
                preset.value = normal.value;
            } else if (sourceField === preset && normal) {
                normal.value = preset.value;
            }
        }

        function initializeExpirationInputs() {
            const normal = document.getElementById('expiresAt');
            const preset = document.getElementById('presetExpiresAt');

            if (!normal && !preset) {
                return;
            }

            updateExpirationMinAttributes();

            const defaultValue = toLocalDateTimeValue(new Date(Date.now() + 24 * 60 * 60 * 1000));
            const minValue = document.getElementById('expiresAt')?.min || document.getElementById('presetExpiresAt')?.min || toLocalDateTimeValue(new Date());
            let initialValue = '';
            if (normal && normal.value) {
                initialValue = normal.value;
            } else if (preset && preset.value) {
                initialValue = preset.value;
            } else {
                initialValue = defaultValue;
            }

            if (minValue && initialValue < minValue) {
                initialValue = minValue;
            }

            if (normal && !normal.value) {
                normal.value = initialValue;
            } else if (normal && normal.value && normal.value < minValue) {
                normal.value = minValue;
            }
            if (preset && !preset.value) {
                preset.value = initialValue;
            } else if (preset && preset.value && preset.value < minValue) {
                preset.value = minValue;
            }

            [normal, preset].forEach((field) => {
                if (!field) {
                    return;
                }
                field.addEventListener('focus', () => {
                    updateExpirationMinAttributes();
                    if (field.min && field.value && field.value < field.min) {
                        field.value = field.min;
                        syncExpirationField(field);
                    }
                });
                field.addEventListener('input', () => syncExpirationField(field));
            });

            syncExpirationField(normal && normal.value ? normal : preset);
        }

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

        // 프리셋 모드로 전환
        function switchToPresetMode() {
            document.getElementById('normalLayout').style.display = 'none';
            document.getElementById('presetLayout').style.display = 'block';

            updateExpirationMinAttributes();
            const normalExpires = document.getElementById('expiresAt');
            const presetExpires = document.getElementById('presetExpiresAt');
            if (normalExpires && presetExpires && normalExpires.value) {
                presetExpires.value = normalExpires.value;
            }

            // 일반 레이아웃의 required 필드 비활성화
            document.querySelectorAll('#normalLayout [required]').forEach(field => {
                field.removeAttribute('required');
            });
        }

        // 일반 모드로 전환
        function switchToNormalMode() {
            document.getElementById('normalLayout').style.display = 'block';
            document.getElementById('presetLayout').style.display = 'none';

            updateExpirationMinAttributes();
            const normalExpires = document.getElementById('expiresAt');
            const presetExpires = document.getElementById('presetExpiresAt');
            if (normalExpires && presetExpires && presetExpires.value) {
                normalExpires.value = presetExpires.value;
            }
        }

        // 프리셋 HTML 렌더링 및 변수를 입력 필드로 교체
        function renderPresetHtml(html, presetName) {
            const container = document.getElementById('presetHtmlContainer');
            if (!container) return;

            console.log('[DEBUG] renderPresetHtml - Input HTML length:', html.length);
            console.log('[DEBUG] renderPresetHtml - First 500 chars:', html.substring(0, 500));

            // CSS는 통일된 contract-common.css에서 로드하므로 동적 추출 불필요

            // body 태그 내용만 추출
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = html;

            const bodyTag = tempDiv.querySelector('body');
            let contentHtml = bodyTag ? bodyTag.innerHTML : tempDiv.innerHTML;

            // body 안에 있는 style 태그 제거
            contentHtml = contentHtml.replace(/<style[^>]*>[\s\S]*?<\/style>/gi, '');

            console.log('[DEBUG] Content HTML length:', contentHtml.length);
            console.log('[DEBUG] Content HTML first 500 chars:', contentHtml.substring(0, 500));

            // 컨테이너에 preset-document 클래스 추가하고 HTML 삽입
            container.className = 'preset-document';
            container.innerHTML = contentHtml;

            console.log('[DEBUG] After innerHTML - container has', container.children.length, 'children');
            console.log('[DEBUG] Container HTML:', container.innerHTML.substring(0, 500));

            // 변수를 입력 필드로 교체
            replaceVariablesWithInputs(container);
            restoreSavedVariableInputs(container);

            // 제목 설정
            document.getElementById('presetLayoutTitle').textContent = presetName || '표준 근로계약서';
            document.getElementById('presetTitleHidden').value = presetName || '표준 근로계약서';
        }

        let legacyVariableCounter = 0;

        function restoreSavedVariableInputs(container) {
            const savedSpans = container.querySelectorAll('.contract-variable-underline');
            savedSpans.forEach(span => {
                if (span.querySelector('input')) {
                    return;
                }
                let varName = span.getAttribute('data-variable-name');
                if (!varName) {
                    varName = 'LEGACY_FIELD_' + (legacyVariableCounter++);
                    span.setAttribute('data-variable-name', varName);
                }
                const value = span.textContent || '';
                const wrapper = createVariableInput(varName);
                const input = wrapper.querySelector('input[data-variable-name]');
                if (input) {
                    input.value = value;
                }
                if (varName) {
                    customVariableValues[varName] = value;
                }
                span.replaceWith(wrapper);
            });
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

                    // 서명 이미지는 localStorage에서 가져와서 img 태그로 교체
                    if (varName === 'EMPLOYER_SIGNATURE_IMAGE') {
                        const signatureImg = createSignatureImage();
                        fragment.appendChild(signatureImg);
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
            const upper = varName.toUpperCase();
            const normalized = upper.replace(/[-_\s]/g, '');

            // 변수 타입에 따라 적절한 문자 수 결정 (size 속성)
            let inputSize = 10;
            let maxLength = null;

            // 이름 관련 (최대 6자) - 한글과 영문 모두 지원
            if (normalized.includes('NAME') || upper === 'EMPLOYER' || upper === 'EMPLOYEE' ||
                upper.includes('이름') || upper === '사업주' || upper === '근로자' || upper === '직원' ||
                upper === '갑' || upper === '을') {
                inputSize = 6;
                maxLength = 10;
            }
            // 날짜 관련 (yyyy-mm-dd = 10자)
            else if (normalized.includes('DATE') || upper.includes('날짜') || upper.includes('일자') ||
                     upper.includes('계약일') || upper.includes('시작일') || upper.includes('종료일')) {
                inputSize = 11;
                maxLength = 10;
            }
            // 시간 관련 (hh:mm = 5자)
            else if (normalized.includes('TIME') || upper.includes('시간') || upper.includes('시각')) {
                inputSize = 6;
                maxLength = 5;
            }
            // 요일, 숫자 등 짧은 값
            else if (normalized.includes('DAY') || normalized.includes('DAYS') || normalized.includes('HOLIDAYS') ||
                     upper.includes('요일') || upper.includes('휴일')) {
                inputSize = 4;
                maxLength = 10;
            }
            // 주소, 장소, 업무 등 긴 값 (최대 20자)
            else if (normalized.includes('ADDRESS') || normalized.includes('WORKPLACE') || normalized.includes('DESCRIPTION') ||
                     upper.includes('주소') || upper.includes('장소') || upper.includes('업무') || upper.includes('내용')) {
                inputSize = 20;
                maxLength = 50;
            }
            // 급여, 금액 관련
            else if (normalized.includes('SALARY') || normalized.includes('BONUS') || normalized.includes('ALLOWANCE') ||
                     normalized.includes('PAYMENT') || normalized.includes('METHOD') ||
                     upper.includes('급여') || upper.includes('임금') || upper.includes('금액') || upper.includes('지급') || upper.includes('방법')) {
                inputSize = 12;
                maxLength = 30;
            }
            // 전화번호
            else if (normalized.includes('PHONE') || normalized.includes('TEL') || upper.includes('전화') || upper.includes('연락처')) {
                inputSize = 13;
                maxLength = 15;
            }
            // 이메일
            else if (normalized.includes('EMAIL') || normalized.includes('MAIL') || upper.includes('이메일') || upper.includes('메일')) {
                inputSize = 20;
                maxLength = 50;
            }
            // 회사명/조직명
            else if (normalized.includes('COMPANY') || normalized.includes('ORGANIZATION') ||
                     upper.includes('회사') || upper.includes('조직')) {
                inputSize = 15;
                maxLength = 30;
            }

            const wrapper = document.createElement('span');
            wrapper.className = 'contract-variable-underline';
            wrapper.setAttribute('data-variable-name', varName);

            const input = document.createElement('input');
            input.type = 'text';
            input.className = 'form-control contract-input-inline';
            input.size = inputSize;
            if (maxLength) {
                input.maxLength = maxLength;
            }
            input.setAttribute('data-variable-name', varName);

            // 적절한 플레이스홀더 설정
            input.placeholder = getPlaceholderExample(varName, upper, normalized);

            // 자동 값 설정
            const value = getDefaultValueForVariable(varName);
            if (value) {
                input.value = value;
            }

            input.addEventListener('input', updatePresetContent);

            wrapper.appendChild(input);
            return wrapper;
        }

        // 변수명에 따른 플레이스홀더 예시 생성
        function getPlaceholderExample(varName, upper, normalized) {
            // 이름 관련
            if (normalized.includes('NAME') || upper === 'EMPLOYER' || upper === 'EMPLOYEE' ||
                upper.includes('이름') || upper === '사업주' || upper === '근로자' || upper === '직원' ||
                upper === '갑' || upper === '을') {
                if (upper.includes('EMPLOYEE') || upper.includes('근로자') || upper.includes('직원') || upper === '을') {
                    return '예) 홍길동';
                }
                return '예) 김철수';
            }
            // 날짜 관련
            if (normalized.includes('DATE') || upper.includes('날짜') || upper.includes('일자') ||
                upper.includes('계약일') || upper.includes('시작일') || upper.includes('종료일')) {
                return '예) 2025-01-01';
            }
            // 시간 관련
            if (normalized.includes('TIME') || upper.includes('시간') || upper.includes('시각')) {
                return '예) 09:00';
            }
            // 요일
            if (normalized.includes('DAY') || normalized.includes('DAYS') || upper.includes('요일')) {
                return '예) 월~금';
            }
            // 휴일
            if (normalized.includes('HOLIDAYS') || upper.includes('휴일')) {
                return '예) 토, 일요일';
            }
            // 주소
            if (normalized.includes('ADDRESS') || upper.includes('주소')) {
                return '예) 서울시 강남구';
            }
            // 장소
            if (normalized.includes('WORKPLACE') || upper.includes('장소')) {
                return '예) 본사 사무실';
            }
            // 업무 내용
            if (normalized.includes('DESCRIPTION') || normalized.includes('JOB') || upper.includes('업무') || upper.includes('내용')) {
                return '예) 소프트웨어 개발';
            }
            // 급여
            if (normalized.includes('SALARY') || upper.includes('급여') || upper.includes('임금')) {
                return '예) 3,000,000';
            }
            // 상여금
            if (normalized.includes('BONUS') || upper.includes('상여')) {
                return '예) 연 500만원';
            }
            // 수당
            if (normalized.includes('ALLOWANCE') || upper.includes('수당')) {
                return '예) 식대 10만원';
            }
            // 지급일
            if (normalized.includes('PAYMENT') && normalized.includes('DAY') || upper.includes('지급일')) {
                return '예) 25';
            }
            // 지급방법
            if (normalized.includes('METHOD') || upper.includes('방법')) {
                return '예) 계좌이체';
            }
            // 전화번호
            if (normalized.includes('PHONE') || normalized.includes('TEL') || upper.includes('전화') || upper.includes('연락처')) {
                return '예) 010-1234-5678';
            }
            // 이메일
            if (normalized.includes('EMAIL') || normalized.includes('MAIL') || upper.includes('이메일') || upper.includes('메일')) {
                return '예) hong@example.com';
            }
            // 회사명
            if (normalized.includes('COMPANY') || normalized.includes('ORGANIZATION') || upper.includes('회사') || upper.includes('조직')) {
                return '예) (주)테크컴퍼니';
            }

            // 기본값
            return '';
        }

        // 서명 이미지 엘리먼트 생성
        function createSignatureImage() {
            // localStorage에서 서명 이미지 가져오기
            const signatureRaw = localStorage.getItem('signly_owner_signature');

            if (!signatureRaw) {
                // 서명이 없으면 빈 span 반환
                const span = document.createElement('span');
                span.textContent = '(서명 없음)';
                span.style.cssText = 'color: #999; font-size: 11px;';
                return span;
            }

            try {
                const signatureData = JSON.parse(signatureRaw);
                const imgSrc = signatureData.dataUrl || signatureData.imageData || signatureData.signatureData || signatureData;

                if (!imgSrc) {
                    console.warn('[WARN] 서명 이미지 데이터가 없습니다:', signatureData);
                    const span = document.createElement('span');
                    span.textContent = '(서명 없음)';
                    span.style.cssText = 'color: #999; font-size: 11px;';
                    return span;
                }

                const img = document.createElement('img');
                img.src = imgSrc;
                img.className = 'signature-stamp-image-element';
                img.style.cssText = 'display: inline-block; max-width: 90px; max-height: 40px; vertical-align: middle;';
                img.alt = '사업주 서명';

                return img;
            } catch (error) {
                console.error('[ERROR] 서명 이미지 파싱 실패:', error);
                const span = document.createElement('span');
                span.textContent = '(서명 오류)';
                span.style.cssText = 'color: #f00; font-size: 11px;';
                return span;
            }
        }

        // 변수의 기본값 가져오기 (프리셋 + 커스텀 템플릿 모두 지원)
        function getDefaultValueForVariable(varName) {
            if (!varName) return '';

            const upper = varName.toUpperCase();
            const normalized = upper.replace(/[-_\s]/g, '');

            // 사업주/고용주 이름
            if (normalized.includes('EMPLOYER') && normalized.includes('NAME') ||
                upper === 'EMPLOYER' ||
                normalized.includes('OWNER') && normalized.includes('NAME') ||
                upper === '사업주' || upper === '사업주명' || upper === '갑') {
                return ownerInfo?.name || '';
            }

            // 사업주/고용주 이메일
            if (normalized.includes('EMPLOYER') && normalized.includes('EMAIL') ||
                normalized.includes('OWNER') && normalized.includes('EMAIL') ||
                upper === '사업주이메일') {
                return ownerInfo?.email || '';
            }

            // 회사명/조직명
            if (normalized.includes('COMPANY') || normalized.includes('ORGANIZATION') ||
                upper === '회사' || upper === '회사명' || upper === '조직' || upper === '조직명') {
                return ownerInfo?.companyName || '';
            }

            // 사업주 전화번호
            if (normalized.includes('EMPLOYER') && (normalized.includes('PHONE') || normalized.includes('TEL')) ||
                normalized.includes('OWNER') && (normalized.includes('PHONE') || normalized.includes('TEL')) ||
                upper === '사업주전화번호' || upper === '사업장전화번호' || upper === '업체전화번호') {
                return ownerInfo?.businessPhone || '';
            }

            // 사업주 주소
            if (normalized.includes('EMPLOYER') && normalized.includes('ADDRESS') ||
                normalized.includes('OWNER') && normalized.includes('ADDRESS') ||
                upper === '사업주주소' || upper === '사업장주소' || upper === '업체주소') {
                return ownerInfo?.businessAddress || '';
            }

            // 근로자/직원 이름
            if (normalized.includes('EMPLOYEE') && normalized.includes('NAME') ||
                upper === 'EMPLOYEE' ||
                upper === '근로자' || upper === '근로자명' || upper === '직원' || upper === '직원명' || upper === '을') {
                return '';
            }

            // 날짜 관련
            if (normalized.includes('DATE') || normalized.includes('START') ||
                upper === '날짜' || upper === '계약일' || upper === '시작일') {
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

        // 저장된 계약 내용에서 변수 값을 추출
        function extractSavedVariableValues(savedHtml) {
            if (!savedHtml) {
                return [];
            }
            const decoded = decodeHtmlEntities(savedHtml);
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = decoded;
            const nodes = tempDiv.querySelectorAll('.contract-variable-underline');
            return Array.from(nodes).map(node => ({
                name: (node.getAttribute('data-variable-name') || '').trim(),
                value: (node.textContent || '').trim()
            }));
        }

        function fillPresetInputsFromSavedValues(savedValues, attempt = 0) {
            const container = document.getElementById('presetHtmlContainer');
            if (!container || attempt > 10) {
                return;
            }
            const inputs = container.querySelectorAll('input[data-variable-name]');
            if (!inputs.length) {
                setTimeout(() => fillPresetInputsFromSavedValues(savedValues, attempt + 1), 60);
                return;
            }

            const valueMap = new Map();
            savedValues.forEach(entry => {
                if (entry.name) {
                    valueMap.set(entry.name.toUpperCase(), entry.value);
                }
            });

            inputs.forEach((input, index) => {
                const key = (input.getAttribute('data-variable-name') || '').trim();
                let value = key ? valueMap.get(key.toUpperCase()) : undefined;
                if (value === undefined && savedValues[index]) {
                    value = savedValues[index].value;
                }
                if (value !== undefined) {
                    input.value = value;
                    if (key) {
                        customVariableValues[key] = value;
                    }
                }
            });

            updatePresetContent();
            updateDirectPreview();
        }

        // 수정 모드일 때 기존 계약서 데이터를 폼에 적용
        function applyExistingContractData() {
            if (!existingContractData) {
                return;
            }

            if (existingContractData.secondPartyEmail) {
                const emailField = document.getElementById('presetSecondPartyEmail') || document.getElementById('secondPartyEmail');
                if (emailField) {
                    emailField.value = existingContractData.secondPartyEmail;
                }
            }

            if (existingContractData.firstPartyName) {
                const target = document.getElementById('presetFirstPartyName') || document.getElementById('firstPartyName');
                if (target) {
                    target.value = existingContractData.firstPartyName;
                }
            }
            if (existingContractData.firstPartyEmail) {
                const target = document.getElementById('presetFirstPartyEmail') || document.getElementById('firstPartyEmail');
                if (target) {
                    target.value = existingContractData.firstPartyEmail;
                }
            }
            if (existingContractData.firstPartyAddress) {
                const target = document.getElementById('presetFirstPartyAddress') || document.getElementById('firstPartyAddress');
                if (target) {
                    target.value = existingContractData.firstPartyAddress;
                }
            }
            if (existingContractData.secondPartyName) {
                const target = document.getElementById('presetSecondPartyName') || document.getElementById('secondPartyName');
                if (target) {
                    target.value = existingContractData.secondPartyName;
                }
            }

            if (existingContractData.content) {
                const savedValues = extractSavedVariableValues(existingContractData.content);
                if (savedValues.length) {
                    fillPresetInputsFromSavedValues(savedValues);
                } else if (contractContentTextarea && !contractContentTextarea.value) {
                    contractContentTextarea.value = decodeHtmlEntities(existingContractData.content);
                    detectCustomVariables();
                    updateDirectPreview();
                }
            }
        }

        function loadExistingContractAsPreset() {
            if (!existingContractData || !existingContractData.content) {
                return;
            }
            const decoded = decodeHtmlEntities(existingContractData.content);
            switchToPresetMode();
            const title = existingContractData.title || document.getElementById('presetTitleHidden')?.value || '계약서';
            renderPresetHtml(decoded, title);
            applyExistingContractData();
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
                const wrapper = input.parentNode;
                if (wrapper) {
                    wrapper.setAttribute('data-variable-name', varName || '');
                    wrapper.textContent = value;
                } else {
                    input.replaceWith(document.createTextNode(value));
                }

                // secondPartyName hidden 필드 업데이트
                if (varName.toUpperCase() === 'EMPLOYEE' || varName.toUpperCase() === 'EMPLOYEE_NAME') {
                    document.getElementById('presetSecondPartyName').value = value;
                }
            });

            // 서명 이미지를 img src에서 실제 이미지 데이터로 교체
            const signatureImgs = clone.querySelectorAll('img.signature-stamp-image-element');
            signatureImgs.forEach(img => {
                // 이미 src가 있으면 그대로 유지 (Base64 데이터)
                if (img.src) {
                    // img 태그를 그대로 유지
                } else {
                    // 없으면 제거
                    img.remove();
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
                    showAlertModal('표준 양식을 불러오지 못했습니다.');
                    return;
                }

                const preset = await response.json();
                console.log('[DEBUG] Preset data:', preset);

                const sectionHtml = Array.isArray(preset.sections)
                    ? preset.sections.map(section => section.content || '').join('\n')
                    : '';

                // renderedHtml이 이미 HTML이면 그대로 사용, 아니면 디코딩
                let rendered = preset.renderedHtml || sectionHtml || '';

                console.log('[DEBUG] Initial HTML length:', rendered.length);
                console.log('[DEBUG] Has <br> tags:', rendered.includes('<br'));
                console.log('[DEBUG] Has <span> tags:', rendered.includes('<span'));
                console.log('[DEBUG] Has &lt; entities:', rendered.includes('&lt;'));
                console.log('[DEBUG] Has &gt; entities:', rendered.includes('&gt;'));

                // HTML 엔티티가 인코딩되어 있는지 확인
                if (rendered.includes('&lt;') || rendered.includes('&gt;')) {
                    console.log('[DEBUG] Decoding HTML entities...');
                    rendered = decodeHtmlEntities(rendered);
                    console.log('[DEBUG] After decode - Has <br> tags:', rendered.includes('<br'));
                }

                console.log('[DEBUG] Final HTML length:', rendered.length);
                console.log('[DEBUG] HTML preview (first 500):', rendered.substring(0, 500));
                console.log('[DEBUG] HTML contains "section-number":', rendered.includes('section-number'));

                // 프리셋 모드로 전환
                switchToPresetMode();

                // HTML 렌더링
                renderPresetHtml(rendered, preset.name);

                // 수정 모드인 경우 기존 데이터로 필드 채우기
                applyExistingContractData();

            } catch (error) {
                console.error('프리셋 로딩 실패:', error);
                showAlertModal('표준 양식을 불러오지 못했습니다.');
            }
        }

        function loadTemplateAsPreset(templateTitle, templateHtml, templateVariables) {
            console.log('[DEBUG] Loading template as preset:', templateTitle);
            console.log('[DEBUG] Template variables:', templateVariables);

            let rendered = templateHtml || '';

            // HTML 엔티티가 인코딩되어 있는지 확인
            if (rendered.includes('&lt;') || rendered.includes('&gt;')) {
                console.log('[DEBUG] Decoding HTML entities...');
                rendered = decodeHtmlEntities(rendered);
            }

            // 프리셋 모드로 전환
            switchToPresetMode();

            // HTML 렌더링
            renderPresetHtml(rendered, templateTitle);

            // 변수 기반 입력 폼 생성
            if (templateVariables && Object.keys(templateVariables).length > 0) {
                renderTemplateVariableForms(templateVariables);
            }

            applyExistingContractData();
        }

        // 템플릿 변수 기반 입력 폼 생성
        function renderTemplateVariableForms(variables) {
            const container = document.getElementById('presetHtmlContainer');
            if (!container) return;

            console.log('[DEBUG] Rendering variable forms for:', variables);

            // 컨테이너 아래에 변수 입력 섹션 추가
            let variableSection = document.getElementById('templateVariableSection');
            if (!variableSection) {
                variableSection = document.createElement('div');
                variableSection.id = 'templateVariableSection';
                variableSection.className = 'card mb-4';
                variableSection.innerHTML = `
                    <div class="card-header">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-sliders2-vertical me-2"></i>변수 값 입력
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="row g-3" id="templateVariableFields"></div>
                    </div>
                `;

                // presetExpirationCard 이전에 삽입
                const expirationCard = document.getElementById('presetExpirationCard');
                if (expirationCard && expirationCard.parentNode) {
                    expirationCard.parentNode.insertBefore(variableSection, expirationCard);
                }
            }

            const fieldsContainer = document.getElementById('templateVariableFields');
            if (!fieldsContainer) return;

            fieldsContainer.innerHTML = '';

            // 각 변수에 대한 입력 필드 생성
            Object.entries(variables).forEach(([varName, varDef]) => {
                const fieldWrapper = document.createElement('div');
                fieldWrapper.className = 'col-md-6';

                const label = document.createElement('label');
                label.className = 'form-label';
                label.textContent = varDef.label || varName;
                if (varDef.required) {
                    const requiredSpan = document.createElement('span');
                    requiredSpan.className = 'text-danger ms-1';
                    requiredSpan.textContent = '*';
                    label.appendChild(requiredSpan);
                }

                let input;

                // 타입에 따라 다른 입력 요소 생성
                switch (varDef.type) {
                    case 'DATE':
                        input = document.createElement('input');
                        input.type = 'date';
                        input.className = 'form-control';
                        break;
                    case 'EMAIL':
                        input = document.createElement('input');
                        input.type = 'email';
                        input.className = 'form-control';
                        input.placeholder = varDef.defaultValue || 'example@domain.com';
                        break;
                    case 'NUMBER':
                        input = document.createElement('input');
                        input.type = 'number';
                        input.className = 'form-control';
                        input.placeholder = varDef.defaultValue || '0';
                        break;
                    case 'TEXTAREA':
                        input = document.createElement('textarea');
                        input.className = 'form-control';
                        input.rows = 3;
                        input.placeholder = varDef.defaultValue || '';
                        break;
                    default: // TEXT
                        input = document.createElement('input');
                        input.type = 'text';
                        input.className = 'form-control';
                        input.placeholder = varDef.defaultValue || '';
                }

                input.id = 'var_' + varName;
                input.setAttribute('data-variable-name', varName);
                input.setAttribute('data-variable-type', varDef.type);
                input.required = varDef.required;
                if (varDef.defaultValue) {
                    input.value = varDef.defaultValue;
                }

                // 입력 시 HTML 컨테이너의 변수 치환
                input.addEventListener('input', function() {
                    updateTemplateVariableInHtml(varName, this.value);
                });

                // 클라이언트 측 검증
                input.addEventListener('blur', function() {
                    validateVariableInput(this, varDef);
                });

                fieldWrapper.appendChild(label);
                fieldWrapper.appendChild(input);
                fieldsContainer.appendChild(fieldWrapper);

                // 기본값이 있으면 즉시 HTML에 반영
                if (varDef.defaultValue) {
                    updateTemplateVariableInHtml(varName, varDef.defaultValue);
                }
            });
        }

        // HTML 내의 변수를 실제 값으로 치환
        function updateTemplateVariableInHtml(varName, value) {
            const container = document.getElementById('presetHtmlContainer');
            if (!container) return;

            // {{varName}} 패턴을 찾아서 value로 치환
            const pattern = new RegExp('\\{\\{' + varName + '\\}\\}', 'g');
            const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT);
            const textNodes = [];

            while (walker.nextNode()) {
                if (walker.currentNode.nodeValue && walker.currentNode.nodeValue.includes('{{' + varName + '}}')) {
                    textNodes.push(walker.currentNode);
                }
            }

            textNodes.forEach(node => {
                node.nodeValue = node.nodeValue.replace(pattern, value || '');
            });

            updatePresetContent();
        }

        // 변수 입력 값 검증
        function validateVariableInput(input, varDef) {
            const value = input.value.trim();

            // 유효성 표시 제거
            input.classList.remove('is-invalid', 'is-valid');
            const existingFeedback = input.parentElement.querySelector('.invalid-feedback');
            if (existingFeedback) {
                existingFeedback.remove();
            }

            // 필수 필드 체크
            if (varDef.required && !value) {
                showValidationError(input, varDef.label + '은(는) 필수 입력 항목입니다.');
                return false;
            }

            // 값이 있으면 타입별 검증
            if (value) {
                let errorMessage = null;

                switch (varDef.type) {
                    case 'NUMBER':
                        if (isNaN(value) || !/^-?\d+(\.\d+)?$/.test(value)) {
                            errorMessage = '숫자만 입력 가능합니다.';
                        }
                        break;
                    case 'EMAIL':
                        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                        if (!emailRegex.test(value)) {
                            errorMessage = '올바른 이메일 형식이 아닙니다.';
                        }
                        break;
                    case 'DATE':
                        const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
                        if (!dateRegex.test(value)) {
                            errorMessage = '날짜 형식은 YYYY-MM-DD 이어야 합니다.';
                        }
                        break;
                }

                if (errorMessage) {
                    showValidationError(input, errorMessage);
                    return false;
                }
            }

            // 검증 통과
            input.classList.add('is-valid');
            return true;
        }

        // 검증 오류 표시
        function showValidationError(input, message) {
            input.classList.add('is-invalid');

            const feedback = document.createElement('div');
            feedback.className = 'invalid-feedback';
            feedback.textContent = message;

            input.parentElement.appendChild(feedback);
        }

        // 폼 제출 시 모든 변수 검증
        function validateAllTemplateVariables() {
            const variableInputs = document.querySelectorAll('[data-variable-name][data-variable-type]');
            let allValid = true;

            variableInputs.forEach(input => {
                const varName = input.getAttribute('data-variable-name');
                const varType = input.getAttribute('data-variable-type');
                const isRequired = input.hasAttribute('required');

                const varDef = {
                    label: input.parentElement.querySelector('label')?.textContent.replace('*', '').trim() || varName,
                    type: varType,
                    required: isRequired
                };

                if (!validateVariableInput(input, varDef)) {
                    allValid = false;
                }
            });

            return allValid;
        }

        function highlightTemplateCard(selectedCard) {
            const cards = document.querySelectorAll('[data-template-card]');
            cards.forEach(card => {
                card.classList.remove('border-primary', 'shadow');
                const button = card.querySelector('[data-template-button]');
                if (button) {
                    button.classList.remove('btn-primary', 'text-white');
                    button.classList.add('btn-outline-primary');
                }
            });

            if (!selectedCard) {
                return;
            }

            selectedCard.classList.add('border-primary', 'shadow');
            const activeButton = selectedCard.querySelector('[data-template-button]');
            if (activeButton) {
                activeButton.classList.remove('btn-outline-primary');
                activeButton.classList.add('btn-primary', 'text-white');
            }
        }

        function handleTemplateSelection(button) {
            if (!button) {
                return;
            }

            const templateId = button.getAttribute('data-template-id');
            const templateTitle = button.getAttribute('data-template-title') || '';
            const templateContent = button.getAttribute('data-template-content') || '';

            const templateIdInput = document.getElementById('templateId');
            if (templateIdInput) {
                templateIdInput.value = templateId || '';
            }

            const selectedCard = button.closest('[data-template-card]');
            highlightTemplateCard(selectedCard);

            if (templateTitle || templateContent) {
                loadTemplateAsPreset(templateTitle, templateContent);
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
            // getDefaultValueForVariable과 동일한 로직 사용
            return getDefaultValueForVariable(variableName);
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
            const previewContent = document.getElementById('previewContent');
            if (!previewContent) return;

            let htmlToPreview = '';

            // 프리셋 모드인 경우
            const presetLayout = document.getElementById('presetLayout');
            if (presetLayout && presetLayout.style.display !== 'none') {
                // 프리셋 컨테이너에서 HTML 가져오기
                const presetContainer = document.getElementById('presetHtmlContainer');
                if (presetContainer) {
                    const clone = presetContainer.cloneNode(true);

                    // input 필드를 실제 값으로 교체
                    const inputs = clone.querySelectorAll('input[data-variable-name]');
                    inputs.forEach(input => {
                        const value = input.value || '';
                        const span = document.createElement('span');
                        span.textContent = value;
                        span.style.borderBottom = '1px solid #333';
                        span.style.display = 'inline-block';
                        span.style.minWidth = '80px';
                        input.parentNode.replaceChild(span, input);
                    });

                    htmlToPreview = clone.innerHTML;
                } else {
                    htmlToPreview = '<p>프리셋 내용이 없습니다.</p>';
                }
            } else {
                // 일반 모드인 경우
                if (!contractContentTextarea) {
                    htmlToPreview = '<p>계약서 내용이 없습니다.</p>';
                } else {
                    const raw = contractContentTextarea.value || '';
                    const withVariables = applyCustomVariablesToContent(raw);
                    const withSignature = applyOwnerSignature(withVariables);
                    htmlToPreview = sanitizeHtml(withSignature);
                }
            }

            previewContent.innerHTML = htmlToPreview;
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
                            // 템플릿 변수 검증
                            if (!validateAllTemplateVariables()) {
                                event.preventDefault();
                                event.stopPropagation();
                                showAlertModal('입력한 변수 값을 확인해주세요.');
                                return false;
                            }

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
                            showAlertModal('갑과 을의 이메일 주소는 달라야 합니다.');
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
                
        document.addEventListener('DOMContentLoaded', initializeExpirationInputs);

        // 초기 레이아웃 설정: selectedPreset이나 selectedTemplate이 있으면 해당 레이아웃 표시
        document.addEventListener('DOMContentLoaded', function() {
            if (selectedTemplateData) {
                console.log('[INIT] Template edit detected, switching to preset layout');
                switchToPresetMode();
            } else if (existingContractData && existingContractData.content) {
                console.log('[INIT] Existing contract content detected, switching to preset layout');
                switchToPresetMode();
            } else if (hasSelectedPreset) {
                console.log('[INIT] Preset mode detected, will load preset');
            } else {
                console.log('[INIT] Normal mode detected, switching to normal layout');
                switchToNormalMode();
            }
        });

        <c:if test="${empty contractId}">
        document.addEventListener('DOMContentLoaded', function() {
            const templateIdInput = document.getElementById('templateId');
            const templateIdValue = templateIdInput ? templateIdInput.value : '';
            if (templateIdValue) {
                const selector = '[data-template-button="' + templateIdValue + '"]';
                const button = document.querySelector(selector);
                if (button) {
                    handleTemplateSelection(button);
                    return;
                }
            }

            const preselectedCard = document.querySelector('[data-template-card][data-template-selected="true"]');
            if (preselectedCard) {
                const button = preselectedCard.querySelector('[data-template-button]');
                if (button) {
                    handleTemplateSelection(button);
                }
            }
        });
        </c:if>

        // 템플릿 선택 후 자동 로드 (수정 모드 포함)
        document.addEventListener('DOMContentLoaded', function() {
            if (selectedTemplateData && selectedTemplateData.renderedHtml) {
                if (selectedTemplateData.templateId) {
                    const templateIdInput = document.getElementById('templateId');
                    if (templateIdInput) {
                        templateIdInput.value = selectedTemplateData.templateId;
                    }
                }
                loadTemplateAsPreset(
                    selectedTemplateData.title || '',
                    selectedTemplateData.renderedHtml || '',
                    selectedTemplateData.variables || {}
                );
            } else if (existingContractData && existingContractData.content) {
                loadExistingContractAsPreset();
            } else if (!hasSelectedPreset) {
                applyExistingContractData();
            }
        });

                // 페이지 로드 시 selectedPreset 또는 selectedTemplate이 있으면 자동으로 로드
        <c:if test="${not empty selectedPreset}">
        console.log('[DEBUG] selectedPreset found:', '${selectedPreset}');
        console.log('[DEBUG] contractId found:', '${contractId}');
        document.addEventListener('DOMContentLoaded', function() {
            console.log('[DEBUG] DOMContentLoaded - Loading preset:', '${selectedPreset}');
            loadPresetById('${selectedPreset}');
        });
        </c:if>
        <c:if test="${empty selectedPreset}">
        console.log('[DEBUG] No selectedPreset found');
        console.log('[DEBUG] contractId:', '${contractId}');
        console.log('[DEBUG] contract.presetType:', '${contract.selectedPreset}');
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

        function showAlertModal(message) {
            const modalHtml = `
                <div class="modal fade" id="alertModal" tabindex="-1">
                    <div class="modal-dialog modal-dialog-centered">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title">알림</h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                            </div>
                            <div class="modal-body">
                                ${message}
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-primary" data-bs-dismiss="modal">확인</button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            const existingModal = document.getElementById('alertModal');
            if (existingModal) {
                existingModal.remove();
            }
            document.body.insertAdjacentHTML('beforeend', modalHtml);
            const modal = new bootstrap.Modal(document.getElementById('alertModal'));
            modal.show();
            document.getElementById('alertModal').addEventListener('hidden.bs.modal', function () {
                this.remove();
            });
        }
    </script>
</body>
</html>
