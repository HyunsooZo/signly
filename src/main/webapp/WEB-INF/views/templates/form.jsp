<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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

                <div class="container mt-4">
                    <div class="row justify-content-center">
                        <div class="col-12">
                            <div class="main-content-card">
                                <!-- 헤더 영역 -->
                                <div class="d-flex justify-content-between align-items-center mb-4">
                                    <div>
                                        <h2 class="mb-2">
                                            <i class="bi bi-file-earmark-plus text-primary me-2"></i>
                                            <c:out value="${empty template.title ? '새 템플릿 생성' : '템플릿 수정'}" />
                                        </h2>
                                        <p class="text-muted mb-0">계약서 템플릿을 자유롭게 구성해보세요</p>
                                    </div>
                                    <div class="d-flex gap-2">
                                        <button type="button" class="btn btn-light" onclick="previewTemplate()">
                                            <i class="bi bi-eye me-2"></i>미리보기
                                        </button>
                                        <button type="button" class="btn btn-primary" onclick="saveTemplate()">
                                            <i class="bi bi-check-circle me-2"></i>저장
                                        </button>
                                    </div>
                                </div>

                                <!-- 프리셋 선택 카드 -->
                                <div class="card mb-4 border-0 shadow-sm">
                                    <div class="card-header bg-white py-3">
                                        <div class="d-flex justify-content-between align-items-center cursor-pointer"
                                            onclick="togglePresetSection()">
                                            <h5 class="mb-0 text-primary">
                                                <i class="bi bi-lightning-charge me-2"></i>프리셋 템플릿으로 시작하기
                                            </h5>
                                            <i class="bi bi-chevron-up text-muted" id="presetToggleIcon"></i>
                                        </div>
                                    </div>
                                    <div class="card-body" id="presetSection">
                                        <div id="presetContent">
                                            <div class="text-center py-4" id="presetLoading">
                                                <div class="spinner-border text-primary mb-2" role="status">
                                                    <span class="visually-hidden">로딩 중...</span>
                                                </div>
                                                <p class="text-muted mb-0">프리셋 템플릿을 불러오는 중...</p>
                                            </div>
                                            <div class="preset-grid" id="presetGrid" style="display: none;">
                                                <!-- 프리셋 카드들이 여기에 동적으로 추가됩니다 -->
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- 변수 툴바 카드 (Sticky) -->
                                <div class="card border-0 shadow-sm mb-4 sticky-top" style="top: 20px; z-index: 100;">
                                    <div class="card-body py-3">
                                        <div class="d-flex align-items-center flex-wrap gap-2">
                                            <small class="text-muted me-2 fw-bold"><i class="bi bi-braces me-1"></i>변수
                                                도구상자:</small>
                                            <button type="button" class="btn btn-sm btn-variable-tool rounded-pill"
                                                onclick="insertVariable('[EMPLOYER]')"><i
                                                    class="bi bi-person-badge me-1"></i>사업주</button>
                                            <button type="button" class="btn btn-sm btn-variable-tool rounded-pill"
                                                onclick="insertVariable('[COMPANY_NAME]')"><i
                                                    class="bi bi-building me-1"></i>회사명</button>
                                            <button type="button" class="btn btn-sm btn-variable-tool rounded-pill"
                                                onclick="insertVariable('[EMPLOYEE]')"><i
                                                    class="bi bi-person me-1"></i>근로자</button>
                                            <button type="button" class="btn btn-sm btn-variable-tool rounded-pill"
                                                onclick="insertVariable('[CONTRACT_DATE]')"><i
                                                    class="bi bi-calendar-event me-1"></i>계약일</button>
                                            <button type="button" class="btn btn-sm btn-variable-tool rounded-pill"
                                                onclick="insertVariable('[WORKPLACE]')"><i
                                                    class="bi bi-geo-alt me-1"></i>근무장소</button>
                                            <button type="button" class="btn btn-sm btn-variable-tool rounded-pill"
                                                onclick="insertVariable('[JOB_DESCRIPTION]')"><i
                                                    class="bi bi-briefcase me-1"></i>업무내용</button>
                                            <button type="button" class="btn btn-sm btn-variable-tool rounded-pill"
                                                onclick="insertVariable('[MONTHLY_SALARY]')"><i
                                                    class="bi bi-cash me-1"></i>월급</button>
                                            <button type="button" class="btn btn-sm btn-secondary rounded-pill ms-auto"
                                                onclick="showVariableModal()">
                                                <i class="bi bi-plus-lg me-1"></i>전체보기
                                            </button>
                                        </div>
                                    </div>
                                </div>

                                <!-- 템플릿 에디터 카드 -->
                                <div class="card border-0 shadow-sm">
                                    <div class="card-body p-4">
                                        <!-- 제목 입력 -->
                                        <div class="mb-4">
                                            <label for="templateTitle" class="form-label fw-bold">템플릿 제목</label>
                                            <input type="text" class="form-control form-control-lg" id="templateTitle"
                                                placeholder="예: 표준 근로계약서" value="${template.title}">
                                        </div>
                                        <!-- 에디터 영역 -->
                                        <div class="document-container border rounded p-4 bg-light"
                                            style="min-height: 500px;">
                                            <div class="document-body" id="documentBody">
                                                <!-- 초기 플레이스홀더 -->
                                                <div class="add-section-placeholder text-center py-5 cursor-pointer hover-bg-light rounded"
                                                    onclick="showAddSectionMenu(this)">
                                                    <i class="bi bi-plus-circle display-4 text-muted mb-3"></i>
                                                    <h5 class="text-muted">여기를 클릭하여 첫 번째 섹션을 추가하세요</h5>
                                                    <p class="text-muted small">제목, 본문, 서명란 등 다양한 요소를 추가할 수 있습니다</p>
                                                </div>
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
                <c:set var="formAction"
                    value="${not empty templateId && templateId ne 'new' ? '/templates/'.concat(templateId) : '/templates'}" />
                <form id="templateForm" method="post" action="${formAction}" class="template-form-hidden">
                    <input type="hidden" name="title" id="formTitle">
                    <input type="hidden" name="sectionsJson" id="sectionsJson">
                </form>

                <!-- 미리보기 모달 -->
                <div class="modal fade" id="previewModal" tabindex="-1" aria-labelledby="previewModalLabel"
                    aria-hidden="true">
                    <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title" id="previewModalLabel">템플릿 미리보기</h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"
                                    aria-label="닫기"></button>
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
                <script id="initialSections"
                    type="application/json"><c:out value="${sectionsJsonSafe}" escapeXml="false" /></script>
                <script src="/js/alerts.js"></script>
                <script src="/js/template-builder.js"></script>
                <script>
                    // Global aliases for TemplateBuilder methods
                    const showAddSectionMenu = (el) => TemplateBuilder.ui.showAddSectionMenu(el);
                    const insertVariable = (v) => TemplateBuilder.variables.insert(v);
                    const showVariableModal = () => TemplateBuilder.ui.showVariableModal();
                    const closeVariableModal = () => TemplateBuilder.ui.closeVariableModal();

                    // Toggle Preset Section
                    function togglePresetSection() {
                        const section = document.getElementById('presetSection');
                        const icon = document.getElementById('presetToggleIcon');
                        if (section.style.display === 'none') {
                            section.style.display = 'block';
                            icon.classList.remove('bi-chevron-down');
                            icon.classList.add('bi-chevron-up');
                        } else {
                            section.style.display = 'none';
                            icon.classList.remove('bi-chevron-up');
                            icon.classList.add('bi-chevron-down');
                        }
                    }

                    // Save Template
                    function saveTemplate() {
                        const title = document.getElementById('templateTitle').value.trim();
                        if (!title) {
                            alert('템플릿 제목을 입력해주세요.');
                            return;
                        }

                        TemplateBuilder.sections.updateSectionsData();
                        const sections = TemplateBuilder.state.sections;

                        if (sections.length === 0) {
                            if (!confirm('섹션이 없습니다. 빈 템플릿을 저장하시겠습니까?')) return;
                        }

                        document.getElementById('formTitle').value = title;
                        document.getElementById('sectionsJson').value = JSON.stringify(sections);
                        document.getElementById('templateForm').submit();
                    }

                    // Preview Template (Basic)
                    function previewTemplate() {
                        alert('미리보기 기능은 현재 준비 중입니다. 저장 후 목록에서 확인해주세요.');
                    }

                    // Initialize
                    document.addEventListener('DOMContentLoaded', function () {
                        TemplateBuilder.sections.loadInitialSections();
                        TemplateBuilder.presets.load();
                    });
                </script>
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
            </body>

            </html>