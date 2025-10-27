<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<c:set var="additionalCss" value="/css/templates.css" />
<jsp:include page="../common/header.jsp" />
<body>
    <c:set var="currentPage" value="templates" />
    <jsp:include page="../common/navbar.jsp" />

    <div class="container mt-4">
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

                <!-- 필터 -->
                <div class="card mb-4">
                    <div class="card-body">
                        <form method="get" class="row g-3">
                            <div class="col-md-4">
                                <label for="status" class="form-label">상태 필터</label>
                                <select class="form-select" id="status" name="status" onchange="this.form.submit()">
                                    <option value="">전체</option>
                                    <c:forEach var="status" items="${statuses}">
                                        <option value="${status}"
                                                <c:if test="${currentStatus == status}">selected</c:if>>
                                            <c:choose>
                                                <c:when test="${status == 'DRAFT'}">초안</c:when>
                                                <c:when test="${status == 'ACTIVE'}">활성</c:when>
                                                <c:when test="${status == 'ARCHIVED'}">보관</c:when>
                                                <c:otherwise>${status}</c:otherwise>
                                            </c:choose>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- 템플릿 목록 -->
                <div class="card">
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${empty templates.content}">
                                <div class="text-center py-5">
                                    <i class="bi bi-file-earmark-text display-1 text-muted"></i>
                                    <h4 class="mt-3 text-muted">등록된 템플릿이 없습니다</h4>
                                    <p class="text-muted">새로운 계약서 템플릿을 생성해보세요.</p>
                                    <a href="/templates/new" class="btn btn-primary">
                                        <i class="bi bi-plus-circle me-2"></i>첫 번째 템플릿 생성
                                    </a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-hover">
                                        <thead class="table-light">
                                            <tr>
                                                <th>제목</th>
                                                <th>상태</th>
                                                <th>생성일</th>
                                                <th>수정일</th>
                                                <th class="text-center">작업</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="template" items="${templates.content}">
                                                <tr>
                                                    <td>
                                                        <a href="/templates/${template.templateId}" class="text-decoration-none">
                                                            <strong>${template.title}</strong>
                                                        </a>
                                                        <c:if test="${not empty template.previewText}">
                                                            <br>
                                                            <small class="text-muted preview-text" data-html="${fn:escapeXml(template.previewText)}">
                                                            </small>
                                                        </c:if>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${template.status == 'DRAFT'}">
                                                                <span class="badge bg-secondary">초안</span>
                                                            </c:when>
                                                            <c:when test="${template.status == 'ACTIVE'}">
                                                                <span class="badge bg-success">활성</span>
                                                            </c:when>
                                                            <c:when test="${template.status == 'ARCHIVED'}">
                                                                <span class="badge bg-warning">보관</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge bg-light text-dark">${template.status}</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <fmt:formatDate value="${template.createdAtDate}" pattern="yyyy-MM-dd HH:mm"/>
                                                    </td>
                                                    <td>
                                                        <fmt:formatDate value="${template.updatedAtDate}" pattern="yyyy-MM-dd HH:mm"/>
                                                    </td>
                                                    <td class="text-center">
                                                        <div class="btn-group btn-group-sm" role="group">
                                                            <button type="button"
                                                                    class="btn btn-outline-info"
                                                                    data-template-id="${template.templateId}"
                                                                    data-template-title="${fn:escapeXml(template.title)}"
                                                                    data-template-html="${fn:escapeXml(template.renderedHtml)}"
                                                                    onclick="previewTemplateButton(this)"
                                                                    title="미리보기">
                                                                <i class="bi bi-eye"></i>
                                                            </button>
                                                            <a href="/templates/${template.templateId}"
                                                               class="btn btn-outline-primary"
                                                               title="상세보기">
                                                                <i class="bi bi-box-arrow-up-right"></i>
                                                            </a>
                                                            <a href="/templates/${template.templateId}/edit"
                                                               class="btn btn-outline-secondary"
                                                               title="수정">
                                                                <i class="bi bi-pencil"></i>
                                                            </a>
                                                            <c:if test="${template.status == 'DRAFT'}">
                                                                <form method="post" action="/templates/${template.templateId}/activate" class="d-inline">
                                                                    <c:if test="${not empty _csrf}">
                                                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                                    </c:if>
                                                                    <button type="submit"
                                                                            class="btn btn-outline-success"
                                                                            onclick="return confirm('템플릿을 활성화하시겠습니까?');"
                                                                            title="활성화">
                                                                        <i class="bi bi-check-circle"></i>
                                                                    </button>
                                                                </form>
                                                            </c:if>
                                                            <c:if test="${template.status == 'ACTIVE'}">
                                                                <form method="post" action="/templates/${template.templateId}/archive" class="d-inline">
                                                                    <c:if test="${not empty _csrf}">
                                                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                                    </c:if>
                                                                    <button type="submit"
                                                                            class="btn btn-outline-warning"
                                                                            onclick="return confirm('템플릿을 보관하시겠습니까?');"
                                                                            title="보관">
                                                                        <i class="bi bi-archive"></i>
                                                                    </button>
                                                                </form>
                                                            </c:if>
                                                            <c:if test="${template.status == 'DRAFT'}">
                                                                <button type="button"
                                                                        class="btn btn-outline-danger"
                                                                        onclick="deleteTemplate('${template.templateId}', '${template.title}')"
                                                                        title="삭제">
                                                                    <i class="bi bi-trash"></i>
                                                                </button>
                                                            </c:if>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>

                                <!-- 페이지네이션 -->
                                <c:if test="${templates.totalPages > 1}">
                                    <nav aria-label="템플릿 목록 페이지네이션" class="mt-4">
                                        <ul class="pagination justify-content-center">
                                            <c:if test="${templates.hasPrevious()}">
                                                <li class="page-item">
                                                    <a class="page-link" href="?page=${templates.number - 1}&status=${param.status}">
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
                                                    <a class="page-link" href="?page=${templates.number + 1}&status=${param.status}">
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
    </div>

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
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
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
        document.addEventListener('DOMContentLoaded', function() {
            document.querySelectorAll('.preview-text').forEach(function(el) {
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
            decoded = decoded.replace(/<span class="template-variable"[^>]*>[\s\S]*?<\/span>/g, '<span class="template-variable-underline"></span>');
            // [VARIABLE_NAME] 형식도 밑줄로 변환
            decoded = decoded.replace(/\[[\w_]+\]/g, '<span class="template-variable-underline"></span>');

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

        const csrfParam = '${_csrf.parameterName}';
        const csrfToken = '${_csrf.token}';

        function submitPost(action) {
            const form = document.createElement('form');
            form.method = 'post';
            form.action = action;

            if (csrfParam && csrfToken) {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = csrfParam;
                input.value = csrfToken;
                form.appendChild(input);
            }

            document.body.appendChild(form);
            form.submit();
        }

        function activateTemplate(templateId) {
            if (confirm('템플릿을 활성화하시겠습니까?')) {
                submitPost('/templates/' + templateId + '/activate');
            }
        }

        function archiveTemplate(templateId) {
            if (confirm('템플릿을 보관하시겠습니까?')) {
                submitPost('/templates/' + templateId + '/archive');
            }
        }

        function deleteTemplate(templateId, templateName) {
            document.getElementById('deleteTemplateName').textContent = templateName;
            document.getElementById('deleteForm').action = '/templates/' + templateId + '/delete';
            new bootstrap.Modal(document.getElementById('deleteModal')).show();
        }
    </script>
    <jsp:include page="../common/footer.jsp" />
</body>
</html>
