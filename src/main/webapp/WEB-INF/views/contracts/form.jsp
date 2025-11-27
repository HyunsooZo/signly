<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
                <!DOCTYPE html>
                <html lang="ko">
                <jsp:include page="../common/header.jsp">
                    <jsp:param name="additionalCss" value="/css/contract-template-base.css" />
                    <jsp:param name="additionalCss2" value="/css/contract-template-preview.css" />
                    <jsp:param name="additionalCss3" value="/css/contracts.css" />
                    <jsp:param name="additionalCss4" value="/css/templates.css" />
                    <jsp:param name="additionalCss5" value="/css/modal.css" />
                    <jsp:param name="additionalCss6" value="/css/contract-common.css" />
                </jsp:include>

                <body <c:if test="${not empty currentUserId}"> data-current-user-id="${currentUserId}"</c:if>
                    <c:if test="${not empty currentUserName}"> data-owner-name="
                        <c:out value='${currentUserName}' />"
                    </c:if>
                    <c:if test="${not empty currentUserEmail}"> data-owner-email="
                        <c:out value='${currentUserEmail}' />"
                    </c:if>
                    <c:if test="${not empty currentUserCompany}"> data-owner-company="
                        <c:out value='${currentUserCompany}' />"
                    </c:if>
                    <c:if test="${not empty currentUserBusinessPhone}"> data-owner-phone="
                        <c:out value='${currentUserBusinessPhone}' />"
                    </c:if>
                    <c:if test="${not empty currentUserBusinessAddress}"> data-owner-address="
                        <c:out value='${currentUserBusinessAddress}' />"
                    </c:if>
                    >
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
                                        <div class="alert alert-success alert-dismissible fade show" role="alert"
                                            data-auto-dismiss="true">
                                            <i class="bi bi-check-circle me-2"></i>${successMessage}
                                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                                        </div>
                                    </c:if>

                                    <c:if test="${not empty errorMessage}">
                                        <div class="alert alert-danger alert-dismissible fade show" role="alert"
                                            data-auto-dismiss="true">
                                            <i class="bi bi-exclamation-triangle me-2"></i>${errorMessage}
                                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                                        </div>
                                    </c:if>
                                </div>
                            </div>

                            <form method="post"
                                action="${not empty contractId ? '/contracts/'.concat(contractId) : '/contracts'}"
                                class="contract-form">
                                <c:if test="${not empty selectedPreset}">
                                    <input type="hidden" name="selectedPreset"
                                        value="<c:out value='${selectedPreset}'/>" />
                                </c:if>
                                <input type="hidden" id="templateId" name="templateId"
                                    value="<c:out value='${contract.templateId}'/>">
                                <c:if test="${not empty selectedTemplateContent}">
                                    <script type="application/json" id="selectedTemplateData"><c:out value="${selectedTemplateContent}"
                                                                                 escapeXml="false"/></script>
                                </c:if>
                                <c:if test="${not empty existingContractJson}">
                                    <script type="application/json" id="existingContractData"><c:out value="${existingContractJson}"
                                                                                 escapeXml="false"/></script>
                                </c:if>

                                <!-- 템플릿 레이아웃 -->
                                <div id="templateLayout">
                                    <div class="row">
                                        <div class="col-12">
                                            <div class="card mb-4">
                                                <div class="card-header">
                                                    <h5 class="card-title mb-0">
                                                        <i class="bi bi-file-earmark-text me-2"></i> <span
                                                            id="templateLayoutTitle">표준 근로계약서</span>
                                                    </h5>
                                                </div>
                                                <div class="card-body preset-preview-body">
                                                    <!-- HTML 렌더링 영역 (변수는 입력 필드로 교체됨) -->
                                                    <div id="templateHtmlContainer"></div>
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
                                                        <label for="templateSecondPartyEmail" class="form-label">근로자 이메일
                                                            <span class="text-danger">*</span></label>
                                                        <input type="email" class="form-control"
                                                            id="templateSecondPartyEmail" name="secondPartyEmail"
                                                            required maxlength="200" placeholder="example@domain.com">
                                                    </div>
                                                </div>
                                            </div>

                                            <div class="card mb-4" id="templateExpirationCard">
                                                <div class="card-header">
                                                    <h5 class="card-title mb-0">
                                                        <i class="bi bi-hourglass-split me-2"></i>서명 만료 설정
                                                    </h5>
                                                </div>
                                                <div class="card-body">
                                                    <p class="text-muted small mb-3">만료일을 비우면 계약서는 생성 시점 기준 24시간 동안
                                                        유효합니다.</p>
                                                    <div class="mb-0">
                                                        <label for="presetExpiresAt" class="form-label">만료일</label>
                                                        <input type="datetime-local" class="form-control"
                                                            id="templateExpiresAt" name="expiresAt"
                                                            value="<c:out value='${contract.expiresAtInputValue}'/>">
                                                        <div class="form-text">필요 시 서명 마감 일시를 지정하세요.</div>
                                                    </div>
                                                </div>
                                            </div>


                                            <!-- Hidden fields -->
                                            <textarea id="templateContentHidden" name="content"
                                                hidden><c:out value="${contract.content}"/></textarea>
                                            <input type="hidden" id="templateTitleHidden" name="title"
                                                value="<c:out value='${contract.title}'/>" required>
                                            <input type="hidden" id="templateFirstPartyName" name="firstPartyName"
                                                value="<c:out value='${contract.firstPartyName}'/>" required>
                                            <input type="hidden" id="templateFirstPartyEmail" name="firstPartyEmail"
                                                value="<c:out value='${contract.firstPartyEmail}'/>" required>
                                            <input type="hidden" id="templateFirstPartyAddress" name="firstPartyAddress"
                                                value="<c:out value='${contract.firstPartyAddress}'/>">
                                            <input type="hidden" id="templateSecondPartyName" name="secondPartyName"
                                                value="<c:out value='${contract.secondPartyName}'/>" required>
                                            <input type="hidden" id="templateSecondPartyEmail" name="secondPartyEmail"
                                                value="<c:out value='${contract.secondPartyEmail}'/>" required>
                                        </div>
                                    </div>

                                    <!-- 액션 버튼 -->
                                    <div class="row mt-4" id="actionButtonRow">
                                        <div class="col-12">
                                            <div class="d-flex justify-content-between">
                                                <a href="/contracts" class="btn btn-secondary">
                                                    <i class="bi bi-x me-2"></i>취소
                                                </a>
                                                <div>
                                                    <button type="button" class="btn btn-outline-primary me-2"
                                                        onclick="previewContract()">
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


                    <script src="/js/contract-form.js"></script>
                    <jsp:include page="../common/footer.jsp" />
                </body>

                </html>