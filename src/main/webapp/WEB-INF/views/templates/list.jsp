<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<jsp:include page="../common/header.jsp">
    <jsp:param name="additionalCss" value="/css/contract-template-base.css"/>
    <jsp:param name="additionalCss2" value="/css/contract-template-preview.css"/>
    <jsp:param name="additionalCss3" value="/css/contracts.css"/>
    <jsp:param name="additionalCss4" value="/css/templates.css"/>
    <jsp:param name="additionalCss5" value="/css/modal.css"/>
</jsp:include>
<body>
<jsp:include page="../common/navbar.jsp">
    <jsp:param name="currentPage" value="templates"/>
</jsp:include>

<div class="container mt-4">
    <div class="main-content-card">
        <div class="row">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h2 class="mb-2">
                            <i class="bi bi-file-earmark-text text-primary me-2"></i>
                            템플릿 관리
                        </h2>
                        <p class="text-muted mb-0">계약서 템플릿을 생성하고 관리하세요</p>
                    </div>
                    <a href="/templates/new" class="btn btn-primary">
                        <i class="bi bi-plus-circle me-2"></i>새 템플릿 생성
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

                <!-- 상태 필터 탭 -->
                <jsp:include page="../common/status-filter-tabs.jsp">
                    <jsp:param name="filterType" value="template"/>
                    <jsp:param name="currentStatus" value="${currentStatus}"/>
                </jsp:include>

                <!-- 템플릿 목록 -->
                <c:choose>
                    <c:when test="${empty templates.content}">
                        <div class="card">
                            <div class="card-body">
                                <div class="text-center py-5">
                                    <i class="bi bi-file-earmark-text display-1 text-muted"></i>
                                    <h4 class="mt-3 text-muted">등록된 템플릿이 없습니다</h4>
                                    <p class="text-muted">새로운 계약서 템플릿을 생성해보세요.</p>
                                    <a href="/templates/new" class="btn btn-primary">
                                        <i class="bi bi-plus-circle me-2"></i>첫 번째 템플릿 생성
                                    </a>
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="row g-4">
                            <c:forEach var="template" items="${templates.content}">
                                <div class="col-md-6 col-lg-3">
                                    <div class="template-card">
                                        <div class="template-card-body">
                                            <div class="template-preview preset-document"
                                                 data-template-id="<c:out value='${template.templateId}'/>">
                                                <c:choose>
                                                    <c:when test="${not empty template.renderedHtml}">
                                                        <c:out value="${template.renderedHtml}" escapeXml="false"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <p class="text-muted small">미리보기 없음</p>
                                                    </c:otherwise>
                                                </c:choose>
                                                <div class="template-status-badge">
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
                                                    </c:choose>
                                                </div>
                                            </div>
                                            <h5 class="template-title"><c:out value="${template.title}"/></h5>

                                            <div class="template-footer mt-auto">
                                                <div class="template-meta-info mb-2">
                                                    <small class="text-muted d-block mb-1">
                                                        <i class="bi bi-calendar-plus me-1"></i>
                                                        등록일: <fmt:formatDate value="${template.createdAtDate}"
                                                                             pattern="yyyy-MM-dd"/>
                                                    </small>
                                                    <small class="text-muted d-block">
                                                        <i class="bi bi-pencil-square me-1"></i>
                                                        수정일: <fmt:formatDate value="${template.updatedAtDate}"
                                                                             pattern="yyyy-MM-dd"/>
                                                    </small>
                                                </div>

                                                <div class="template-actions d-flex gap-1 justify-content-center">
                                                    <button type="button"
                                                            class="btn btn-sm btn-outline-info"
                                                            data-template-id="<c:out value='${template.templateId}'/>"
                                                            data-template-title="<c:out value='${fn:escapeXml(template.title)}'/>"
                                                            data-template-html="<c:out value='${fn:escapeXml(template.renderedHtml)}'/>"
                                                            onclick="previewTemplateButton(this)"
                                                            title="미리보기">
                                                        <i class="bi bi-eye"></i>
                                                    </button>
                                                    <a href="/templates/<c:out value='${template.templateId}'/>"
                                                       class="btn btn-sm btn-outline-primary"
                                                       title="상세보기">
                                                        <i class="bi bi-box-arrow-up-right"></i>
                                                    </a>
                                                    <a href="/templates/<c:out value='${template.templateId}'/>/edit"
                                                       class="btn btn-sm btn-outline-secondary"
                                                       title="수정">
                                                        <i class="bi bi-pencil"></i>
                                                    </a>
                                                    <c:if test="${template.status == 'DRAFT'}">
                                                        <button type="button"
                                                                class="btn btn-sm btn-outline-success"
                                                                onclick="activateTemplate('<c:out
                                                                        value='${template.templateId}'/>')"
                                                                title="활성화">
                                                            <i class="bi bi-check-circle"></i>
                                                        </button>
                                                    </c:if>
                                                    <c:if test="${template.status == 'ACTIVE'}">
                                                        <button type="button"
                                                                class="btn btn-sm btn-outline-warning"
                                                                onclick="archiveTemplate('<c:out
                                                                        value='${template.templateId}'/>')"
                                                                title="보류">
                                                            <i class="bi bi-archive"></i>
                                                        </button>
                                                    </c:if>
                                                    <c:if test="${template.status == 'ARCHIVED'}">
                                                        <button type="button"
                                                                class="btn btn-sm btn-outline-danger"
                                                                onclick="deleteTemplate('<c:out
                                                                        value='${template.templateId}'/>', '<c:out
                                                                        value='${template.title}'/>')"
                                                                title="삭제">
                                                            <i class="bi bi-trash"></i>
                                                        </button>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <!-- 페이지네이션 -->
                        <c:if test="${templates.totalPages > 1}">
                            <nav aria-label="템플릿 목록 페이지네이션" class="mt-4">
                                <ul class="pagination justify-content-center">
                                    <c:if test="${templates.hasPrevious()}">
                                        <li class="page-item">
                                            <a class="page-link"
                                               href="?page=${templates.number - 1}&status=${param.status}">
                                                <i class="bi bi-chevron-left"></i>
                                            </a>
                                        </li>
                                    </c:if>

                                    <c:forEach begin="0" end="${templates.totalPages - 1}" var="i">
                                        <li class="page-item ${i == templates.number ? 'active' : ''}">
                                            <a class="page-link" href="?page=${i}&status=${param.status}">${i + 1}</a>
                                        </li>
                                    </c:forEach>

                                    <c:if test="${templates.hasNext()}">
                                        <li class="page-item">
                                            <a class="page-link"
                                               href="?page=${templates.number + 1}&status=${param.status}">
                                                <i class="bi bi-chevron-right"></i>
                                            </a>
                                        </li>
                                    </c:if>
                                </ul>
                            </nav>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<!-- Hidden action form -->
<form id="templateActionForm" method="post" class="d-none">
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
                <p>정말로 '<span id="deleteTemplateName"></span>' 템플릿을 삭제하시겠습니까?</p>
                <p class="text-danger">
                    <i class="bi bi-exclamation-triangle me-2"></i>
                    이 작업은 되돌릴 수 없습니다.
                </p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                <form id="deleteForm" method="post" class="d-inline">
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

<!-- 미리보기 모달 -->
<div class="modal fade" id="previewModal" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="previewModalTitle">템플릿 미리보기</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="alert alert-info">
                    <i class="bi bi-info-circle me-2"></i>
                    현재 템플릿에 입력된 내용을 기준으로 샘플 데이터를 이용해 미리보기를 제공합니다.
                </div>
                <div id="previewContent"
                     class="border rounded p-4 template-preview-content"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>

<script>
    // HTML 태그 제거 및 텍스트만 추출
    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.preview-text').forEach(function (el) {
            const html = el.getAttribute('data-html');
            if (html) {
                const temp = document.createElement('div');
                temp.innerHTML = html;
                // 이중 인코딩된 경우 한번 더 디코딩
                let decoded = temp.textContent || temp.innerText || '';
                if (decoded.includes('&lt;') || decoded.includes('&gt;')) {
                    temp.innerHTML = decoded;
                    decoded = temp.textContent || temp.innerText || '';
                }
                let text = decoded.trim().replace(/\s+/g, ' ');
                if (text.length > 120) {
                    text = text.substring(0, 120) + '...';
                }
                el.textContent = text;
            }
        });

        const deleteForm = document.getElementById('deleteForm');
        if (deleteForm) {
            deleteForm.addEventListener('submit', function () {
                ensureCsrf(deleteForm);
            });
        }
    });

    function previewTemplateButton(button) {
        const templateId = button.getAttribute('data-template-id');
        const title = button.getAttribute('data-template-title') || '템플릿 미리보기';
        const htmlContent = button.getAttribute('data-template-html') || '';
        previewTemplateModal(templateId, title, htmlContent);
    }

    function previewTemplateModal(templateId, title, htmlContent) {
        let decoded = decodeHtml(htmlContent).trim();

        // 변수를 밑줄로 변환
        decoded = decoded.replace(/<span class="template-variable"[^>]*>[\s\S]*?<\/span>/g, '<span class="blank-line"></span>');
        // [VARIABLE_NAME] 형식도 밑줄로 변환
        decoded = decoded.replace(/\[[\w_]+\]/g, '<span class="blank-line"></span>');

        const previewContent = decoded ? decoded : '<p class="text-muted">템플릿 내용이 비어있습니다.</p>';

        document.getElementById('previewModalTitle').textContent = title;
        document.getElementById('previewContent').innerHTML = previewContent;
        new bootstrap.Modal(document.getElementById('previewModal')).show();
    }

    function decodeHtml(value) {
        const textarea = document.createElement('textarea');
        textarea.innerHTML = value;
        return textarea.value;
    }

    // 전역 변수로 선언
    window.csrfParam = '<c:out value="${_csrf.parameterName}" default="_csrf"/>';
    window.csrfToken = '<c:out value="${_csrf.token}" default=""/>';

    function readCsrfFromCookie() {
        const match = document.cookie.split(';').map(c => c.trim()).find(c => c.startsWith('XSRF-TOKEN='));
        return match ? decodeURIComponent(match.substring('XSRF-TOKEN='.length)) : '';
    }

    function ensureCsrf(form) {
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
            const existing = document.querySelector('input[name="_csrf"]');
            if (existing) {
                let input = form.querySelector('input[name="_csrf"]');
                if (!input) {
                    input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = '_csrf';
                    form.appendChild(input);
                }
                input.value = existing.value;
            }
        }
    }

    function submitPost(action) {
        const form = document.getElementById('templateActionForm');
        if (!form) {
            console.warn('templateActionForm not found');
            return;
        }
        form.action = action;
        ensureCsrf(form);
        form.submit();
    }

    function activateTemplate(templateId) {
        showConfirmModal(
            '템플릿을 활성화하시겠습니까?',
            function () {
                submitPost('/templates/' + templateId + '/activate');
            },
            '활성화',
            '취소',
            'btn-success'
        );
    }

    function archiveTemplate(templateId) {
        showConfirmModal(
            '템플릿을 보류하시겠습니까?',
            function () {
                submitPost('/templates/' + templateId + '/archive');
            },
            '보류',
            '취소',
            'btn-warning'
        );
    }

    function deleteTemplate(templateId, templateName) {
        document.getElementById('deleteTemplateName').textContent = templateName;
        document.getElementById('deleteForm').action = '/templates/' + templateId + '/delete';
        new bootstrap.Modal(document.getElementById('deleteModal')).show();
    }
</script>
<jsp:include page="../common/footer.jsp"/>
</body>
</html>
