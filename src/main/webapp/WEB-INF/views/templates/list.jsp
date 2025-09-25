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
    <link href="/css/templates.css" rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container">
            <a class="navbar-brand" href="/home">
                <i class="bi bi-file-earmark-text me-2"></i>Signly
            </a>
            <div class="navbar-nav ms-auto">
                <a class="nav-link" href="/home">대시보드</a>
                <a class="nav-link active" href="/templates">템플릿</a>
                <a class="nav-link" href="/contracts">계약서</a>
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
                                                        <c:if test="${not empty template.content}">
                                                            <br>
                                                            <small class="text-muted">
                                                                ${template.content.length() > 100 ?
                                                                  template.content.substring(0, 100).concat('...') :
                                                                  template.content}
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
                                                        <fmt:formatDate value="${template.createdAt}" pattern="yyyy-MM-dd HH:mm"/>
                                                    </td>
                                                    <td>
                                                        <fmt:formatDate value="${template.updatedAt}" pattern="yyyy-MM-dd HH:mm"/>
                                                    </td>
                                                    <td class="text-center">
                                                        <div class="btn-group btn-group-sm" role="group">
                                                            <button type="button"
                                                                    class="btn btn-outline-info"
                                                                    data-template-id="${template.templateId}"
                                                                    data-template-title="${fn:escapeXml(template.title)}"
                                                                    data-template-content="${fn:escapeXml(template.content)}"
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
                                                                <button type="button"
                                                                        class="btn btn-outline-success"
                                                                        onclick="activateTemplate('${template.templateId}')"
                                                                        title="활성화">
                                                                    <i class="bi bi-check-circle"></i>
                                                                </button>
                                                            </c:if>
                                                            <c:if test="${template.status == 'ACTIVE'}">
                                                                <button type="button"
                                                                        class="btn btn-outline-warning"
                                                                        onclick="archiveTemplate('${template.templateId}')"
                                                                        title="보관">
                                                                    <i class="bi bi-archive"></i>
                                                                </button>
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
                    <form id="deleteForm" method="post" style="display: inline;">
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
                         class="border rounded p-4"
                         style="background-color:#f8f9fa; min-height:400px; white-space:pre-wrap; font-family:'Times New Roman', serif;"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function previewTemplateButton(button) {
            const templateId = button.getAttribute('data-template-id');
            const title = button.getAttribute('data-template-title') || '템플릿 미리보기';
            const encodedContent = button.getAttribute('data-template-content') || '';
            previewTemplateModal(templateId, title, encodedContent);
        }

        function previewTemplateModal(templateId, title, encodedContent) {
            const rawContent = decodeHtml(encodedContent).trim();
            const previewContent = rawContent
                ? buildPreviewContent(rawContent, title)
                : '템플릿 내용이 비어있습니다.';

            document.getElementById('previewModalTitle').textContent = title;
            document.getElementById('previewContent').textContent = previewContent;
            new bootstrap.Modal(document.getElementById('previewModal')).show();
        }

        function buildPreviewContent(content, title) {
            return content
                .replace(/\{PARTY_A_NAME\}/g, '홍길동')
                .replace(/\{PARTY_A_EMAIL\}/g, 'hong@example.com')
                .replace(/\{PARTY_A_ADDRESS\}/g, '서울특별시 강남구 테헤란로 123')
                .replace(/\{PARTY_B_NAME\}/g, '김철수')
                .replace(/\{PARTY_B_EMAIL\}/g, 'kim@example.com')
                .replace(/\{PARTY_B_ADDRESS\}/g, '서울특별시 서초구 서초대로 456')
                .replace(/\{CONTRACT_TITLE\}/g, title || '샘플 계약서')
                .replace(/\{CONTRACT_DATE\}/g, new Date().toLocaleDateString('ko-KR'))
                .replace(/\{CONTRACT_AMOUNT\}/g, '1,000,000원')
                .replace(/\{START_DATE\}/g, new Date().toLocaleDateString('ko-KR'))
                .replace(/\{END_DATE\}/g, new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toLocaleDateString('ko-KR'))
                .replace(/\{SIGNATURE_A\}/g, '[갑 서명]')
                .replace(/\{SIGNATURE_B\}/g, '[을 서명]')
                .replace(/\{SIGNATURE_DATE\}/g, new Date().toLocaleDateString('ko-KR'));
        }

        function decodeHtml(value) {
            const textarea = document.createElement('textarea');
            textarea.innerHTML = value;
            return textarea.value;
        }

        const csrfParam = '${_csrf.parameterName}';
        const csrfToken = '${_csrf.token}';

        function appendCsrfField(form) {
            if (!csrfParam || !csrfToken) {
                return;
            }
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = csrfParam;
            input.value = csrfToken;
            form.appendChild(input);
        }

        function activateTemplate(templateId) {
            if (confirm('템플릿을 활성화하시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/templates/' + templateId + '/activate';
                appendCsrfField(form);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function archiveTemplate(templateId) {
            if (confirm('템플릿을 보관하시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/templates/' + templateId + '/archive';
                appendCsrfField(form);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function deleteTemplate(templateId, templateName) {
            document.getElementById('deleteTemplateName').textContent = templateName;
            document.getElementById('deleteForm').action = '/templates/' + templateId + '/delete';
            new bootstrap.Modal(document.getElementById('deleteModal')).show();
        }
    </script>
</body>
</html>
