<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<jsp:include page="../common/header.jsp">
    <jsp:param name="additionalCss" value="/css/contracts.css"/>
    <jsp:param name="additionalCss2" value="/css/contract-common.css"/>
    <jsp:param name="additionalCss3" value="/css/modal.css"/>
</jsp:include>
<body>
<jsp:include page="../common/navbar.jsp">
    <jsp:param name="currentPage" value="contracts"/>
</jsp:include>

<div class="container mt-4">
    <div class="main-content-card">
        <div class="row">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-start mb-4">
                    <div>
                        <h2 class="mb-2">
                            <i class="bi bi-file-earmark-check text-primary me-2"></i>
                            <c:out value="${contract.title}"/>
                        </h2>
                        <div class="d-flex align-items-center gap-3">
                            <c:choose>
                                <c:when test="${contract.status == 'DRAFT'}">
                                    <span class="badge bg-secondary fs-6">초안</span>
                                </c:when>
                                <c:when test="${contract.status == 'PENDING'}">
                                    <span class="badge bg-warning fs-6">서명 대기</span>
                                </c:when>
                                <c:when test="${contract.status == 'SIGNED'}">
                                    <span class="badge bg-success fs-6">서명 완료</span>
                                </c:when>
                                <c:when test="${contract.status == 'CANCELLED'}">
                                    <span class="badge bg-danger fs-6">취소</span>
                                </c:when>
                                <c:when test="${contract.status == 'EXPIRED'}">
                                    <span class="badge bg-dark fs-6">만료</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-light text-dark fs-6"><c:out
                                            value="${contract.status}"/></span>
                                </c:otherwise>
                            </c:choose>
                            <small class="text-muted">
                                생성일: <fmt:formatDate value="${contract.createdAt}" pattern="yyyy-MM-dd HH:mm"/>
                            </small>
                            <small class="text-muted">
                                수정일: <fmt:formatDate value="${contract.updatedAt}" pattern="yyyy-MM-dd HH:mm"/>
                            </small>
                        </div>
                    </div>
                    <a href="/contracts" class="btn btn-outline-secondary">
                        <i class="bi bi-arrow-left me-2"></i>목록으로
                    </a>
                </div>

                <!-- 알림 메시지 -->
                <c:if test="${not empty successMessage}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <i class="bi bi-check-circle me-2"></i><c:out value="${successMessage}"/>
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="bi bi-exclamation-triangle me-2"></i><c:out value="${errorMessage}"/>
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>
            </div>
        </div>

        <div class="row">
            <!-- 계약서 내용 -->
            <div class="col-lg-8">
                <div class="card">
                    <div class="card-header">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-file-earmark-text me-2"></i>계약서 내용
                        </h5>
                    </div>
                    <div class="card-body p-0 contract-detail-card-body">
                        <!-- HTML 자동 감지를 위해 항상 동일한 컨테이너 사용 -->
                        <div class="contract-content contract-content--html contract-detail-content-white"
                             id="contractContentHtmlContainer"></div>
                    </div>
                </div>

                <!-- 서명 정보 -->
                <c:if test="${not empty contract.signatures}">
                    <div class="card mt-4">
                        <div class="card-header">
                            <h5 class="card-title mb-0">
                                <i class="bi bi-pen me-2"></i>서명 정보
                            </h5>
                        </div>
                        <div class="card-body">
                            <c:forEach var="signature" items="${contract.signatures}">
                                <div class="signature-info">
                                    <div class="d-flex justify-content-between align-items-start">
                                        <div>
                                            <h6 class="mb-1"><c:out value="${signature.signerName}"/></h6>
                                            <p class="text-muted mb-1"><c:out value="${signature.signerEmail}"/></p>
                                            <small class="text-muted">
                                                서명일시: <fmt:formatDate value="${signature.signedAt}"
                                                                      pattern="yyyy-MM-dd HH:mm:ss"/>
                                            </small>
                                        </div>
                                        <div class="text-end">
                                            <span class="badge bg-success">서명 완료</span>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>
            </div>

            <!-- 사이드바: 정보 및 작업 -->
            <div class="col-lg-4">
                <div class="action-buttons">
                    <!-- 작업 버튼들 -->
                    <div class="card mb-3">
                        <div class="card-header">
                            <h6 class="card-title mb-0">
                                <i class="bi bi-gear me-2"></i>작업
                            </h6>
                        </div>
                        <div class="card-body">
                            <div class="d-grid gap-2">
                                <c:choose>
                                    <c:when test="${contract.status == 'SIGNED'}">
                                        <!-- 서명 완료된 계약서 -->
                                        <a href="/contracts/<c:out value='${contract.id}'/>/pdf-view"
                                           class="btn btn-success">
                                            <i class="bi bi-file-pdf me-2"></i>계약서 PDF 보기
                                        </a>
                                        <a href="/contracts/<c:out value='${contract.id}'/>/pdf/download"
                                           class="btn btn-outline-primary" download>
                                            <i class="bi bi-download me-2"></i>PDF 다운로드
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <!-- 진행 중인 계약서 -->
                                        <c:if test="${contract.status == 'DRAFT'}">
                                            <a href="/contracts/<c:out value='${contract.id}'/>/edit"
                                               class="btn btn-primary">
                                                <i class="bi bi-pencil me-2"></i>수정
                                            </a>
                                            <button type="button" class="btn btn-success" onclick="sendForSigning()">
                                                <i class="bi bi-send me-2"></i>서명 요청 전송
                                            </button>
                                            <hr>
                                            <button type="button" class="btn btn-outline-danger"
                                                    onclick="deleteContract()">
                                                <i class="bi bi-trash me-2"></i>삭제
                                            </button>
                                        </c:if>

                                        <c:if test="${contract.status == 'PENDING'}">
                                            <button type="button" class="btn btn-outline-info"
                                                    onclick="resendEmail()">
                                                <i class="bi bi-arrow-repeat me-2"></i>서명 요청 재전송
                                            </button>
                                        </c:if>

                                        <c:if test="${contract.status == 'PENDING' or contract.status == 'SIGNED'}">
                                            <button type="button" class="btn btn-outline-warning"
                                                    onclick="cancelContract()">
                                                <i class="bi bi-x-circle me-2"></i>계약 취소
                                            </button>
                                        </c:if>

                                        <c:if test="${contract.status != 'SIGNED'}">
                                            <button type="button" class="btn btn-outline-primary"
                                                    onclick="previewContract()">
                                                <i class="bi bi-eye me-2"></i>미리보기
                                            </button>
                                        </c:if>

                                        <c:if test="${contract.status == 'PENDING'}">
                                            <a href="/sign/<c:out value='${contract.id}'/>"
                                               class="btn btn-outline-success" target="_blank">
                                                <i class="bi bi-pen me-2"></i>서명 페이지 보기
                                            </a>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <!-- 계약서 정보 -->
                    <div class="card mb-3">
                        <div class="card-header">
                            <h6 class="card-title mb-0">
                                <i class="bi bi-info-circle me-2"></i>계약서 정보
                            </h6>
                        </div>
                        <div class="card-body p-0">
                            <div class="contract-info">
                                <div class="info-item">
                                    <span class="fw-medium">계약서 ID:</span>
                                    <span class="text-muted"><c:out value="${contract.id}"/></span>
                                </div>
                                <div class="info-item">
                                    <span class="fw-medium">템플릿 ID:</span>
                                    <span class="text-muted"><c:out value="${contract.templateId}"/></span>
                                </div>
                                <div class="info-item">
                                    <span class="fw-medium">상태:</span>
                                    <c:choose>
                                        <c:when test="${contract.status == 'DRAFT'}">
                                            <span class="badge bg-secondary">초안</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'PENDING'}">
                                            <span class="badge bg-warning">서명 대기</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'SIGNED'}">
                                            <span class="badge bg-success">서명 완료</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'CANCELLED'}">
                                            <span class="badge bg-danger">취소</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'EXPIRED'}">
                                            <span class="badge bg-dark">만료</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-light text-dark"><c:out
                                                    value="${contract.status}"/></span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="info-item">
                                    <span class="fw-medium">생성일:</span>
                                    <span class="text-muted">
                                        <fmt:formatDate value="${contract.createdAt}" pattern="yyyy-MM-dd HH:mm"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="fw-medium">수정일:</span>
                                    <span class="text-muted">
                                        <fmt:formatDate value="${contract.updatedAt}" pattern="yyyy-MM-dd HH:mm"/>
                                    </span>
                                </div>
                                <c:if test="${not empty contract.expiresAt}">
                                    <div class="info-item">
                                        <span class="fw-medium">만료일:</span>
                                        <span class="text-muted">
                                            <fmt:formatDate value="${contract.expiresAt}" pattern="yyyy-MM-dd HH:mm"/>
                                        </span>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <!-- 당사자 정보 -->
                    <div class="card">
                        <div class="card-header">
                            <h6 class="card-title mb-0">
                                <i class="bi bi-people me-2"></i>당사자 정보
                            </h6>
                        </div>
                        <div class="card-body p-0">
                            <div class="contract-info">
                                <div class="mb-3">
                                    <h6 class="text-primary mb-2">갑 (첫 번째 당사자)</h6>
                                    <div class="info-item">
                                        <span class="fw-medium">이름:</span>
                                        <span class="text-muted"><c:out value="${contract.firstParty.name}"/></span>
                                    </div>
                                    <div class="info-item">
                                        <span class="fw-medium">이메일:</span>
                                        <span class="text-muted"><c:out value="${contract.firstParty.email}"/></span>
                                    </div>
                                    <c:if test="${not empty contract.firstParty.organizationName}">
                                        <div class="info-item">
                                            <span class="fw-medium">회사/조직:</span>
                                            <span class="text-muted"><c:out
                                                    value="${contract.firstParty.organizationName}"/></span>
                                        </div>
                                    </c:if>
                                </div>
                                <div>
                                    <h6 class="text-success mb-2">을 (두 번째 당사자)</h6>
                                    <div class="info-item">
                                        <span class="fw-medium">이름:</span>
                                        <span class="text-muted"><c:out value="${contract.secondParty.name}"/></span>
                                    </div>
                                    <div class="info-item">
                                        <span class="fw-medium">이메일:</span>
                                        <span class="text-muted"><c:out value="${contract.secondParty.email}"/></span>
                                    </div>
                                    <c:if test="${not empty contract.secondParty.organizationName}">
                                        <div class="info-item">
                                            <span class="fw-medium">회사/조직:</span>
                                            <span class="text-muted"><c:out
                                                    value="${contract.secondParty.organizationName}"/></span>
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- 미리보기 모달 -->
<div class="modal fade contract-preview-modal" id="previewModal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down">
        <div class="modal-content contract-detail-modal-white">
            <div class="modal-header">
                <h5 class="modal-title">
                    <i class="bi bi-eye me-2"></i>계약서 미리보기
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body p-4 contract-detail-modal-body-white">
                <div class="alert alert-info">
                    <i class="bi bi-info-circle me-2"></i>
                    아래는 현재 계약서 내용의 미리보기입니다.
                </div>
                <div class="border rounded p-4 contract-preview-modal-content contract-detail-modal-body-white"
                     id="previewContent">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>

<!-- 삭제 확인 모달 -->
<div class="modal fade" id="deleteModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">계약서 삭제 확인</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p>정말로 '<strong><c:out value="${contract.title}"/></strong>' 계약서를 삭제하시겠습니까?</p>
                <p class="text-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>
                    이 작업은 되돌릴 수 없습니다.
                </p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                <form id="deleteForm" method="post" action="/contracts/<c:out value='${contract.id}'/>/delete"
                      class="d-inline">
                    <c:if test="${not empty _csrf}">
                        <input type="hidden" name="<c:out value='${_csrf.parameterName}'/>"
                               value="<c:out value='${_csrf.token}'/>"/>
                    </c:if>
                    <button type="submit" class="btn btn-danger">삭제</button>
                </form>
            </div>
        </div>
    </div>
</div>

<div id="contractPreviewData" hidden
     data-first-party-name="<c:out value='${contract.firstParty.name}'/>"
     data-first-party-email="<c:out value='${contract.firstParty.email}'/>"
     data-first-party-org="<c:out value='${empty contract.firstParty.organizationName ? "-" : contract.firstParty.organizationName}'/>"
     data-second-party-name="<c:out value='${contract.secondParty.name}'/>"
     data-second-party-email="<c:out value='${contract.secondParty.email}'/>"
     data-second-party-org="<c:out value='${empty contract.secondParty.organizationName ? "-" : contract.secondParty.organizationName}'/>"
     data-contract-title="<c:out value='${contract.title}'/>"
     data-preset-type="<c:out value='${contract.presetType}'/>">
</div>

<textarea id="contractContentHtml" hidden>${fn:escapeXml(contract.content)}</textarea>

<!-- External JavaScript -->
<script src="/js/common.js"></script>
<script src="/js/contract-detail.js"></script>
<jsp:include page="../common/footer.jsp"/>
</body>
</html>
