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
                            <i class="bi bi-file-earmark-check text-primary me-2"></i>
                            계약서 관리
                        </h2>
                        <p class="text-muted mb-0">계약서를 생성하고 서명을 관리하세요</p>
                    </div>
                    <a href="/contracts/new" class="btn btn-primary">
                        <i class="bi bi-plus-circle me-2"></i>새 계약서 생성
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
                                                <c:when test="${status == 'PENDING'}">서명 대기</c:when>
                                                <c:when test="${status == 'SIGNED'}">서명 완료</c:when>
                                                <c:when test="${status == 'COMPLETED'}">완료</c:when>
                                                <c:when test="${status == 'CANCELLED'}">취소</c:when>
                                                <c:when test="${status == 'EXPIRED'}">만료</c:when>
                                                <c:otherwise>${status}</c:otherwise>
                                            </c:choose>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- 계약서 목록 -->
                <div class="card">
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${empty contracts.content}">
                                <div class="text-center py-5">
                                    <i class="bi bi-file-earmark-check display-1 text-muted"></i>
                                    <h4 class="mt-3 text-muted">등록된 계약서가 없습니다</h4>
                                    <p class="text-muted">새로운 계약서를 생성해보세요.</p>
                                    <a href="/contracts/new" class="btn btn-primary">
                                        <i class="bi bi-plus-circle me-2"></i>첫 번째 계약서 생성
                                    </a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-hover">
                                        <thead class="table-light">
                                            <tr>
                                                <th>제목</th>
                                                <th>당사자</th>
                                                <th>상태</th>
                                                <th>생성일</th>
                                                <th>만료일</th>
                                                <th class="text-center">작업</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="contract" items="${contracts.content}">
                                                <tr>
                                                    <td>
                                                        <a href="/contracts/${contract.id}" class="text-decoration-none">
                                                            <strong>${contract.title}</strong>
                                                        </a>
                                                        <c:if test="${not empty contract.content and contract.content.length() > 100}">
                                                            <br>
                                                            <small class="text-muted">
                                                                ${contract.content.substring(0, 100)}...
                                                            </small>
                                                        </c:if>
                                                    </td>
                                                    <td>
                                                        <div class="small">
                                                            <strong>갑:</strong> ${contract.firstParty.name}<br>
                                                            <strong>을:</strong> ${contract.secondParty.name}
                                                        </div>
                                                    </td>
                                                    <td>
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
                                                    </td>
                                                    <td>
                                                        <fmt:formatDate value="${contract.createdAt}" pattern="yyyy-MM-dd HH:mm"/>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty contract.expiresAt}">
                                                                <fmt:formatDate value="${contract.expiresAt}" pattern="yyyy-MM-dd HH:mm"/>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-muted">없음</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="text-center">
                                                        <div class="btn-group btn-group-sm" role="group">
                                                            <a href="/contracts/${contract.id}"
                                                               class="btn btn-outline-primary"
                                                               title="상세보기">
                                                                <i class="bi bi-eye"></i>
                                                            </a>
                                                            <c:if test="${contract.status == 'DRAFT'}">
                                                                <a href="/contracts/${contract.id}/edit"
                                                                   class="btn btn-outline-secondary"
                                                                   title="수정">
                                                                    <i class="bi bi-pencil"></i>
                                                                </a>
                                                                <button type="button"
                                                                        class="btn btn-outline-success"
                                                                        onclick="sendForSigning('${contract.id}')"
                                                                        title="서명 요청">
                                                                    <i class="bi bi-send"></i>
                                                                </button>
                                                                <button type="button"
                                                                        class="btn btn-outline-danger"
                                                                        onclick="deleteContract('${contract.id}', '${contract.title}')"
                                                                        title="삭제">
                                                                    <i class="bi bi-trash"></i>
                                                                </button>
                                                            </c:if>
                                                            <c:if test="${contract.status == 'PENDING' or contract.status == 'SIGNED'}">
                                                                <button type="button"
                                                                        class="btn btn-outline-warning"
                                                                        onclick="cancelContract('${contract.id}')"
                                                                        title="취소">
                                                                    <i class="bi bi-x-circle"></i>
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
                                <c:if test="${contracts.totalPages > 1}">
                                    <nav aria-label="계약서 목록 페이지네이션" class="mt-4">
                                        <ul class="pagination justify-content-center">
                                            <c:if test="${contracts.hasPrevious()}">
                                                <li class="page-item">
                                                    <a class="page-link" href="?page=${contracts.number - 1}&status=${param.status}">
                                                        <i class="bi bi-chevron-left"></i>
                                                    </a>
                                                </li>
                                            </c:if>

                                            <c:forEach begin="0" end="${contracts.totalPages - 1}" var="i">
                                                <li class="page-item ${i == contracts.number ? 'active' : ''}">
                                                    <a class="page-link" href="?page=${i}&status=${param.status}">${i + 1}</a>
                                                </li>
                                            </c:forEach>

                                            <c:if test="${contracts.hasNext()}">
                                                <li class="page-item">
                                                    <a class="page-link" href="?page=${contracts.number + 1}&status=${param.status}">
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
                    <h5 class="modal-title">계약서 삭제 확인</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <p>정말로 '<span id="deleteContractName"></span>' 계약서를 삭제하시겠습니까?</p>
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

    <!-- 취소 확인 모달 -->
    <div class="modal fade" id="cancelModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">계약서 취소 확인</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <p>정말로 계약서를 취소하시겠습니까?</p>
                    <p class="text-warning">
                        <i class="bi bi-exclamation-triangle me-2"></i>
                        취소된 계약서는 더 이상 서명할 수 없습니다.
                    </p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">돌아가기</button>
                    <form id="cancelForm" method="post" class="d-inline">
                        <c:if test="${not empty _csrf}">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        </c:if>
                        <button type="submit" class="btn btn-warning">취소</button>
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

        function sendForSigning(contractId) {
            if (confirm('계약서 서명 요청을 보내시겠습니까?')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '/contracts/' + contractId + '/send';
                appendCsrfField(form);
                document.body.appendChild(form);
                form.submit();
            }
        }

        function cancelContract(contractId) {
            document.getElementById('cancelForm').action = '/contracts/' + contractId + '/cancel';
            new bootstrap.Modal(document.getElementById('cancelModal')).show();
        }

        function deleteContract(contractId, contractTitle) {
            document.getElementById('deleteContractName').textContent = contractTitle;
            document.getElementById('deleteForm').action = '/contracts/' + contractId + '/delete';
            new bootstrap.Modal(document.getElementById('deleteModal')).show();
        }
    </script>
</body>
</html>
