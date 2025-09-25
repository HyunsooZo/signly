<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
        .template-form {
            max-width: 800px;
            margin: 0 auto;
        }
        .content-editor {
            min-height: 400px;
            font-family: 'Courier New', monospace;
        }
        .template-variables {
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 1rem;
            margin-bottom: 1rem;
        }
        .variable-tag {
            display: inline-block;
            background-color: #e3f2fd;
            color: #1976d2;
            padding: 0.25rem 0.5rem;
            border-radius: 4px;
            margin: 0.125rem;
            cursor: pointer;
            font-size: 0.875rem;
        }
        .variable-tag:hover {
            background-color: #bbdefb;
        }
    </style>
</head>
<body>
    <c:set var="formAction" value="/templates" />
    <c:if test="${not empty templateId}">
        <c:set var="formAction" value="/templates/${templateId}" />
    </c:if>
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
        <div class="template-form">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 class="mb-2">
                        <i class="bi bi-file-earmark-plus text-primary me-2"></i>
                        ${pageTitle}
                    </h2>
                    <p class="text-muted mb-0">계약서 템플릿의 제목과 내용을 입력하세요</p>
                </div>
                <a href="/templates" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-2"></i>목록으로
                </a>
            </div>

            <!-- 알림 메시지 -->
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="bi bi-exclamation-triangle me-2"></i>${errorMessage}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <!-- 템플릿 변수 가이드 -->
            <div class="template-variables">
                <h6 class="mb-3">
                    <i class="bi bi-info-circle text-primary me-2"></i>
                    사용 가능한 템플릿 변수
                </h6>
                <p class="small text-muted mb-2">아래 변수들을 클릭하면 템플릿 내용에 자동으로 추가됩니다:</p>
                <div class="mb-2">
                    <strong>당사자 정보:</strong>
                    <span class="variable-tag" onclick="insertVariable('{{PARTY_A_NAME}}')">{PARTY_A_NAME}</span>
                    <span class="variable-tag" onclick="insertVariable('{{PARTY_A_ADDRESS}}')">{PARTY_A_ADDRESS}</span>
                    <span class="variable-tag" onclick="insertVariable('{{PARTY_B_NAME}}')">{PARTY_B_NAME}</span>
                    <span class="variable-tag" onclick="insertVariable('{{PARTY_B_ADDRESS}}')">{PARTY_B_ADDRESS}</span>
                </div>
                <div class="mb-2">
                    <strong>계약 정보:</strong>
                    <span class="variable-tag" onclick="insertVariable('{{CONTRACT_TITLE}}')">{CONTRACT_TITLE}</span>
                    <span class="variable-tag" onclick="insertVariable('{{CONTRACT_DATE}}')">{CONTRACT_DATE}</span>
                    <span class="variable-tag" onclick="insertVariable('{{CONTRACT_AMOUNT}}')">{CONTRACT_AMOUNT}</span>
                    <span class="variable-tag" onclick="insertVariable('{{START_DATE}}')">{START_DATE}</span>
                    <span class="variable-tag" onclick="insertVariable('{{END_DATE}}')">{END_DATE}</span>
                </div>
                <div>
                    <strong>서명 정보:</strong>
                    <span class="variable-tag" onclick="insertVariable('{{SIGNATURE_A}}')">{SIGNATURE_A}</span>
                    <span class="variable-tag" onclick="insertVariable('{{SIGNATURE_B}}')">{SIGNATURE_B}</span>
                    <span class="variable-tag" onclick="insertVariable('{{SIGNATURE_DATE}}')">{SIGNATURE_DATE}</span>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <form method="post" action="${formAction}">
                        <c:if test="${not empty _csrf}">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                        </c:if>
                        <div class="mb-4">
                            <label for="title" class="form-label">
                                <i class="bi bi-type me-2"></i>템플릿 제목 <span class="text-danger">*</span>
                            </label>
                            <input type="text"
                                   class="form-control form-control-lg"
                                   id="title"
                                   name="title"
                                   value="${template.title}"
                                   placeholder="예: 용역계약서, 임대차계약서, 매매계약서 등"
                                   required
                                   maxlength="255">
                            <div class="form-text">명확하고 구체적인 템플릿 제목을 입력해주세요.</div>
                        </div>

                        <div class="mb-4">
                            <label for="content" class="form-label">
                                <i class="bi bi-file-earmark-text me-2"></i>템플릿 내용 <span class="text-danger">*</span>
                            </label>
                            <textarea class="form-control content-editor"
                                      id="content"
                                      name="content"
                                      placeholder="계약서 내용을 입력하세요. 위의 템플릿 변수를 사용하여 동적인 계약서를 만들 수 있습니다.

예시:
===== ${CONTRACT_TITLE} =====

갑: ${PARTY_A_NAME}
주소: ${PARTY_A_ADDRESS}

을: ${PARTY_B_NAME}
주소: ${PARTY_B_ADDRESS}

계약일: ${CONTRACT_DATE}
계약금액: ${CONTRACT_AMOUNT}

제1조 (목적)
이 계약은 ...

제2조 (계약기간)
계약기간은 ${START_DATE}부터 ${END_DATE}까지로 한다.

갑: ${SIGNATURE_A}     을: ${SIGNATURE_B}

서명일: ${SIGNATURE_DATE}"
                                      required>${template.content}</textarea>
                            <div class="form-text">계약서의 상세 내용을 입력하세요. 변수를 사용하여 동적인 값을 삽입할 수 있습니다.</div>
                        </div>

                        <div class="d-flex justify-content-between">
                            <a href="/templates" class="btn btn-secondary">
                                <i class="bi bi-x-circle me-2"></i>취소
                            </a>
                            <div>
                                <button type="button" class="btn btn-outline-primary me-2" onclick="previewTemplate()">
                                    <i class="bi bi-eye me-2"></i>미리보기
                                </button>
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-check-circle me-2"></i>
                                    ${not empty templateId ? '수정' : '생성'}
                                </button>
                            </div>
                        </div>
                    </form>
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
                    <div class="border rounded p-3" style="background-color: #f8f9fa; min-height: 400px; white-space: pre-wrap; font-family: 'Times New Roman', serif;" id="previewContent">
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
        function insertVariable(variable) {
            const textarea = document.getElementById('content');
            const start = textarea.selectionStart;
            const end = textarea.selectionEnd;
            const text = textarea.value;

            textarea.value = text.substring(0, start) + variable + text.substring(end);
            textarea.selectionStart = textarea.selectionEnd = start + variable.length;
            textarea.focus();
        }

        function previewTemplate() {
            const title = document.getElementById('title').value;
            const content = document.getElementById('content').value;

            if (!title || !content) {
                alert('제목과 내용을 모두 입력해주세요.');
                return;
            }

            // 샘플 데이터로 변수 치환
            let previewContent = content
                .replace(/\{PARTY_A_NAME\}/g, '홍길동')
                .replace(/\{PARTY_A_ADDRESS\}/g, '서울특별시 강남구 테헤란로 123')
                .replace(/\{PARTY_B_NAME\}/g, '김철수')
                .replace(/\{PARTY_B_ADDRESS\}/g, '서울특별시 서초구 서초대로 456')
                .replace(/\{CONTRACT_TITLE\}/g, title)
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

        // 폼 제출 전 확인
        document.querySelector('form').addEventListener('submit', function(e) {
            const title = document.getElementById('title').value.trim();
            const content = document.getElementById('content').value.trim();

            if (!title || !content) {
                e.preventDefault();
                alert('제목과 내용을 모두 입력해주세요.');
                return false;
            }

            if (title.length > 255) {
                e.preventDefault();
                alert('제목은 255자를 초과할 수 없습니다.');
                return false;
            }
        });
    </script>
</body>
</html>
