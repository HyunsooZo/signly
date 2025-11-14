<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<jsp:include page="../common/header.jsp">
    <jsp:param name="additionalCss" value="/css/templates.css"/>
    <jsp:param name="additionalCss2" value="/css/modal.css"/>
</jsp:include>
<body>
<jsp:include page="../common/navbar.jsp">
    <jsp:param name="currentPage" value="templates"/>
</jsp:include>

<div class="container mt-4">
    <div class="row">
        <div class="col-12">
            <div class="d-flex justify-content-between align-items-start mb-4">
                <div>
                    <h2 class="mb-2">
                        <i class="bi bi-file-earmark-text text-primary me-2"></i>
                        <c:out value="${template.title}"/>
                    </h2>
                    <div class="d-flex align-items-center gap-3">
                        <c:choose>
                            <c:when test="${template.status == 'DRAFT'}">
                                <span class="badge bg-secondary fs-6">초안</span>
                            </c:when>
                            <c:when test="${template.status == 'ACTIVE'}">
                                <span class="badge bg-success fs-6">활성</span>
                            </c:when>
                            <c:when test="${template.status == 'ARCHIVED'}">
                                <span class="badge bg-warning fs-6">보류</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-light text-dark fs-6"><c:out value="${template.status}"/></span>
                            </c:otherwise>
                        </c:choose>
                        <small class="text-muted">
                            생성일: <fmt:formatDate value="${template.createdAtDate}" pattern="yyyy-MM-dd HH:mm"/>
                        </small>
                        <small class="text-muted">
                            수정일: <fmt:formatDate value="${template.updatedAtDate}" pattern="yyyy-MM-dd HH:mm"/>
                        </small>
                    </div>
                </div>
                <a href="/templates" class="btn btn-outline-secondary">
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

    <div class="row">
        <!-- 템플릿 내용 -->
        <div class="col-lg-8">
            <div class="card">
                <div class="card-header">
                    <h5 class="card-title mb-0">
                        <i class="bi bi-file-earmark-text me-2"></i>템플릿 내용
                    </h5>
                </div>
                <div class="card-body p-0">
                    <div class="template-content" id="templateContent">
                    </div>
                </div>
            </div>
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
                            <a href="/templates/<c:out value='${template.templateId}'/>/edit" class="btn btn-primary">
                                <i class="bi bi-pencil me-2"></i>수정
                            </a>

                            <button type="button" class="btn btn-outline-primary" onclick="previewTemplate()">
                                <i class="bi bi-eye me-2"></i>미리보기
                            </button>

                            <c:if test="${template.status == 'ACTIVE'}">
                                <a href="/contracts/new?templateId=<c:out value='${template.templateId}'/>"
                                   class="btn btn-success">
                                    <i class="bi bi-file-plus me-2"></i>계약서 생성
                                </a>
                            </c:if>

                            <hr>

                            <c:choose>
                                <c:when test="${template.status == 'DRAFT'}">
                                    <button type="button" class="btn btn-outline-success" onclick="activateTemplate()">
                                        <i class="bi bi-check-circle me-2"></i>활성화
                                    </button>
                                    <button type="button" class="btn btn-outline-danger" onclick="deleteTemplate()">
                                        <i class="bi bi-trash me-2"></i>삭제
                                    </button>
                                </c:when>
                                <c:when test="${template.status == 'ACTIVE'}">
                                    <button type="button" class="btn btn-outline-warning" onclick="archiveTemplate()">
                                        <i class="bi bi-archive me-2"></i>보류
                                    </button>
                                </c:when>
                                <c:when test="${template.status == 'ARCHIVED'}">
                                    <button type="button" class="btn btn-outline-success" onclick="activateTemplate()">
                                        <i class="bi bi-arrow-clockwise me-2"></i>다시 활성화
                                    </button>
                                </c:when>
                            </c:choose>
                        </div>
                    </div>
                </div>

                <!-- 템플릿 정보 -->
                <div class="card">
                    <div class="card-header">
                        <h6 class="card-title mb-0">
                            <i class="bi bi-info-circle me-2"></i>템플릿 정보
                        </h6>
                    </div>
                    <div class="card-body p-0">
                        <div class="template-info">
                            <div class="info-item">
                                <span class="fw-medium">템플릿 ID:</span>
                                <span class="text-muted"><c:out value="${template.templateId}"/></span>
                            </div>
                            <div class="info-item">
                                <span class="fw-medium">상태:</span>
                                <c:choose>
                                    <c:when test="${template.status == 'DRAFT'}">
                                        <span class="badge bg-secondary">초안</span>
                                    </c:when>
                                    <c:when test="${template.status == 'ACTIVE'}">
                                        <span class="badge bg-success">활성</span>
                                    </c:when>
                                    <c:when test="${template.status == 'ARCHIVED'}">
                                        <span class="badge bg-warning">보류</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge bg-light text-dark"><c:out
                                                value="${template.status}"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="info-item">
                                <span class="fw-medium">생성일:</span>
                                <span class="text-muted">
                                        <fmt:formatDate value="${template.createdAtDate}" pattern="yyyy-MM-dd HH:mm"/>
                                    </span>
                            </div>
                            <div class="info-item">
                                <span class="fw-medium">수정일:</span>
                                <span class="text-muted">
                                        <fmt:formatDate value="${template.updatedAtDate}" pattern="yyyy-MM-dd HH:mm"/>
                                    </span>
                            </div>
                            <div class="info-item">
                                <span class="fw-medium">섹션 수:</span>
                                <span class="text-muted"><c:out value="${template.sections.size()}"/></span>
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
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">
                    <i class="bi bi-eye me-2"></i>템플릿 미리보기
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="alert alert-info">
                    <i class="bi bi-info-circle me-2"></i>
                    실제 계약서 생성 시 변수들이 실제 값으로 치환됩니다.
                </div>
                <div class="border rounded p-3 template-preview-content" id="previewContent">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>

<!-- Hidden forms for sensitive actions -->
<form id="activateForm" method="post" action="/templates/<c:out value='${template.templateId}'/>/activate"
      class="d-none">
    <c:if test="${not empty _csrf}">
        <input type="hidden" name="<c:out value='${_csrf.parameterName}'/>" value="<c:out value='${_csrf.token}'/>"/>
    </c:if>
</form>
<form id="archiveForm" method="post" action="/templates/<c:out value='${template.templateId}'/>/archive" class="d-none">
    <c:if test="${not empty _csrf}">
        <input type="hidden" name="<c:out value='${_csrf.parameterName}'/>" value="<c:out value='${_csrf.token}'/>"/>
    </c:if>
</form>

<!-- 삭제 확인 모달 -->
<div class="modal fade" id="deleteModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">템플릿 삭제 확인</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p>정말로 '<strong><c:out value="${template.title}"/></strong>' 템플릿을 삭제하시겠습니까?</p>
                <p class="text-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>
                    이 작업은 되돌릴 수 없습니다.
                </p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                <form id="deleteForm" method="post" action="/templates/<c:out value='${template.templateId}'/>/delete"
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

<script>
    // 전역 변수로 선언
    window.csrfParam = '<c:out value="${_csrf.parameterName}" default="_csrf"/>';
    window.csrfToken = '<c:out value="${_csrf.token}" default=""/>';

    // 페이지 로드 시 템플릿 내용 디코딩 및 표시
    document.addEventListener('DOMContentLoaded', function () {
        const templateContent = document.getElementById('templateContent');
        const rawHtml = `<c:out value="${template.renderedHtml}" escapeXml="true" />`;

        // 이중 인코딩 디코딩
        const temp = document.createElement('textarea');
        temp.innerHTML = rawHtml;
        let decoded = temp.value;

        // HTML 엔티티가 남아있으면 한번 더 디코딩
        if (decoded.includes('&lt;') || decoded.includes('&gt;')) {
            temp.innerHTML = decoded;
            decoded = temp.value;
        }

        // 변수를 밑줄로 변환
        decoded = decoded.replace(/<span class="template-variable"[^>]*>[\s\S]*?<\/span>/g, '<span class="blank-line"></span>');
        // [VARIABLE_NAME] 형식도 밑줄로 변환
        decoded = decoded.replace(/\[[\w_]+\]/g, '<span class="blank-line"></span>');

        templateContent.innerHTML = decoded;

        const deleteForm = document.getElementById('deleteForm');
        if (deleteForm) {
            deleteForm.addEventListener('submit', function () {
                appendCsrfField(deleteForm);
            });
        }
    });

    function readCsrfFromCookie() {
        const match = document.cookie.split(';').map(c => c.trim()).find(c => c.startsWith('XSRF-TOKEN='));
        return match ? decodeURIComponent(match.substring('XSRF-TOKEN='.length)) : '';
    }

    function appendCsrfField(form) {
        if (!form) {
            return;
        }
        const tokenValue = readCsrfFromCookie() || window.csrfToken;
        if (window.csrfParam && tokenValue) {
            let input = form.querySelector('input[name="' + window.csrfParam + '"]');
            if (!input) {
                input = document.createElement('input');
                input.type = 'hidden';
                input.name = window.csrfParam;
                form.appendChild(input);
            }
            input.value = tokenValue;
        } else {
            const existingCsrfInput = document.querySelector('input[name="_csrf"]');
            if (existingCsrfInput) {
                let input = form.querySelector('input[name="_csrf"]');
                if (!input) {
                    input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = '_csrf';
                    form.appendChild(input);
                }
                input.value = existingCsrfInput.value;
            }
        }
    }

    function submitHiddenForm(formId) {
        const form = document.getElementById(formId);
        if (!form) {
            console.warn('Form not found:', formId);
            return;
        }
        appendCsrfField(form);
        form.submit();
    }

    function previewTemplate() {
        let html = decodeHtml('<c:out value="${template.renderedHtml}" escapeXml="true" />');

        // 변수를 밑줄로 변환
        html = html.replace(/<span class="template-variable"[^>]*>[\s\S]*?<\/span>/g, '<span class="blank-line"></span>');
        // [VARIABLE_NAME] 형식도 밑줄로 변환
        html = html.replace(/\[[\w_]+\]/g, '<span class="blank-line"></span>');

        document.getElementById('previewContent').innerHTML = html || '<p class="text-muted">템플릿 내용이 비어있습니다.</p>';
        new bootstrap.Modal(document.getElementById('previewModal')).show();
    }

    function decodeHtml(value) {
        const textarea = document.createElement('textarea');
        textarea.innerHTML = value;
        return textarea.value;
    }

    function activateTemplate() {
        showConfirmModal(
            '템플릿을 활성화하시겠습니까?',
            function () {
                submitHiddenForm('activateForm');
            },
            '활성화',
            '취소',
            'btn-success'
        );
    }

    function archiveTemplate() {
        showConfirmModal(
            '템플릿을 보류하시겠습니까?',
            function () {
                submitHiddenForm('archiveForm');
            },
            '보류',
            '취소',
            'btn-warning'
        );
    }

    function deleteTemplate() {
        new bootstrap.Modal(document.getElementById('deleteModal')).show();
    }
</script>
<jsp:include page="../common/footer.jsp"/>
</body>
</html>
