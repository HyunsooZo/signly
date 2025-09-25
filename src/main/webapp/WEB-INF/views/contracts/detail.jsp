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
    <link href="/css/contracts.css" rel="stylesheet">
    <style>
        .contract-content {
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 2rem;
            font-family: 'Times New Roman', serif;
            line-height: 1.6;
            white-space: pre-wrap;
            min-height: 400px;
        }
        .contract-info {
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
        .signature-info {
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 1rem;
            margin-bottom: 1rem;
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
                <a class="nav-link" href="/templates">템플릿</a>
                <a class="nav-link active" href="/contracts">계약서</a>
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
                            <i class="bi bi-file-earmark-check text-primary me-2"></i>
                            ${contract.title}
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
                                <c:when test="${contract.status == 'COMPLETED'}">
                                    <span class="badge bg-primary fs-6">완료</span>
                                </c:when>
                                <c:when test="${contract.status == 'CANCELLED'}">
                                    <span class="badge bg-danger fs-6">취소</span>
                                </c:when>
                                <c:when test="${contract.status == 'EXPIRED'}">
                                    <span class="badge bg-dark fs-6">만료</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-light text-dark fs-6">${contract.status}</span>
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
            <!-- 계약서 내용 -->
            <div class="col-lg-8">
                <div class="card">
                    <div class="card-header">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-file-earmark-text me-2"></i>계약서 내용
                        </h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="contract-content">${contract.content}</div>
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
                                            <h6 class="mb-1">${signature.signerName}</h6>
                                            <p class="text-muted mb-1">${signature.signerEmail}</p>
                                            <small class="text-muted">
                                                서명일시: <fmt:formatDate value="${signature.signedAt}" pattern="yyyy-MM-dd HH:mm:ss"/>
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
                                <c:if test="${contract.status == 'DRAFT'}">
                                    <a href="/contracts/${contract.contractId}/edit" class="btn btn-primary">
                                        <i class="bi bi-pencil me-2"></i>수정
                                    </a>
                                    <button type="button" class="btn btn-success" onclick="sendForSigning()">
                                        <i class="bi bi-send me-2"></i>서명 요청 전송
                                    </button>
                                    <hr>
                                    <button type="button" class="btn btn-outline-danger" onclick="deleteContract()">
                                        <i class="bi bi-trash me-2"></i>삭제
                                    </button>
                                </c:if>

                                <c:if test="${contract.status == 'PENDING' or contract.status == 'SIGNED'}">
                                    <button type="button" class="btn btn-outline-warning" onclick="cancelContract()">
                                        <i class="bi bi-x-circle me-2"></i>계약 취소
                                    </button>
                                </c:if>

                                <button type="button" class="btn btn-outline-primary" onclick="previewContract()">
                                    <i class="bi bi-eye me-2"></i>미리보기
                                </button>

                                <c:if test="${contract.status == 'SIGNED'}">
                                    <button type="button" class="btn btn-primary" onclick="completeContract()">
                                        <i class="bi bi-check-circle me-2"></i>계약 완료
                                    </button>
                                </c:if>

                                <c:if test="${contract.status == 'PENDING' or contract.status == 'SIGNED'}">
                                    <a href="/sign/${contract.contractId}" class="btn btn-outline-success" target="_blank">
                                        <i class="bi bi-pen me-2"></i>서명 페이지 보기
                                    </a>
                                </c:if>
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
                                    <span class="text-muted">${contract.contractId}</span>
                                </div>
                                <div class="info-item">
                                    <span class="fw-medium">템플릿 ID:</span>
                                    <span class="text-muted">${contract.templateId}</span>
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
                                        <c:when test="${contract.status == 'COMPLETED'}">
                                            <span class="badge bg-primary">완료</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'CANCELLED'}">
                                            <span class="badge bg-danger">취소</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'EXPIRED'}">
                                            <span class="badge bg-dark">만료</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-light text-dark">${contract.status}</span>
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
                                        <span class="text-muted">${contract.firstParty.name}</span>
                                    </div>
                                    <div class="info-item">
                                        <span class="fw-medium">이메일:</span>
                                        <span class="text-muted">${contract.firstParty.email}</span>
                                    </div>
                                    <c:if test="${not empty contract.firstParty.address}">
                                        <div class="info-item">
                                            <span class="fw-medium">주소:</span>
                                            <span class="text-muted">${contract.firstParty.address}</span>
                                        </div>
                                    </c:if>
                                </div>
                                <div>
                                    <h6 class="text-success mb-2">을 (두 번째 당사자)</h6>
                                    <div class="info-item">
                                        <span class="fw-medium">이름:</span>
                                        <span class="text-muted">${contract.secondParty.name}</span>
                                    </div>
                                    <div class="info-item">
                                        <span class="fw-medium">이메일:</span>
                                        <span class="text-muted">${contract.secondParty.email}</span>
                                    </div>
                                    <c:if test="${not empty contract.secondParty.address}">
                                        <div class="info-item">
                                            <span class="fw-medium">주소:</span>
                                            <span class="text-muted">${contract.secondParty.address}</span>
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

    <!-- 미리보기 모달 -->
    <div class="modal fade" id="previewModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
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
                        아래는 현재 계약서 내용의 미리보기입니다.
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
                    <h5 class="modal-title">계약서 삭제 확인</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <p>정말로 '<strong>${contract.title}</strong>' 계약서를 삭제하시겠습니까?</p>
                    <p class="text-danger">
                        <i class="bi bi-exclamation-triangle me-2"></i>
                        이 작업은 되돌릴 수 없습니다.
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                    <form id="deleteForm" method="post" action="/contracts/${contract.contractId}/delete" style="display: inline;">
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

        function previewContract() {
            const content = `${contract.content}`;

            // 실제 데이터로 변수 치환
            let previewContent = content
                .replace(/\{FIRST_PARTY_NAME\}/g, '${contract.firstParty.name}')
                .replace(/\{FIRST_PARTY_EMAIL\}/g, '${contract.firstParty.email}')
                .replace(/\{FIRST_PARTY_ADDRESS\}/g, '${contract.firstParty.address}')
                .replace(/\{SECOND_PARTY_NAME\}/g, '${contract.secondParty.name}')
                .replace(/\{SECOND_PARTY_EMAIL\}/g, '${contract.secondParty.email}')
                .replace(/\{SECOND_PARTY_ADDRESS\}/g, '${contract.secondParty.address}')
                .replace(/\{CONTRACT_TITLE\}/g, '${contract.title}')
                .replace(/\{CONTRACT_DATE\}/g, new Date().toLocaleDateString('ko-KR'))
                .replace(/\{SIGNATURE_FIRST\}/g, '[갑 서명]')
                .replace(/\{SIGNATURE_SECOND\}/g, '[을 서명]');

            document.getElementById('previewContent').textContent = previewContent;
            new bootstrap.Modal(document.getElementById('previewModal')).show();
        }

        function sendForSigning() {
            if (confirm('계약서 서명 요청을 전송하시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/contracts/${contract.contractId}/send';
                appendCsrfField(form);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function cancelContract() {
            if (confirm('계약서를 취소하시겠습니까? 취소된 계약서는 더 이상 서명할 수 없습니다.')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/contracts/${contract.contractId}/cancel';
                appendCsrfField(form);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function completeContract() {
            if (confirm('계약을 완료하시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/contracts/${contract.contractId}/complete';
                appendCsrfField(form);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function deleteContract() {
            new bootstrap.Modal(document.getElementById('deleteModal')).show();
        }
    </script>
</body>
</html>
