<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
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
    <style>
        .template-content {
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 2rem;
            font-family: 'Times New Roman', serif;
            line-height: 1.6;
            white-space: pre-wrap;
            min-height: 400px;
        }
        .template-info {
            background-color: #fff;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 1.5rem;
        }
        .info-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.5rem 0;
            border-bottom: 1px solid #f0f0f0;
        }
        .info-item:last-child {
            border-bottom: none;
        }
        .action-buttons {
            position: sticky;
            top: 1rem;
            z-index: 1000;
        }
    </style>
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
                <div class="d-flex justify-content-between align-items-start mb-4">
                    <div>
                        <h2 class="mb-2">
                            <i class="bi bi-file-earmark-text text-primary me-2"></i>
                            ${template.title}
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
                                    <span class="badge bg-warning fs-6">보관</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-light text-dark fs-6">${template.status}</span>
                                </c:otherwise>
                            </c:choose>
                            <small class="text-muted">
                                생성일: <fmt:formatDate value="${template.createdAt}" pattern="yyyy-MM-dd HH:mm"/>
                            </small>
                            <small class="text-muted">
                                수정일: <fmt:formatDate value="${template.updatedAt}" pattern="yyyy-MM-dd HH:mm"/>
                            </small>
                        </div>
                    </div>
                    <a href="/templates" class="btn btn-outline-secondary">
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
                        <div class="template-content">${template.content}</div>
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
                                <a href="/templates/${template.templateId}/edit" class="btn btn-primary">
                                    <i class="bi bi-pencil me-2"></i>수정
                                </a>

                                <button type="button" class="btn btn-outline-primary" onclick="previewTemplate()">
                                    <i class="bi bi-eye me-2"></i>미리보기
                                </button>

                                <c:if test="${template.status == 'ACTIVE'}">
                                    <a href="/contracts/new?templateId=${template.templateId}" class="btn btn-success">
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
                                            <i class="bi bi-archive me-2"></i>보관
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
                                    <span class="text-muted">${template.templateId}</span>
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
                                            <span class="badge bg-warning">보관</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-light text-dark">${template.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="info-item">
                                    <span class="fw-medium">생성일:</span>
                                    <span class="text-muted">
                                        <fmt:formatDate value="${template.createdAt}" pattern="yyyy-MM-dd HH:mm"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="fw-medium">수정일:</span>
                                    <span class="text-muted">
                                        <fmt:formatDate value="${template.updatedAt}" pattern="yyyy-MM-dd HH:mm"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="fw-medium">내용 길이:</span>
                                    <span class="text-muted">${template.content.length()} 자</span>
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
                    <div class="border rounded p-3" style="background-color: #f8f9fa; min-height: 400px; white-space: pre-wrap; font-family: 'Times New Roman', serif;" id="previewContent">
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
                    <h5 class="modal-title">템플릿 삭제 확인</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <p>정말로 '<strong>${template.title}</strong>' 템플릿을 삭제하시겠습니까?</p>
                    <p class="text-danger">
                        <i class="bi bi-exclamation-triangle me-2"></i>
                        이 작업은 되돌릴 수 없습니다.
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                    <form id="deleteForm" method="post" action="/templates/${template.templateId}/delete" style="display: inline;">
                        <c:if test="${not empty _csrf}">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        </c:if>
                        <button type="submit" class="btn btn-danger">삭제</button>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
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

        function previewTemplate() {
            const content = `${template.content}`;

            // 샘플 데이터로 변수 치환
            let previewContent = content
                .replace(/\{PARTY_A_NAME\}/g, '홍길동')
                .replace(/\{PARTY_A_ADDRESS\}/g, '서울특별시 강남구 테헤란로 123')
                .replace(/\{PARTY_B_NAME\}/g, '김철수')
                .replace(/\{PARTY_B_ADDRESS\}/g, '서울특별시 서초구 서초대로 456')
                .replace(/\{CONTRACT_TITLE\}/g, '${template.title}')
                .replace(/\{CONTRACT_DATE\}/g, new Date().toLocaleDateString('ko-KR'))
                .replace(/\{CONTRACT_AMOUNT\}/g, '1,000,000원')
                .replace(/\{START_DATE\}/g, new Date().toLocaleDateString('ko-KR'))
                .replace(/\{END_DATE\}/g, new Date(Date.now() + 365*24*60*60*1000).toLocaleDateString('ko-KR'))
                .replace(/\{SIGNATURE_A\}/g, '[갑 서명]')
                .replace(/\{SIGNATURE_B\}/g, '[을 서명]')
                .replace(/\{SIGNATURE_DATE\}/g, new Date().toLocaleDateString('ko-KR'));

            document.getElementById('previewContent').textContent = previewContent;
            new bootstrap.Modal(document.getElementById('previewModal')).show();
        }

        function activateTemplate() {
            if (confirm('템플릿을 활성화하시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/templates/${template.templateId}/activate';
                appendCsrfField(form);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function archiveTemplate() {
            if (confirm('템플릿을 보관하시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/templates/${template.templateId}/archive';
                appendCsrfField(form);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function deleteTemplate() {
            new bootstrap.Modal(document.getElementById('deleteModal')).show();
        }
    </script>
</body>
</html>
