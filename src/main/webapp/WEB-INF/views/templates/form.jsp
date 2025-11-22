<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<jsp:include page="../common/header.jsp">
    <jsp:param name="additionalCss" value="/css/template-builder.css" />
    <jsp:param name="additionalCss2" value="/css/modal.css" />
</jsp:include>
<body>
    <jsp:include page="../common/navbar.jsp">
        <jsp:param name="currentPage" value="templates" />
    </jsp:include>

<div class="builder-container">
    <!-- 프리셋 템플릿 선택 영역 -->
    <div class="main-content-card">
        <div class="card">
            <div class="card-body">
                <div class="preset-section" id="presetSection">
        <div class="preset-header">
            <h5 class="preset-title">
                <i class="bi bi-lightning-charge"></i>
                프리셋 템플릿으로 시작하기
            </h5>
            <button class="btn btn-sm btn-outline-secondary" onclick="togglePresetSection()">
                <i class="bi bi-chevron-up" id="presetToggleIcon"></i>
            </button>
        </div>
        <div class="preset-content" id="presetContent">
            <div class="preset-loading" id="presetLoading">
                <div class="spinner-border spinner-border-sm" role="status">
                    <span class="visually-hidden">로딩 중...</span>
                </div>
                프리셋 템플릿을 불러오는 중...
            </div>
            <div class="preset-grid" id="presetGrid" style="display: none;">
                <!-- 프리셋 카드들이 여기에 동적으로 추가됩니다 -->
            </div>
        </div>
                </div>

                <!-- 툴바 -->
                <div class="toolbar">
        <div class="d-flex align-items-center flex-wrap gap-2">
            <strong class="me-2 toolbar-label">변수 추가하기:</strong>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[EMPLOYER]')" title="사업주">
                <i class="bi bi-person-badge"></i> 사업주
            </button>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[COMPANY_NAME]')" title="회사명">
                <i class="bi bi-building"></i> 회사명
            </button>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[EMPLOYEE]')" title="근로자">
                <i class="bi bi-person"></i> 근로자
            </button>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[CONTRACT_DATE]')" title="계약일">
                <i class="bi bi-calendar-event"></i> 계약일
            </button>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[WORKPLACE]')" title="근무장소">
                <i class="bi bi-geo-alt"></i> 근무장소
            </button>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[JOB_DESCRIPTION]')" title="업무내용">
                <i class="bi bi-briefcase"></i> 업무내용
            </button>
            <button class="toolbar-btn toolbar-btn-sm" onclick="insertVariable('[MONTHLY_SALARY]')" title="월급">
                <i class="bi bi-cash"></i> 월급
            </button>
            <button class="toolbar-btn toolbar-btn-sm ms-auto" onclick="showVariableModal()">
                <i class="bi bi-braces"></i> 더보기
            </button>
        </div>

                <!-- 문서 편집 영역 -->
                <div class="document-container">
        <div class="document-header">
            <input type="text"
                   class="document-title-input"
                   id="templateTitle"
                   placeholder="템플릿 제목을 입력하세요"
                   value="${template.title}">
            <div class="document-actions">
                <button class="btn btn-light btn-sm" onclick="previewTemplate()">
                    <i class="bi bi-eye"></i> 미리보기
                </button>
                <button class="btn btn-success btn-sm" onclick="saveTemplate()">
                    <i class="bi bi-check-circle"></i> 저장
                </button>
            </div>
        </div>

        <div class="document-body" id="documentBody">
            <!-- 초기 플레이스홀더 -->
            <div class="add-section-placeholder" onclick="showAddSectionMenu(this)">
                <i class="bi bi-plus-circle"></i>
                <div>여기를 클릭하여 섹션을 추가하세요</div>
            </div>
        </div>
    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- 변수 선택 모달 -->
<div class="modal-backdrop" id="modalBackdrop"></div>
<div class="variable-modal" id="variableModal">
    <h5 class="mb-3">변수 선택</h5>
    <p class="text-muted small">클릭하여 현재 커서 위치에 변수를 삽입합니다</p>

    <div class="mb-3">
        <strong>근로자 정보</strong>
        <div class="variable-grid">
            <div class="variable-item" onclick="insertVariable('[EMPLOYEE]')">근로자</div>
            <div class="variable-item" onclick="insertVariable('[EMPLOYEE_ADDRESS]')">근로자주소</div>
            <div class="variable-item" onclick="insertVariable('[EMPLOYEE_PHONE]')">근로자연락처</div>
            <div class="variable-item" onclick="insertVariable('[EMPLOYEE_ID]')">주민등록번호</div>
        </div>
    </div>

    <div class="mb-3">
        <strong>사업주 정보</strong>
        <div class="variable-grid">
            <div class="variable-item" onclick="insertVariable('[EMPLOYER]')">사업주</div>
            <div class="variable-item" onclick="insertVariable('[COMPANY_NAME]')">회사명</div>
            <div class="variable-item" onclick="insertVariable('[EMPLOYER_ADDRESS]')">사업주주소</div>
            <div class="variable-item" onclick="insertVariable('[EMPLOYER_PHONE]')">사업주전화</div>
            <div class="variable-item" onclick="insertVariable('[BUSINESS_NUMBER]')">사업자번호</div>
        </div>
    </div>

    <div class="mb-3">
        <strong>계약 정보</strong>
        <div class="variable-grid">
            <div class="variable-item" onclick="insertVariable('[CONTRACT_START_DATE]')">시작일</div>
            <div class="variable-item" onclick="insertVariable('[CONTRACT_END_DATE]')">종료일</div>
            <div class="variable-item" onclick="insertVariable('[CONTRACT_DATE]')">계약일</div>
            <div class="variable-item" onclick="insertVariable('[WORKPLACE]')">근무장소</div>
            <div class="variable-item" onclick="insertVariable('[JOB_DESCRIPTION]')">업무내용</div>
        </div>
    </div>

    <div class="mb-3">
        <strong>근무 조건</strong>
        <div class="variable-grid">
            <div class="variable-item" onclick="insertVariable('[WORK_START_TIME]')">근무시작</div>
            <div class="variable-item" onclick="insertVariable('[WORK_END_TIME]')">근무종료</div>
            <div class="variable-item" onclick="insertVariable('[BREAK_START_TIME]')">휴게시작</div>
            <div class="variable-item" onclick="insertVariable('[BREAK_END_TIME]')">휴게종료</div>
            <div class="variable-item" onclick="insertVariable('[WORK_DAYS]')">근무일수</div>
            <div class="variable-item" onclick="insertVariable('[HOLIDAYS]')">휴일</div>
        </div>
    </div>

    <div class="mb-3">
        <strong>임금 정보</strong>
        <div class="variable-grid">
            <div class="variable-item" onclick="insertVariable('[MONTHLY_SALARY]')">월급</div>
            <div class="variable-item" onclick="insertVariable('[HOURLY_WAGE]')">시급</div>
            <div class="variable-item" onclick="insertVariable('[BONUS]')">상여금</div>
            <div class="variable-item" onclick="insertVariable('[OTHER_ALLOWANCES]')">기타수당</div>
            <div class="variable-item" onclick="insertVariable('[PAYMENT_DAY]')">지급일</div>
            <div class="variable-item" onclick="insertVariable('[PAYMENT_METHOD]')">지급방법</div>
        </div>
    </div>

    <div class="mb-3">
        <strong>서명</strong>
        <div class="variable-grid">
            <div class="variable-item" onclick="insertVariable('[EMPLOYER_SIGNATURE]')">사업주서명</div>
            <div class="variable-item" onclick="insertVariable('[EMPLOYEE_SIGNATURE]')">근로자서명</div>
            <div class="variable-item" onclick="insertVariable('[SIGNATURE_DATE]')">서명일</div>
        </div>
    </div>

    <div class="text-end mt-4">
        <button class="btn btn-secondary" onclick="closeVariableModal()">닫기</button>
    </div>
</div>

<!-- 플로팅 액션 버튼 -->
<div class="floating-actions">
    <div class="floating-btn" onclick="saveTemplate()" title="저장">
        <i class="bi bi-save"></i>
    </div>
    <div class="floating-btn" onclick="previewTemplate()" title="미리보기">
        <i class="bi bi-eye"></i>
    </div>
</div>

<!-- Hidden form for submission -->
<c:set var="formAction" value="${not empty templateId && templateId ne 'new' ? '/templates/'.concat(templateId) : '/templates'}" />
<form id="templateForm" method="post" action="${formAction}" class="template-form-hidden">
    <c:if test="${not empty _csrf}">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
    </c:if>
    <input type="hidden" name="title" id="formTitle">
    <input type="hidden" name="sectionsJson" id="sectionsJson">
</form>

<!-- 미리보기 모달 -->
<div class="modal fade" id="previewModal" tabindex="-1" aria-labelledby="previewModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="previewModalLabel">템플릿 미리보기</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="닫기"></button>
            </div>
            <div class="modal-body p-0">
                <iframe id="previewFrame" title="템플릿 미리보기" class="preview-iframe"></iframe>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>



<c:set var="sectionsJsonRaw" value="${empty template.sectionsJson ? '[]' : template.sectionsJson}" />
<c:set var="sectionsJsonSafe" value="${fn:replace(sectionsJsonRaw, '</script>', '<&#92;/script>')}" />
<script id="initialSections" type="application/json"><c:out value="${sectionsJsonSafe}" escapeXml="false" /></script>

<jsp:include page="../common/footer.jsp" />
<script src="/js/template-builder.js"></script>
</body>
</html>
