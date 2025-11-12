<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<jsp:include page="../common/header.jsp">
    <jsp:param name="additionalCss" value="/css/fonts.css" />
    <jsp:param name="additionalCss2" value="/css/contracts.css" />
    <jsp:param name="additionalCss3" value="/css/contract-common.css" />
    <jsp:param name="additionalCss4" value="/css/modal.css" />
</jsp:include>
<body <c:if test="${not empty currentUserId}">data-current-user-id="<c:out value="${currentUserId}"/>"</c:if> data-has-selected-preset="<c:out value="${not empty selectedPreset ? 'true' : 'false'}"/>">
    <jsp:include page="../common/navbar.jsp">
        <jsp:param name="currentPage" value="contracts" />
    </jsp:include>

    <div class="container mt-4">
        <div class="main-content-card">
        <div class="row">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h2 class="mb-2">
                            <i class="bi bi-file-earmark-plus text-primary me-2"></i>
                            <c:out value="${pageTitle}"/>
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
                        <i class="bi bi-check-circle me-2"></i><c:out value="${successMessage}"/>
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert" data-auto-dismiss="true">
                        <i class="bi bi-exclamation-triangle me-2"></i><c:out value="${errorMessage}"/>
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>
            </div>
        </div>

        <form method="post" action="<c:out value='${not empty contractId ? "/contracts/".concat(contractId) : "/contracts"}'/>" class="contract-form">
            <c:if test="${not empty _csrf}">
                <input type="hidden" name="<c:out value='${_csrf.parameterName}'/>" value="<c:out value='${_csrf.token}'/>" />
            </c:if>
            <c:if test="${not empty selectedPreset}">
                <input type="hidden" name="selectedPreset" value="<c:out value='${selectedPreset}'/>" />
            </c:if>
            <input type="hidden" id="templateId" name="templateId" value="<c:out value='${contract.templateId}'/>">
            <c:if test="${not empty selectedTemplate}">
                <script type="application/json" id="selectedTemplateData"><c:out value="${selectedTemplateContent}"/></script>
            </c:if>
            <c:if test="${not empty existingContractJson}">
                <script type="application/json" id="existingContractData"><c:out value="${existingContractJson}"/></script>
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
                                           value="<c:out value='${contract.expiresAtInputValue}'/>">
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
                                                            <div class="card h-100 template-option-card border" data-template-card data-template-id="<c:out value='${template.templateId}'/>" data-template-selected="<c:out value='${isSelectedTemplate}'/>">
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
                                                                            data-template-button="<c:out value='${template.templateId}'/>"
                                                                            data-template-id="<c:out value='${template.templateId}'/>"
                                                                            data-template-title="<c:out value='${fn:escapeXml(template.title)}'/>"
                                                                            data-template-content="<c:out value='${fn:escapeXml(renderedHtml)}'/>"
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
                                                <option value="<c:out value='${preset.id}'/>" data-name="<c:out value='${preset.name}'/>">
                                                    <c:out value="${preset.name}"/> - <c:out value="${preset.description}"/>
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
                                                           value="<c:out value='${contract.title}'/>" required maxlength="200"
                                                           placeholder="계약서 제목을 입력하세요">
                                                </div>

                                                <div class="mb-3" id="contentSection">
                                                    <label for="content" class="form-label">계약서 내용 <span class="text-danger">*</span></label>
                                                    <textarea class="form-control content-editor" id="content" name="content"
                                                              rows="15" required placeholder="계약서 내용을 입력하세요..."><c:out value='${contract.content}'/></textarea>
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
                                       value="<c:out value='${contract.firstPartyName}'/>" required maxlength="100"
                                       placeholder="성명을 입력하세요">
                            </div>
                            <div class="mb-3">
                                <label for="firstPartyEmail" class="form-label">이메일 <span class="text-danger">*</span></label>
                                <input type="email" class="form-control" id="firstPartyEmail" name="firstPartyEmail"
                                       value="<c:out value='${contract.firstPartyEmail}'/>" required maxlength="200"
                                       placeholder="example@domain.com">
                            </div>
                            <div class="mb-4">
                                <label for="firstPartyAddress" class="form-label">회사/조직명</label>
                                <textarea class="form-control" id="firstPartyAddress" name="firstPartyAddress"
                                          rows="3" maxlength="500" placeholder="회사 또는 조직명을 입력하세요"><c:out value='${contract.firstPartyAddress}'/></textarea>
                            </div>

                            <!-- 을 (두 번째 당사자) -->
                            <h6 class="text-success mb-3">을 (두 번째 당사자)</h6>
                            <div class="mb-3">
                                <label for="secondPartyName" class="form-label">이름 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="secondPartyName" name="secondPartyName"
                                       value="<c:out value='${contract.secondPartyName}'/>" required maxlength="100"
                                       placeholder="성명을 입력하세요">
                            </div>
                            <div class="mb-3">
                                <label for="secondPartyEmail" class="form-label">이메일 <span class="text-danger">*</span></label>
                                <input type="email" class="form-control" id="secondPartyEmail" name="secondPartyEmail"
                                       value="<c:out value='${contract.secondPartyEmail}'/>" required maxlength="200"
                                       placeholder="example@domain.com">
                            </div>
                            <div class="mb-4">
                                <label for="secondPartyAddress" class="form-label">회사/조직명</label>
                                <textarea class="form-control" id="secondPartyAddress" name="secondPartyAddress"
                                          rows="3" maxlength="500" placeholder="회사 또는 조직명을 입력하세요"><c:out value='${contract.secondPartyAddress}'/></textarea>
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
                                       value="<c:out value='${contract.expiresAtInputValue}'/>">
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
                                <c:out value="${empty contractId ? '계약서 생성' : '수정 완료'}"/>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </form>
        </div>
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

    <!-- External JavaScript -->
    <script src="/js/common/utils.js"></script>
    <script src="/js/common/storage.js"></script>
    <script src="/js/contract-form.js"></script>
    
    <!-- Preset loading script for JSP conditionals -->
    <c:if test="${not empty selectedPreset}">
    <script>
        console.log('[DEBUG] selectedPreset found:', '<c:out value="${selectedPreset}"/>');
        console.log('[DEBUG] contractId found:', '<c:out value="${contractId}"/>');
        document.addEventListener('DOMContentLoaded', function() {
            console.log('[DEBUG] DOMContentLoaded - Loading preset:', '<c:out value="${selectedPreset}"/>');
            // This function will be available from contract-form.js
            if (window.contractForm && window.contractForm.loadPresetById) {
                window.contractForm.loadPresetById('<c:out value="${selectedPreset}"/>');
            }
        });
    </script>
    </c:if>
    <c:if test="${empty selectedPreset}">
    <script>
        console.log('[DEBUG] No selectedPreset found');
        console.log('[DEBUG] contractId:', '<c:out value="${contractId}"/>');
        console.log('[DEBUG] contract.presetType:', '<c:out value="${contract.selectedPreset}"/>');
    </script>
    </c:if>
    <jsp:include page="../common/footer.jsp" />
</body>
</html>
