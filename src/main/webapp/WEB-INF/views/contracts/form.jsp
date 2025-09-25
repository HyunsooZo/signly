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

        <form method="post" action="${not empty contractId ? '/contracts/'.concat(contractId) : '/contracts'}" class="contract-form">
            <c:if test="${not empty _csrf}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            </c:if>
            <div class="row">
                <!-- 계약서 기본 정보 -->
                <div class="col-lg-8">
                    <div class="card mb-4">
                        <div class="card-header">
                            <h5 class="card-title mb-0">
                                <i class="bi bi-file-earmark-text me-2"></i>계약서 기본 정보
                            </h5>
                        </div>
                        <div class="card-body">
                            <!-- 템플릿 선택 (새 계약서인 경우만) -->
                            <c:if test="${empty contractId}">
                                <div class="mb-3">
                                    <label for="templateId" class="form-label">템플릿 선택</label>
                                    <select class="form-select" id="templateId" name="templateId" onchange="loadTemplate()">
                                        <option value="">직접 작성</option>
                                        <c:forEach var="template" items="${templates}">
                                            <option value="${template.templateId}"
                                                    data-title="${template.title}"
                                                    data-content="${template.content}"
                                                    <c:if test="${contract.templateId == template.templateId}">selected</c:if>>
                                                ${template.title}
                                            </option>
                                        </c:forEach>
                                    </select>
                                    <div class="form-text">기존 템플릿을 선택하거나 직접 작성하세요.</div>
                                </div>
                            </c:if>

                            <div class="mb-3">
                                <label for="title" class="form-label">계약서 제목 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control form-control-lg" id="title" name="title"
                                       value="${contract.title}" required maxlength="200"
                                       placeholder="계약서 제목을 입력하세요">
                            </div>

                            <div class="mb-3">
                                <label for="content" class="form-label">계약서 내용 <span class="text-danger">*</span></label>
                                <textarea class="form-control content-editor" id="content" name="content"
                                          rows="15" required placeholder="계약서 내용을 입력하세요...">${contract.content}</textarea>
                                <div class="form-text">계약서의 전체 내용을 입력하세요. 변수를 사용하여 동적 값을 설정할 수 있습니다.</div>
                            </div>

                            <div class="mb-3">
                                <label for="expiresAt" class="form-label">만료일</label>
                                <input type="datetime-local" class="form-control" id="expiresAt" name="expiresAt"
                                       value="<fmt:formatDate value='${contract.expiresAt}' pattern='yyyy-MM-ddTHH:mm'/>">
                                <div class="form-text">계약서의 서명 만료일을 설정하세요 (선택사항).</div>
                            </div>
                        </div>
                    </div>

                    <!-- 변수 가이드 -->
                    <div class="card mb-4">
                        <div class="card-header">
                            <h6 class="card-title mb-0">
                                <i class="bi bi-lightbulb me-2"></i>사용 가능한 변수
                            </h6>
                        </div>
                        <div class="card-body">
                            <div class="contract-variables">
                                <p class="mb-3">아래 변수를 클릭하여 계약서 내용에 삽입할 수 있습니다:</p>
                                <div class="variable-tags">
                                    <span class="variable-tag" onclick="insertVariable('{FIRST_PARTY_NAME}')">{FIRST_PARTY_NAME}</span>
                                    <span class="variable-tag" onclick="insertVariable('{FIRST_PARTY_EMAIL}')">{FIRST_PARTY_EMAIL}</span>
                                    <span class="variable-tag" onclick="insertVariable('{FIRST_PARTY_ADDRESS}')">{FIRST_PARTY_ADDRESS}</span>
                                    <span class="variable-tag" onclick="insertVariable('{SECOND_PARTY_NAME}')">{SECOND_PARTY_NAME}</span>
                                    <span class="variable-tag" onclick="insertVariable('{SECOND_PARTY_EMAIL}')">{SECOND_PARTY_EMAIL}</span>
                                    <span class="variable-tag" onclick="insertVariable('{SECOND_PARTY_ADDRESS}')">{SECOND_PARTY_ADDRESS}</span>
                                    <span class="variable-tag" onclick="insertVariable('{CONTRACT_TITLE}')">{CONTRACT_TITLE}</span>
                                    <span class="variable-tag" onclick="insertVariable('{CONTRACT_DATE}')">{CONTRACT_DATE}</span>
                                    <span class="variable-tag" onclick="insertVariable('{SIGNATURE_FIRST}')">{SIGNATURE_FIRST}</span>
                                    <span class="variable-tag" onclick="insertVariable('{SIGNATURE_SECOND}')">{SIGNATURE_SECOND}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 당사자 정보 -->
                <div class="col-lg-4">
                    <div class="card mb-4">
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
                                       value="${contract.firstPartyName}" required maxlength="100"
                                       placeholder="성명을 입력하세요">
                            </div>
                            <div class="mb-3">
                                <label for="firstPartyEmail" class="form-label">이메일 <span class="text-danger">*</span></label>
                                <input type="email" class="form-control" id="firstPartyEmail" name="firstPartyEmail"
                                       value="${contract.firstPartyEmail}" required maxlength="200"
                                       placeholder="example@domain.com">
                            </div>
                            <div class="mb-4">
                                <label for="firstPartyAddress" class="form-label">회사/조직명</label>
                                <textarea class="form-control" id="firstPartyAddress" name="firstPartyAddress"
                                          rows="3" maxlength="500" placeholder="회사 또는 조직명을 입력하세요">${contract.firstPartyAddress}</textarea>
                            </div>

                            <!-- 을 (두 번째 당사자) -->
                            <h6 class="text-success mb-3">을 (두 번째 당사자)</h6>
                            <div class="mb-3">
                                <label for="secondPartyName" class="form-label">이름 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="secondPartyName" name="secondPartyName"
                                       value="${contract.secondPartyName}" required maxlength="100"
                                       placeholder="성명을 입력하세요">
                            </div>
                            <div class="mb-3">
                                <label for="secondPartyEmail" class="form-label">이메일 <span class="text-danger">*</span></label>
                                <input type="email" class="form-control" id="secondPartyEmail" name="secondPartyEmail"
                                       value="${contract.secondPartyEmail}" required maxlength="200"
                                       placeholder="example@domain.com">
                            </div>
                            <div class="mb-4">
                                <label for="secondPartyAddress" class="form-label">회사/조직명</label>
                                <textarea class="form-control" id="secondPartyAddress" name="secondPartyAddress"
                                          rows="3" maxlength="500" placeholder="회사 또는 조직명을 입력하세요">${contract.secondPartyAddress}</textarea>
                            </div>

                            <!-- 액션 버튼 -->
                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-check-circle me-2"></i>
                                    ${empty contractId ? '계약서 생성' : '수정 완료'}
                                </button>
                                <button type="button" class="btn btn-outline-primary" onclick="previewContract()">
                                    <i class="bi bi-eye me-2"></i>미리보기
                                </button>
                                <a href="/contracts" class="btn btn-outline-secondary">
                                    <i class="bi bi-x me-2"></i>취소
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </form>
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
                    <div class="contract-preview border rounded p-4" style="background-color: #f8f9fa; min-height: 400px; white-space: pre-wrap; font-family: 'Times New Roman', serif;" id="previewContent">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function loadTemplate() {
            const select = document.getElementById('templateId');
            const selectedOption = select.options[select.selectedIndex];

            if (selectedOption.value) {
                document.getElementById('title').value = selectedOption.dataset.title || '';
                document.getElementById('content').value = selectedOption.dataset.content || '';
            }
        }

        function insertVariable(variable) {
            const textarea = document.getElementById('content');
            const start = textarea.selectionStart;
            const end = textarea.selectionEnd;
            const text = textarea.value;

            textarea.value = text.substring(0, start) + variable + text.substring(end);
            textarea.selectionStart = textarea.selectionEnd = start + variable.length;
            textarea.focus();
        }

        function previewContract() {
            const title = document.getElementById('title').value || '제목 없음';
            const content = document.getElementById('content').value || '내용 없음';
            const firstPartyName = document.getElementById('firstPartyName').value || '[갑 이름]';
            const firstPartyEmail = document.getElementById('firstPartyEmail').value || '[갑 이메일]';
            const firstPartyAddress = document.getElementById('firstPartyAddress').value || '[갑 주소]';
            const secondPartyName = document.getElementById('secondPartyName').value || '[을 이름]';
            const secondPartyEmail = document.getElementById('secondPartyEmail').value || '[을 이메일]';
            const secondPartyAddress = document.getElementById('secondPartyAddress').value || '[을 주소]';

            let previewContent = content
                .replace(/\{FIRST_PARTY_NAME\}/g, firstPartyName)
                .replace(/\{FIRST_PARTY_EMAIL\}/g, firstPartyEmail)
                .replace(/\{FIRST_PARTY_ADDRESS\}/g, firstPartyAddress)
                .replace(/\{SECOND_PARTY_NAME\}/g, secondPartyName)
                .replace(/\{SECOND_PARTY_EMAIL\}/g, secondPartyEmail)
                .replace(/\{SECOND_PARTY_ADDRESS\}/g, secondPartyAddress)
                .replace(/\{CONTRACT_TITLE\}/g, title)
                .replace(/\{CONTRACT_DATE\}/g, new Date().toLocaleDateString('ko-KR'))
                .replace(/\{SIGNATURE_FIRST\}/g, '[갑 서명]')
                .replace(/\{SIGNATURE_SECOND\}/g, '[을 서명]');

            document.getElementById('previewContent').textContent = previewContent;
            new bootstrap.Modal(document.getElementById('previewModal')).show();
        }

        // 폼 유효성 검사
        (function() {
            'use strict';
            window.addEventListener('load', function() {
                var forms = document.getElementsByClassName('contract-form');
                var validation = Array.prototype.filter.call(forms, function(form) {
                    form.addEventListener('submit', function(event) {
                        const firstEmail = document.getElementById('firstPartyEmail').value;
                        const secondEmail = document.getElementById('secondPartyEmail').value;

                        if (firstEmail && secondEmail && firstEmail === secondEmail) {
                            event.preventDefault();
                            event.stopPropagation();
                            alert('갑과 을의 이메일 주소는 달라야 합니다.');
                            return false;
                        }

                        if (form.checkValidity() === false) {
                            event.preventDefault();
                            event.stopPropagation();
                        }
                        form.classList.add('was-validated');
                    }, false);
                });
            }, false);
        })();
    </script>
</body>
</html>
