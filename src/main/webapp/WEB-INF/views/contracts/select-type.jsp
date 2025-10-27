<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<c:set var="additionalCss" value="${['/css/contracts.css']}" />
<jsp:include page="../common/header.jsp" />
<body>
    <c:set var="currentPage" value="contracts" />
    <jsp:include page="../common/navbar.jsp" />

    <div class="container mt-5">
        <div class="text-center mb-5">
            <h2 class="mb-3">
                <i class="bi bi-file-earmark-plus text-primary me-2"></i>
                계약서 유형 선택
            </h2>
            <p class="text-muted">작성하실 계약서 유형을 선택해주세요</p>
        </div>

        <div class="row g-4 justify-content-center">
            <!-- 템플릿 불러오기 -->
            <div class="col-md-6 col-lg-4">
                <div class="contract-type-card contract-type-card-clickable" onclick="showTemplateSelector()">
                    <div class="contract-type-icon">
                        <i class="bi bi-file-earmark-text"></i>
                    </div>
                    <div class="contract-type-title">템플릿 불러오기</div>
                    <p class="contract-type-desc">저장된 템플릿을 선택하여 계약서 작성</p>
                </div>
            </div>

            <!-- 템플릿 만들기 -->
            <div class="col-md-6 col-lg-4">
                <a href="/templates/new" class="contract-type-card">
                    <div class="contract-type-icon">
                        <i class="bi bi-plus-circle"></i>
                    </div>
                    <div class="contract-type-title">새 템플릿 만들기</div>
                    <p class="contract-type-desc">템플릿을 먼저 만들어 두고 계약서를 생성하세요</p>
                </a>
            </div>
        </div>

        <div class="text-center mt-5">
            <a href="/contracts" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left me-2"></i>목록으로 돌아가기
            </a>
        </div>
    </div>

    <!-- 템플릿 선택 모달 -->
    <div class="modal fade" id="templateSelectorModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">템플릿 선택</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="templateSelect" class="form-label">사용할 템플릿을 선택하세요</label>
                        <select class="form-select" id="templateSelect">
                            <option value="">-- 템플릿 선택 --</option>
                            <optgroup label="프리셋 템플릿">
                                <c:forEach var="preset" items="${presets}">
                                    <option value="preset:${preset.id}">${preset.name}</option>
                                </c:forEach>
                            </optgroup>
                            <optgroup label="내 템플릿" id="userTemplatesGroup">
                                <!-- JavaScript로 동적 로드 -->
                            </optgroup>
                        </select>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                    <button type="button" class="btn btn-primary" onclick="selectTemplate()">선택</button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        let templateModal;

        document.addEventListener('DOMContentLoaded', function() {
            templateModal = new bootstrap.Modal(document.getElementById('templateSelectorModal'));
            loadUserTemplates();
        });

        function showTemplateSelector() {
            templateModal.show();
        }

        async function loadUserTemplates() {
            try {
                const userInfo = JSON.parse(localStorage.getItem('signly_user_info'));
                const userId = userInfo?.userId;
                const response = await fetch('/api/templates?status=ACTIVE&size=100', {
                    headers: {
                        'X-User-Id': userId
                    }
                });
                if (response.ok) {
                    const data = await response.json();
                    const userTemplatesGroup = document.getElementById('userTemplatesGroup');

                    if (data.content && data.content.length > 0) {
                        data.content.forEach(template => {
                            const option = document.createElement('option');
                            option.value = 'template:' + template.templateId;
                            option.textContent = template.title;
                            userTemplatesGroup.appendChild(option);
                        });
                    } else {
                        const option = document.createElement('option');
                        option.disabled = true;
                        option.textContent = '저장된 템플릿이 없습니다';
                        userTemplatesGroup.appendChild(option);
                    }
                }
            } catch (error) {
                console.error('템플릿 로드 실패:', error);
            }
        }

        function selectTemplate() {
            const select = document.getElementById('templateSelect');
            const selectedValue = select.value;

            if (!selectedValue) {
                showAlertModal('템플릿을 선택해주세요.');
                return;
            }

            const [type, id] = selectedValue.split(':');

            if (type === 'preset') {
                window.location.href = '/contracts/new?preset=' + id;
            } else if (type === 'template') {
                window.location.href = '/contracts/new?templateId=' + id;
            }
        }
    </script>
    <jsp:include page="../common/footer.jsp" />
</body>
</html>
