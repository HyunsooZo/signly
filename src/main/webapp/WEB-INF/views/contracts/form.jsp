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
        .contract-builder-wrap {
            display: grid;
            grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
            gap: 1.5rem;
        }
        @media (max-width: 992px) {
            .contract-builder-wrap {
                grid-template-columns: minmax(0, 1fr);
            }
        }
        .preview-container {
            position: sticky;
            top: 20px;
            align-self: flex-start;
            transition: transform 0.2s ease;
            z-index: 10;
        }
        .preview-surface {
            background: #fff;
            padding: 1.5rem;
            max-height: calc(100vh - 120px);
            overflow: auto;
            min-height: 500px;
            word-wrap: break-word;
            word-break: break-word;
            font-size: 0.9rem;
            transition: all 0.3s ease;
            border-radius: 0 0 12px 12px;
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

            <div id="normalLayout">
                <div class="row">
                    <!-- 계약서 기본 정보 -->
                    <div class="col-lg-8" id="mainFormCol">
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

                            <!-- 숨겨진 프리셋 select (selectedPreset으로 넘어온 경우를 위해) -->
                            <select id="presetSelect" style="display: none;">
                                <option value="">표준 양식을 선택하세요</option>
                                <c:forEach var="preset" items="${presets}">
                                    <option value="${preset.id}" data-name="${preset.name}">
                                        ${preset.name} - ${preset.description}
                                    </option>
                                </c:forEach>
                            </select>

                            <div class="mb-3">
                                <label for="title" class="form-label">계약서 제목 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control form-control-lg" id="title" name="title"
                                       value="${contract.title}" required maxlength="200"
                                       placeholder="계약서 제목을 입력하세요">
                            </div>

                            <div class="mb-3" id="contentSection">
                                <label for="content" class="form-label">계약서 내용 <span class="text-danger">*</span></label>
                                <textarea class="form-control content-editor" id="content" name="content"
                                          rows="15" required placeholder="계약서 내용을 입력하세요...">${contract.content}</textarea>
                                <div class="form-text">계약서의 전체 내용을 입력하세요. 변수를 사용하여 동적 값을 설정할 수 있습니다.</div>

                                <!-- 프리셋 폼 필드 컨테이너 (동적으로 생성됨) -->
                                <div id="presetFormFields" style="display: none;"></div>
                            </div>

                            <div class="mb-3">
                                <label for="expiresAt" class="form-label">만료일</label>
                                <input type="datetime-local" class="form-control" id="expiresAt" name="expiresAt"
                                       value="${contract.expiresAtInputValue}">
                                <div class="form-text">계약서의 서명 만료일을 설정하세요 (선택사항).</div>
                            </div>
                        </div>
                    </div>

                </div>

                <!-- 당사자 정보 -->
                <div class="col-lg-4" id="partyInfoCol">
                    <div class="card mb-4" id="partyInfoCard">
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
                        </div>
                    </div>
                </div>

                </div>
            </div>

            <!-- 프리셋 레이아웃 (grid 사용) -->
            <div id="presetLayout" class="contract-builder-wrap" style="display: none;">
                <div id="presetFormArea">
                    <!-- 좌측: 프리셋 폼 필드 영역 -->
                </div>
                <div>
                    <!-- 우측: 실시간 미리보기 -->
                    <div class="preview-container">
                        <div class="card mb-4">
                            <div class="card-header">
                                <h5 class="card-title mb-0">
                                    <i class="bi bi-display me-2"></i>실시간 미리보기
                                </h5>
                            </div>
                            <div class="card-body p-0">
                                <div id="livePreview" class="preview-surface">
                                    <div class="text-muted text-center py-5">
                                        <i class="bi bi-eye display-6 d-block mb-3"></i>
                                        표준 양식을 선택하면 미리보기가 표시됩니다.
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 액션 버튼 (별도 row) -->
            <div class="row mt-4" id="actionButtonRow">
                <div class="col-12">
                    <div class="d-flex justify-content-between">
                        <a href="/contracts" class="btn btn-secondary">
                            <i class="bi bi-x me-2"></i>취소
                        </a>
                        <div>
                            <button type="button" class="btn btn-outline-primary me-2" onclick="previewContract()">
                                <i class="bi bi-eye me-2"></i>미리보기
                            </button>
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-check-circle me-2"></i>
                                ${empty contractId ? '계약서 생성' : '수정 완료'}
                            </button>
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
                    <div class="contract-preview border rounded p-4" style="background-color: #f8f9fa; min-height: 400px; white-space: pre-wrap; font-family: 'Malgun Gothic', sans-serif; line-height: 1.4; font-size: 13px;" id="previewContent">
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
        let presetFormData = {}; // 프리셋 폼 데이터 저장용
        let currentPresetHtml = ''; // 현재 프리셋 HTML 템플릿

        function loadTemplate() {
            const select = document.getElementById('templateId');
            const selectedOption = select.options[select.selectedIndex];

            if (selectedOption.value) {
                document.getElementById('title').value = selectedOption.dataset.title || '';
                document.getElementById('content').value = selectedOption.dataset.content || '';
            }
        }

        // 프리셋 선택 이벤트 핸들러
        const presetSelect = document.getElementById('presetSelect');
        if (presetSelect) {
            presetSelect.addEventListener('change', async (event) => {
                const presetId = event.target.value;
                if (!presetId) return;

                try {
                    const response = await fetch('/templates/presets/' + presetId, {
                        headers: { 'Accept': 'application/json' }
                    });

                    if (!response.ok) {
                        alert('표준 양식을 불러오지 못했습니다.');
                        return;
                    }

                    const preset = await response.json();
                    if (!preset.sections || preset.sections.length === 0) {
                        alert('이 프리셋은 섹션 데이터가 없습니다.');
                        return;
                    }

                    // HTML 템플릿 저장
                    currentPresetHtml = preset.sections[0].content;

                    // 제목 자동 설정
                    const titleInput = document.getElementById('title');
                    if (titleInput && !titleInput.value.trim() && preset.name) {
                        titleInput.value = preset.name;
                    }

                    // 레이아웃 전환: 일반 레이아웃 숨기고 프리셋 레이아웃 표시
                    const normalLayout = document.getElementById('normalLayout');
                    const presetLayout = document.getElementById('presetLayout');
                    const presetFormArea = document.getElementById('presetFormArea');

                    normalLayout.style.display = 'none';
                    presetLayout.style.display = 'grid';

                    // normalLayout의 모든 input/select/textarea disabled 처리 (content, title 제외)
                    const normalInputs = normalLayout.querySelectorAll('input, select, textarea');
                    normalInputs.forEach(field => {
                        if (field.id !== 'content' && field.id !== 'title') {
                            field.disabled = true;
                        }
                    });

                    // container를 container-fluid로 변경
                    const containerEl = document.querySelector('.container.mt-4');
                    if (containerEl) {
                        containerEl.classList.remove('container');
                        containerEl.classList.add('container-fluid');
                        containerEl.style.maxWidth = '100%';
                        containerEl.style.padding = '0 2rem';
                    }

                    // 기본 content textarea 숨기기 및 초기값 설정
                    const contentTextarea = document.getElementById('content');
                    contentTextarea.style.display = 'none';
                    contentTextarea.required = false;
                    contentTextarea.value = currentPresetHtml; // preset HTML을 초기값으로 설정

                    // 프리셋 폼 필드를 presetFormArea에 생성
                    presetFormArea.innerHTML = `
                        <div class="card mb-4">
                            <div class="card-header">
                                <h5 class="card-title mb-0">
                                    <i class="bi bi-file-earmark-text me-2"></i>표준 근로계약서 작성
                                </h5>
                            </div>
                            <div class="card-body">
                        <div class="alert alert-info mt-3">
                            <i class="bi bi-info-circle me-2"></i>
                            표준 근로계약서 양식이 로드되었습니다. 아래 필드들을 채워주세요.
                        </div>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">사업주명 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" data-field="employer" placeholder="회사명 또는 사업주명" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">근로자명 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" data-field="employee" placeholder="근로자 성명" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">계약 시작일 <span class="text-danger">*</span></label>
                                <input type="date" class="form-control" data-field="contractStartDate" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">계약 종료일 (선택사항)</label>
                                <input type="date" class="form-control" data-field="contractEndDate">
                            </div>
                            <div class="col-md-12">
                                <label class="form-label">근무장소 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" data-field="workplace" placeholder="근무지를 입력하세요" required>
                            </div>
                            <div class="col-md-12">
                                <label class="form-label">업무내용 <span class="text-danger">*</span></label>
                                <textarea class="form-control" data-field="jobDescription" rows="3" placeholder="담당 업무를 입력하세요" required></textarea>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">근무 시작시간 <span class="text-danger">*</span></label>
                                <input type="time" class="form-control" data-field="workStartTime" required>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">근무 종료시간 <span class="text-danger">*</span></label>
                                <input type="time" class="form-control" data-field="workEndTime" required>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">휴게 시작시간 <span class="text-danger">*</span></label>
                                <input type="time" class="form-control" data-field="breakStartTime" required>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">휴게 종료시간 <span class="text-danger">*</span></label>
                                <input type="time" class="form-control" data-field="breakEndTime" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">주 근무일수 <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" data-field="workDays" placeholder="5" min="1" max="7" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">휴일 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" data-field="holidays" placeholder="토, 일" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">월급 (원) <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" data-field="monthlySalary" placeholder="3000000" min="0" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">상여금 (원)</label>
                                <input type="number" class="form-control" data-field="bonus" placeholder="0" min="0">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">기타급여 (제수당 등)</label>
                                <input type="text" class="form-control" data-field="otherAllowances" placeholder="예: 교통비, 식비 등">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">급여 지급일 <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" data-field="paymentDay" placeholder="25" min="1" max="31" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">지급방법 <span class="text-danger">*</span></label>
                                <select class="form-select" data-field="paymentMethod" required>
                                    <option value="">선택하세요</option>
                                    <option value="직접 지급">직접 지급</option>
                                    <option value="계좌 입금">계좌 입금</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">계약일 <span class="text-danger">*</span></label>
                                <input type="date" class="form-control" data-field="contractDate" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">사업체명 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" data-field="companyName" placeholder="회사명" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">사업주 주소 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" data-field="employerAddress" placeholder="주소를 입력하세요" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">사업주 전화번호 <span class="text-danger">*</span></label>
                                <input type="tel" class="form-control" data-field="employerPhone" placeholder="010-0000-0000" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">근로자 주소 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" data-field="employeeAddress" placeholder="주소를 입력하세요" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">근로자 연락처 <span class="text-danger">*</span></label>
                                <input type="tel" class="form-control" data-field="employeePhone" placeholder="010-0000-0000" required>
                            </div>
                        </div>
                            </div>
                        </div>

                        <!-- 근로자 정보 카드 -->
                        <div class="card mb-4">
                            <div class="card-header">
                                <h5 class="card-title mb-0">
                                    <i class="bi bi-people me-2"></i>근로자 정보
                                </h5>
                            </div>
                            <div class="card-body">
                                <div class="mb-3">
                                    <label for="presetSecondPartyEmail" class="form-label">이메일 <span class="text-danger">*</span></label>
                                    <input type="email" class="form-control" id="presetSecondPartyEmail" name="secondPartyEmail" required maxlength="200" placeholder="example@domain.com">
                                </div>
                                <!-- hidden field for secondPartyName -->
                                <input type="hidden" id="presetSecondPartyName" name="secondPartyName">
                            </div>
                        </div>
                    `;

                    // 폼 필드 변경 시 미리보기 업데이트
                    const formFields = presetFormArea.querySelectorAll('[data-field]');
                    formFields.forEach(field => {
                        field.addEventListener('input', () => {
                            // employee 필드 값을 presetSecondPartyName hidden 필드에 동기화
                            if (field.dataset.field === 'employee') {
                                const secondPartyNameInput = document.getElementById('presetSecondPartyName');
                                if (secondPartyNameInput) {
                                    secondPartyNameInput.value = field.value;
                                }
                            }
                            updatePresetContent();
                            updateLivePreview();
                        });
                        field.addEventListener('change', () => {
                            // employee 필드 값을 presetSecondPartyName hidden 필드에 동기화
                            if (field.dataset.field === 'employee') {
                                const secondPartyNameInput = document.getElementById('presetSecondPartyName');
                                if (secondPartyNameInput) {
                                    secondPartyNameInput.value = field.value;
                                }
                            }
                            updatePresetContent();
                            updateLivePreview();
                        });
                    });


                    // 초기 content 업데이트 및 미리보기
                    updatePresetContent();
                    updateLivePreview();

                    // body 스타일 무효화 (프리셋 HTML의 body max-width 제거)
                    const bodyStyleOverride = document.createElement('style');
                    bodyStyleOverride.id = 'preset-body-override';
                    bodyStyleOverride.textContent = `
                        body {
                            max-width: 100% !important;
                            padding: 0 !important;
                            margin: 0 !important;
                        }
                    `;
                    document.head.appendChild(bodyStyleOverride);

                    presetSelect.value = '';
                } catch (error) {
                    console.error('프리셋 로딩 실패:', error);
                    alert('표준 양식을 불러오지 못했습니다.');
                }
            });
        }

        // 실시간 미리보기 업데이트
        function updateLivePreview() {
            if (!currentPresetHtml) return;

            let updatedHtml = currentPresetHtml;
            const formFields = document.querySelectorAll('#presetFormArea [data-field]');

            formFields.forEach(field => {
                const fieldName = field.dataset.field;
                const value = field.value || '';

                switch(fieldName) {
                    case 'employer':
                        updatedHtml = updatedHtml.replace(/\[EMPLOYER\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'employee':
                        updatedHtml = updatedHtml.replace(/\[EMPLOYEE\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'contractStartDate':
                        if (value && value.trim()) {
                            const parts = value.split('-');
                            if (parts.length === 3) {
                                const formattedDate = parts[0] + '년 ' + parseInt(parts[1], 10) + '월 ' + parseInt(parts[2], 10) + '일';
                                updatedHtml = updatedHtml.replace(/\[CONTRACT_START_DATE\]/g, '<strong>' + formattedDate + '</strong>');
                            }
                        } else {
                            updatedHtml = updatedHtml.replace(/\[CONTRACT_START_DATE\]/g, '');
                        }
                        break;
                    case 'contractEndDate':
                        if (value && value.trim()) {
                            const parts = value.split('-');
                            if (parts.length === 3) {
                                const formattedDate = parts[0] + '년 ' + parseInt(parts[1], 10) + '월 ' + parseInt(parts[2], 10) + '일';
                                updatedHtml = updatedHtml.replace(/\[CONTRACT_END_DATE\]/g, '<strong>' + formattedDate + '</strong>');
                            }
                        } else {
                            updatedHtml = updatedHtml.replace(/\[CONTRACT_END_DATE\]/g, '');
                        }
                        break;
                    case 'contractDate':
                        if (value && value.trim()) {
                            const parts = value.split('-');
                            if (parts.length === 3) {
                                const formattedDate = parts[0] + '년 ' + parseInt(parts[1], 10) + '월 ' + parseInt(parts[2], 10) + '일';
                                updatedHtml = updatedHtml.replace(/\[CONTRACT_DATE\]/g, '<strong>' + formattedDate + '</strong>');
                            }
                        } else {
                            updatedHtml = updatedHtml.replace(/\[CONTRACT_DATE\]/g, '');
                        }
                        break;
                    case 'workplace':
                        updatedHtml = updatedHtml.replace(/\[WORKPLACE\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'jobDescription':
                        updatedHtml = updatedHtml.replace(/\[JOB_DESCRIPTION\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'workStartTime':
                        if (value && value.includes(':')) {
                            const [h, m] = value.split(':');
                            updatedHtml = updatedHtml.replace(/\[WORK_START_TIME\]/g, '<strong>' + h + '시 ' + m + '분</strong>');
                        } else {
                            updatedHtml = updatedHtml.replace(/\[WORK_START_TIME\]/g, '');
                        }
                        break;
                    case 'workEndTime':
                        if (value && value.includes(':')) {
                            const [h, m] = value.split(':');
                            updatedHtml = updatedHtml.replace(/\[WORK_END_TIME\]/g, '<strong>' + h + '시 ' + m + '분</strong>');
                        } else {
                            updatedHtml = updatedHtml.replace(/\[WORK_END_TIME\]/g, '');
                        }
                        break;
                    case 'breakStartTime':
                        if (value && value.includes(':')) {
                            const [h, m] = value.split(':');
                            updatedHtml = updatedHtml.replace(/\[BREAK_START_TIME\]/g, '<strong>' + h + '시 ' + m + '분</strong>');
                        } else {
                            updatedHtml = updatedHtml.replace(/\[BREAK_START_TIME\]/g, '');
                        }
                        break;
                    case 'breakEndTime':
                        if (value && value.includes(':')) {
                            const [h, m] = value.split(':');
                            updatedHtml = updatedHtml.replace(/\[BREAK_END_TIME\]/g, '<strong>' + h + '시 ' + m + '분</strong>');
                        } else {
                            updatedHtml = updatedHtml.replace(/\[BREAK_END_TIME\]/g, '');
                        }
                        break;
                    case 'workDays':
                        updatedHtml = updatedHtml.replace(/\[WORK_DAYS\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'holidays':
                        updatedHtml = updatedHtml.replace(/\[HOLIDAYS\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'monthlySalary':
                        if (value) {
                            const amount = parseInt(value);
                            if (!isNaN(amount) && amount > 0) {
                                updatedHtml = updatedHtml.replace(/\[MONTHLY_SALARY\]/g, '<strong>' + amount.toLocaleString() + '</strong>');
                            } else {
                                updatedHtml = updatedHtml.replace(/\[MONTHLY_SALARY\]/g, '');
                            }
                        } else {
                            updatedHtml = updatedHtml.replace(/\[MONTHLY_SALARY\]/g, '');
                        }
                        break;
                    case 'bonus':
                        if (value) {
                            const amount = parseInt(value);
                            if (!isNaN(amount) && amount > 0) {
                                updatedHtml = updatedHtml.replace(/\[BONUS\]/g, '<strong>있음 (' + amount.toLocaleString() + '원)</strong>');
                            } else {
                                updatedHtml = updatedHtml.replace(/\[BONUS\]/g, '<strong>없음</strong>');
                            }
                        } else {
                            updatedHtml = updatedHtml.replace(/\[BONUS\]/g, '<strong>없음</strong>');
                        }
                        break;
                    case 'paymentDay':
                        updatedHtml = updatedHtml.replace(/\[PAYMENT_DAY\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'paymentMethod':
                        let methodText = '';
                        if (value === 'direct') {
                            methodText = '<strong>근로자에게 직접 지급</strong>';
                        } else if (value === 'bank') {
                            methodText = '<strong>근로자 명의 예금통장에 입금</strong>';
                        }
                        updatedHtml = updatedHtml.replace(/\[PAYMENT_METHOD\]/g, methodText);
                        break;
                    case 'companyName':
                        updatedHtml = updatedHtml.replace(/\[COMPANY_NAME\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'employerAddress':
                        updatedHtml = updatedHtml.replace(/\[EMPLOYER_ADDRESS\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'employerPhone':
                        updatedHtml = updatedHtml.replace(/\[EMPLOYER_PHONE\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'employeeAddress':
                        updatedHtml = updatedHtml.replace(/\[EMPLOYEE_ADDRESS\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'employeePhone':
                        updatedHtml = updatedHtml.replace(/\[EMPLOYEE_PHONE\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'otherAllowances':
                        updatedHtml = updatedHtml.replace(/\[OTHER_ALLOWANCES\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                }
            });

            // 계약 종료일 유무에 따른 "까지" 텍스트 처리
            const endDateField = document.querySelector('[data-field="contractEndDate"]');
            if (!endDateField || !endDateField.value.trim()) {
                updatedHtml = updatedHtml.replace(/(\[CONTRACT_START_DATE\]|<strong>\d{4}년 \d{1,2}월 \d{1,2}일<\/strong>) 부터\s+까지/g, '$1 부터');
                updatedHtml = updatedHtml.replace(/(\[CONTRACT_START_DATE\]|<strong>\d{4}년 \d{1,2}월 \d{1,2}일<\/strong>) 부터\s+\[CONTRACT_END_DATE\]\s+까지/g, '$1 부터');
            }

            // 실시간 미리보기 영역에 표시
            const livePreview = document.getElementById('livePreview');
            if (livePreview) {
                livePreview.innerHTML = updatedHtml;
            }

            // content textarea에도 최종 HTML 저장 (폼 제출용)
            const contentTextarea = document.getElementById('content');
            if (contentTextarea) {
                contentTextarea.value = updatedHtml;
                console.log('[DEBUG] updateLivePreview에서 content 저장됨, 길이:', updatedHtml.length);
            } else {
                console.error('[ERROR] content textarea를 찾을 수 없음!');
            }
        }

        // 프리셋 필드 값으로 HTML 업데이트
        function updatePresetContent() {
            if (!currentPresetHtml) return;

            let updatedHtml = currentPresetHtml;
            const formFields = document.querySelectorAll('#presetFormArea [data-field]');

            formFields.forEach(field => {
                const fieldName = field.dataset.field;
                const value = field.value || '';

                switch(fieldName) {
                    case 'employer':
                        updatedHtml = updatedHtml.replace(/\[EMPLOYER\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'employee':
                        updatedHtml = updatedHtml.replace(/\[EMPLOYEE\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'contractStartDate':
                        if (value && value.trim()) {
                            const parts = value.split('-');
                            if (parts.length === 3) {
                                const formattedDate = parts[0] + '년 ' + parseInt(parts[1], 10) + '월 ' + parseInt(parts[2], 10) + '일';
                                updatedHtml = updatedHtml.replace(/\[CONTRACT_START_DATE\]/g, '<strong>' + formattedDate + '</strong>');
                            }
                        } else {
                            updatedHtml = updatedHtml.replace(/\[CONTRACT_START_DATE\]/g, '');
                        }
                        break;
                    case 'contractEndDate':
                        if (value && value.trim()) {
                            const parts = value.split('-');
                            if (parts.length === 3) {
                                const formattedDate = parts[0] + '년 ' + parseInt(parts[1], 10) + '월 ' + parseInt(parts[2], 10) + '일';
                                updatedHtml = updatedHtml.replace(/\[CONTRACT_END_DATE\]/g, '<strong>' + formattedDate + '</strong>');
                            }
                        } else {
                            updatedHtml = updatedHtml.replace(/\[CONTRACT_END_DATE\]/g, '');
                        }
                        break;
                    case 'contractDate':
                        if (value && value.trim()) {
                            const parts = value.split('-');
                            if (parts.length === 3) {
                                const formattedDate = parts[0] + '년 ' + parseInt(parts[1], 10) + '월 ' + parseInt(parts[2], 10) + '일';
                                updatedHtml = updatedHtml.replace(/\[CONTRACT_DATE\]/g, '<strong>' + formattedDate + '</strong>');
                            }
                        } else {
                            updatedHtml = updatedHtml.replace(/\[CONTRACT_DATE\]/g, '');
                        }
                        break;
                    case 'workplace':
                        updatedHtml = updatedHtml.replace(/\[WORKPLACE\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'jobDescription':
                        updatedHtml = updatedHtml.replace(/\[JOB_DESCRIPTION\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'workStartTime':
                        if (value && value.includes(':')) {
                            const [h, m] = value.split(':');
                            updatedHtml = updatedHtml.replace(/\[WORK_START_TIME\]/g, '<strong>' + h + '시 ' + m + '분</strong>');
                        } else {
                            updatedHtml = updatedHtml.replace(/\[WORK_START_TIME\]/g, '');
                        }
                        break;
                    case 'workEndTime':
                        if (value && value.includes(':')) {
                            const [h, m] = value.split(':');
                            updatedHtml = updatedHtml.replace(/\[WORK_END_TIME\]/g, '<strong>' + h + '시 ' + m + '분</strong>');
                        } else {
                            updatedHtml = updatedHtml.replace(/\[WORK_END_TIME\]/g, '');
                        }
                        break;
                    case 'breakStartTime':
                        if (value && value.includes(':')) {
                            const [h, m] = value.split(':');
                            updatedHtml = updatedHtml.replace(/\[BREAK_START_TIME\]/g, '<strong>' + h + '시 ' + m + '분</strong>');
                        } else {
                            updatedHtml = updatedHtml.replace(/\[BREAK_START_TIME\]/g, '');
                        }
                        break;
                    case 'breakEndTime':
                        if (value && value.includes(':')) {
                            const [h, m] = value.split(':');
                            updatedHtml = updatedHtml.replace(/\[BREAK_END_TIME\]/g, '<strong>' + h + '시 ' + m + '분</strong>');
                        } else {
                            updatedHtml = updatedHtml.replace(/\[BREAK_END_TIME\]/g, '');
                        }
                        break;
                    case 'workDays':
                        updatedHtml = updatedHtml.replace(/\[WORK_DAYS\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'holidays':
                        updatedHtml = updatedHtml.replace(/\[HOLIDAYS\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'monthlySalary':
                        if (value) {
                            const amount = parseInt(value);
                            if (!isNaN(amount) && amount > 0) {
                                updatedHtml = updatedHtml.replace(/\[MONTHLY_SALARY\]/g, '<strong>' + amount.toLocaleString() + '</strong>');
                            } else {
                                updatedHtml = updatedHtml.replace(/\[MONTHLY_SALARY\]/g, '');
                            }
                        } else {
                            updatedHtml = updatedHtml.replace(/\[MONTHLY_SALARY\]/g, '');
                        }
                        break;
                    case 'bonus':
                        if (value) {
                            const amount = parseInt(value);
                            if (!isNaN(amount) && amount > 0) {
                                updatedHtml = updatedHtml.replace(/\[BONUS\]/g, '<strong>있음 (' + amount.toLocaleString() + '원)</strong>');
                            } else {
                                updatedHtml = updatedHtml.replace(/\[BONUS\]/g, '<strong>없음</strong>');
                            }
                        } else {
                            updatedHtml = updatedHtml.replace(/\[BONUS\]/g, '<strong>없음</strong>');
                        }
                        break;
                    case 'paymentDay':
                        updatedHtml = updatedHtml.replace(/\[PAYMENT_DAY\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'paymentMethod':
                        let methodText = '';
                        if (value === 'direct') {
                            methodText = '<strong>근로자에게 직접 지급</strong>';
                        } else if (value === 'bank') {
                            methodText = '<strong>근로자 명의 예금통장에 입금</strong>';
                        }
                        updatedHtml = updatedHtml.replace(/\[PAYMENT_METHOD\]/g, methodText);
                        break;
                    case 'companyName':
                        updatedHtml = updatedHtml.replace(/\[COMPANY_NAME\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'employerAddress':
                        updatedHtml = updatedHtml.replace(/\[EMPLOYER_ADDRESS\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'employerPhone':
                        updatedHtml = updatedHtml.replace(/\[EMPLOYER_PHONE\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'employeeAddress':
                        updatedHtml = updatedHtml.replace(/\[EMPLOYEE_ADDRESS\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'employeePhone':
                        updatedHtml = updatedHtml.replace(/\[EMPLOYEE_PHONE\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                    case 'otherAllowances':
                        updatedHtml = updatedHtml.replace(/\[OTHER_ALLOWANCES\]/g, value ? '<strong>' + value + '</strong>' : '');
                        break;
                }
            });

            // 계약 종료일 유무에 따른 "까지" 텍스트 처리
            const endDateField = document.querySelector('[data-field="contractEndDate"]');
            if (!endDateField || !endDateField.value.trim()) {
                updatedHtml = updatedHtml.replace(/(\[CONTRACT_START_DATE\]|<strong>\d{4}년 \d{1,2}월 \d{1,2}일<\/strong>) 부터\s+까지/g, '$1 부터');
                updatedHtml = updatedHtml.replace(/(\[CONTRACT_START_DATE\]|<strong>\d{4}년 \d{1,2}월 \d{1,2}일<\/strong>) 부터\s+\[CONTRACT_END_DATE\]\s+까지/g, '$1 부터');
            }

            // content textarea에 최종 HTML 저장
            document.getElementById('content').value = updatedHtml;
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
            const livePreview = document.getElementById('livePreview');
            const previewContent = document.getElementById('previewContent');

            // 실시간 미리보기의 현재 HTML을 그대로 복사
            if (livePreview && previewContent) {
                previewContent.innerHTML = livePreview.innerHTML;
                new bootstrap.Modal(document.getElementById('previewModal')).show();
            } else {
                // 프리셋이 아닌 경우 기존 로직
                const title = document.getElementById('title').value || '제목 없음';
                const content = document.getElementById('content').value || '내용 없음';
                const firstPartyName = document.getElementById('firstPartyName')?.value || '[갑 이름]';
                const firstPartyEmail = document.getElementById('firstPartyEmail')?.value || '[갑 이메일]';
                const firstPartyAddress = document.getElementById('firstPartyAddress')?.value || '[갑 주소]';
                const secondPartyName = document.getElementById('secondPartyName')?.value || '[을 이름]';
                const secondPartyEmail = document.getElementById('secondPartyEmail')?.value || '[을 이메일]';
                const secondPartyAddress = document.getElementById('secondPartyAddress')?.value || '[을 주소]';

                let previewText = content
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

                document.getElementById('previewContent').textContent = previewText;
                new bootstrap.Modal(document.getElementById('previewModal')).show();
            }
        }

        // 폼 유효성 검사
        (function() {
            'use strict';
            window.addEventListener('load', function() {
                var forms = document.getElementsByClassName('contract-form');
                var validation = Array.prototype.filter.call(forms, function(form) {
                    form.addEventListener('submit', function(event) {
                        // 프리셋 사용 중이면 최신 내용 업데이트
                        const presetLayout = document.getElementById('presetLayout');
                        if (presetLayout && presetLayout.style.display === 'grid') {
                            updateLivePreview();

                            // normalLayout의 모든 required 필드를 required = false로 변경
                            const normalLayout = document.getElementById('normalLayout');
                            const normalRequiredFields = normalLayout.querySelectorAll('[required]');
                            console.log('[DEBUG] normalLayout required 필드 개수:', normalRequiredFields.length);
                            normalRequiredFields.forEach(field => {
                                if (field.id !== 'content' && field.id !== 'title') {
                                    field.required = false;
                                    console.log('[DEBUG] required 해제:', field.id || field.name);
                                }
                            });

                            // employee 필드 값을 secondPartyName에 동기화
                            const employeeField = document.querySelector('[data-field="employee"]');
                            if (employeeField && employeeField.value) {
                                const allSecondPartyNames = document.querySelectorAll('[name="secondPartyName"]');
                                allSecondPartyNames.forEach(field => {
                                    field.value = employeeField.value;
                                    console.log('[DEBUG] secondPartyName 설정:', field.id, 'value:', field.value);
                                });
                            }

                            // employer 필드 값을 firstPartyName에 동기화
                            const employerField = document.querySelector('[data-field="employer"]');
                            if (employerField && employerField.value) {
                                const firstPartyNameField = document.querySelector('[name="firstPartyName"]');
                                if (firstPartyNameField) {
                                    firstPartyNameField.value = employerField.value;
                                    console.log('[DEBUG] firstPartyName 설정:', firstPartyNameField.value);
                                }
                            }

                            // presetSecondPartyEmail 값을 모든 이메일 필드에 동기화
                            const presetEmailField = document.getElementById('presetSecondPartyEmail');
                            if (presetEmailField && presetEmailField.value) {
                                // normalLayout의 secondPartyEmail은 비활성화되어 있으니 무시
                                console.log('[DEBUG] secondPartyEmail 설정:', presetEmailField.value);
                            }

                            console.log('[DEBUG] 폼 제출 전 content 값:', document.getElementById('content').value.substring(0, 100));
                        }

                        const firstEmail = document.getElementById('firstPartyEmail')?.value;
                        const secondEmail = document.getElementById('secondPartyEmail')?.value;

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

        // 스티키 미리보기를 근로자 정보 카드 하단에 맞춰 멈추도록 처리
        function handleStickyPreview() {
            const presetLayout = document.getElementById('presetLayout');
            if (!presetLayout || presetLayout.style.display === 'none') {
                return; // presetLayout이 활성화되지 않은 경우 실행 안 함
            }

            const presetFormArea = document.getElementById('presetFormArea');
            const previewContainer = document.querySelector('.preview-container');

            if (!presetFormArea || !previewContainer) return;

            // presetFormArea의 실제 하단 위치 계산 (모든 자식 요소 포함)
            const formAreaTop = presetFormArea.offsetTop;
            const formAreaHeight = presetFormArea.offsetHeight;
            const formAreaBottom = formAreaTop + formAreaHeight;

            const previewHeight = previewContainer.offsetHeight;
            const viewportTop = window.scrollY;

            // 미리보기가 formArea 하단에 도달했는지 확인
            const previewBottom = viewportTop + previewHeight + 20; // sticky top 20px 포함

            if (previewBottom >= formAreaBottom) {
                // 멈춰야 할 위치: formArea 하단에서 preview 높이만큼 뺀 위치
                const absoluteTop = formAreaBottom - previewHeight;
                previewContainer.style.position = 'absolute';
                previewContainer.style.top = absoluteTop + 'px';
            } else {
                // 그 전에는 sticky 유지
                previewContainer.style.position = 'sticky';
                previewContainer.style.top = '20px';
            }
        }

        // 스크롤 이벤트 리스너 등록
        window.addEventListener('scroll', handleStickyPreview);
        window.addEventListener('resize', handleStickyPreview);

        // 페이지 로드 시 selectedPreset이 있으면 자동으로 로드
        <c:if test="${not empty selectedPreset}">
        window.addEventListener('load', function() {
            const presetSelect = document.getElementById('presetSelect');
            if (presetSelect) {
                presetSelect.value = '${selectedPreset}';
                // change 이벤트 트리거
                const event = new Event('change');
                presetSelect.dispatchEvent(event);
            }
        });
        </c:if>
    </script>
</body>
</html>
