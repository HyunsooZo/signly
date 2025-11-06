<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<jsp:include page="../common/header.jsp">
    <jsp:param name="additionalCss" value="/css/contracts.css" />
</jsp:include>
<body>
    <jsp:include page="../common/navbar.jsp">
        <jsp:param name="currentPage" value="contracts" />
    </jsp:include>

    <div class="container mt-5">
        <div class="text-center mb-5">
            <h2 class="mb-3">
                <i class="bi bi-file-earmark-plus text-primary me-2"></i>
                계약서 템플릿 선택
            </h2>
            <p class="text-muted">사용할 템플릿을 선택하거나 새로 만들어주세요</p>
        </div>

        <!-- 통합 템플릿 그리드 -->
        <div class="row g-4" id="allTemplates">
            <!-- 프리셋 템플릿 -->
            <c:forEach var="preset" items="${presets}">
                <div class="col-md-6 col-lg-3">
                    <div class="template-card template-card-clickable" 
                         onclick="selectPreset('${preset.id}')"
                         data-template-id="${preset.id}"
                         data-template-type="preset">
                        <div class="template-card-body">
                            <div class="template-preview" data-preset-id="${preset.id}">
                                <div class="template-preview-badge preset-badge">
                                    <i class="bi bi-file-earmark-text"></i>
                                    기본
                                </div>
                                <p class="text-muted small">미리보기 로딩 중...</p>
                            </div>
                            <h5 class="template-title">${preset.name}</h5>
                        </div>
                    </div>
                </div>
            </c:forEach>
            
            <!-- 사용자 템플릿은 JavaScript로 동적 추가 -->
            
            <!-- 새 템플릿 만들기 카드 -->
            <div class="col-md-6 col-lg-3">
                <a href="/templates/new" class="template-card template-card-new">
                    <div class="template-card-body">
                        <div class="template-preview">
                            <div class="new-template-icon">
                                <i class="bi bi-plus-circle"></i>
                            </div>
                        </div>
                        <h5 class="template-title">새 템플릿 만들기</h5>
                    </div>
                </a>
            </div>
        </div>

        <div class="text-center mt-5">
            <a href="/contracts" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left me-2"></i>목록으로 돌아가기
            </a>
        </div>
    </div>



    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            loadUserTemplates();
            loadPresetPreviews();
        });

        async function loadPresetPreviews() {
            const previewDivs = document.querySelectorAll('.template-preview[data-preset-id]');
            
            for (const div of previewDivs) {
                const presetId = div.getAttribute('data-preset-id');
                try {
                    const response = await fetch(`/api/templates/preset/\${presetId}`);
                    if (response.ok) {
                        const data = await response.json();
                        if (data.renderedHtml) {
                            div.innerHTML = '<div class="template-preview-badge preset-badge"><i class="bi bi-file-earmark-text"></i> 기본</div>' + data.renderedHtml;
                        } else {
                            div.innerHTML = '<div class="template-preview-badge preset-badge"><i class="bi bi-file-earmark-text"></i> 기본</div><p class="text-muted small">미리보기 없음</p>';
                        }
                    }
                } catch (error) {
                    console.error('프리셋 미리보기 로드 실패:', error);
                    div.innerHTML = '<div class="template-preview-badge preset-badge"><i class="bi bi-file-earmark-text"></i> 기본</div><p class="text-muted small">미리보기 로드 실패</p>';
                }
            }
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
                    const allTemplatesContainer = document.getElementById('allTemplates');
                    const newTemplateCard = allTemplatesContainer.querySelector('.col-md-6.col-lg-3:last-child');

                    if (data.content && data.content.length > 0) {
                        data.content.forEach(template => {
                            const templateCard = createUserTemplateCard(template);
                            allTemplatesContainer.insertBefore(templateCard, newTemplateCard);
                        });
                    }
                }
            } catch (error) {
                console.error('사용자 템플릿 로드 실패:', error);
            }
        }

        function createUserTemplateCard(template) {
            const col = document.createElement('div');
            col.className = 'col-md-6 col-lg-3';
            
            col.innerHTML = `
                <div class="template-card template-card-clickable" 
                     onclick="selectUserTemplate('\${template.templateId}')"
                     data-template-id="\${template.templateId}"
                     data-template-type="user">
                    <div class="template-card-body">
                        <div class="template-preview">
                            <div class="template-preview-badge user-badge">
                                <i class="bi bi-file-earmark-text"></i>
                                내 템플릿
                            </div>
                        </div>
                        <h5 class="template-title">\${template.title}</h5>
                    </div>
                </div>
            `;
            
            const previewDiv = col.querySelector('.template-preview');
            if (previewDiv && template.renderedHtml) {
                previewDiv.innerHTML = '<div class="template-preview-badge user-badge"><i class="bi bi-file-earmark-text"></i> 내 템플릿</div>' + template.renderedHtml;
            }
            
            return col;
        }

        function selectPreset(presetId) {
            window.location.href = '/contracts/new?preset=' + presetId;
        }

        function selectUserTemplate(templateId) {
            window.location.href = '/contracts/new?templateId=' + templateId;
        }

        function formatDate(dateString) {
            if (!dateString) return '';
            const date = new Date(dateString);
            return date.toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit'
            });
        }
    </script>
    <jsp:include page="../common/footer.jsp" />
</body>
</html>
