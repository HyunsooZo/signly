<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="ko">
        <jsp:include page="../common/header.jsp">
            <jsp:param name="additionalCss" value="/css/contract-template-base.css" />
            <jsp:param name="additionalCss2" value="/css/contract-template-preview.css" />
            <jsp:param name="additionalCss3" value="/css/contracts.css" />
            <jsp:param name="additionalCss4" value="/css/templates.css" />
        </jsp:include>

        <body>
            <jsp:include page="../common/navbar.jsp">
                <jsp:param name="currentPage" value="contracts" />
            </jsp:include>

            <div class="container mt-5">
                <div class="main-content-card">
                    <div class="text-center mb-5">
                        <h2 class="mb-3">
                            <i class="bi bi-file-earmark-plus text-primary me-2"></i>
                            계약서 템플릿 선택
                        </h2>
                        <p class="text-muted">사용할 템플릿을 선택하거나 새로 만들어주세요</p>
                    </div>

                    <!-- 통합 템플릿 그리드 -->
                    <div class="row g-4" id="allTemplates">
                        <!-- 새 템플릿 만들기 카드 -->
                        <div class="col-md-6 col-lg-3">
                            <a href="/templates/new" class="template-card template-card-new">
                                <div class="template-card-body">
                                    <div class="template-preview">
                                        <div class="new-template-icon">
                                            <i class="bi bi-plus-circle-dotted"></i>
                                        </div>
                                    </div>
                                    <h5 class="template-title">새 템플릿 만들기</h5>
                                </div>
                            </a>
                        </div>

                        <!-- 프리셋 템플릿 -->
                        <c:forEach var="preset" items="${presets}">
                            <div class="col-md-6 col-lg-3">
                                <div class="template-card template-card-clickable"
                                    onclick="selectPreset('<c:out value='${preset.id}'/>')"
                                    data-template-id="<c:out value='${preset.id}'/>" data-template-type="preset">
                                    <div class="template-card-body">
                                        <!-- ✅ preset-document 클래스 추가 -->
                                        <div class="template-preview preset-document"
                                            data-preset-id="<c:out value='${preset.id}'/>">
                                            <!-- ✅ template-status-badge로 변경 (Templates와 통일) -->
                                            <div class="template-status-badge">
                                                <span class="badge bg-primary">기본</span>
                                            </div>
                                            <p class="text-muted small">미리보기 로딩 중...</p>
                                        </div>
                                        <h5 class="template-title">
                                            <c:out value="${preset.name}" />
                                        </h5>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>

                        <!-- 사용자 템플릿은 JavaScript로 동적 추가 -->

                    </div>

                    <div class="text-center mt-5">
                        <a href="/contracts" class="btn btn-outline-secondary">
                            <i class="bi bi-arrow-left me-2"></i>목록으로 돌아가기
                        </a>
                    </div>
                </div>
            </div>


            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
            <script>
                document.addEventListener('DOMContentLoaded', function () {
                    loadUserTemplates();
                    loadPresetPreviews();
                });

                async function loadPresetPreviews() {
                    const previewDivs = document.querySelectorAll('.template-preview[data-preset-id]');

                    for (const div of previewDivs) {
                        const presetId = div.getAttribute('data-preset-id');
                        try {
                            const response = await fetch('/api/templates/preset/' + presetId);
                            if (response.ok) {
                                const data = await response.json();

                                // ✅ 배지 HTML 생성 (Templates와 동일)
                                const badgeHtml = '<div class="template-status-badge"><span class="badge bg-primary">기본</span></div>';

                                if (data.renderedHtml) {
                                    // ✅ 배지 + 렌더링된 HTML
                                    div.innerHTML = badgeHtml + data.renderedHtml;
                                } else {
                                    // ✅ 배지 + 에러 메시지
                                    div.innerHTML = badgeHtml + '<p class="text-muted small">미리보기 없음</p>';
                                }
                            }
                        } catch (error) {
                            console.error('프리셋 미리보기 로드 실패:', error);
                            const badgeHtml = '<div class="template-status-badge"><span class="badge bg-primary">기본</span></div>';
                            div.innerHTML = badgeHtml + '<p class="text-muted small">미리보기 로드 실패</p>';
                        }
                    }
                }

                async function loadUserTemplates() {
                    try {
                        const userInfo = JSON.parse(localStorage.getItem('signly_user_info'));
                        const userId = userInfo?.userId;
                        // 최신순 정렬 (createdAt DESC)
                        const response = await fetch('/api/templates?status=ACTIVE&size=100&sort=createdAt,desc', {
                            headers: {
                                'X-User-Id': userId
                            }
                        });
                        if (response.ok) {
                            const data = await response.json();
                            const allTemplatesContainer = document.getElementById('allTemplates');

                            if (data.content && data.content.length > 0) {
                                // 사용자 템플릿을 맨 뒤에 추가 (프리셋 다음)
                                data.content.forEach(template => {
                                    const templateCard = createUserTemplateCard(template);
                                    allTemplatesContainer.appendChild(templateCard);
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
                     onclick="selectUserTemplate('` + template.templateId + `')"
                     data-template-id="` + template.templateId + `"
                     data-template-type="user">
                    <div class="template-card-body">
                        <!-- ✅ preset-document 클래스 추가 -->
                        <div class="template-preview preset-document">
                            <!-- ✅ template-status-badge로 변경 -->
                            <div class="template-status-badge">
                                <span class="badge bg-success">내 템플릿</span>
                            </div>
                        </div>
                        <h5 class="template-title">` + template.title + `</h5>
                    </div>
                </div>
            `;

                    const previewDiv = col.querySelector('.template-preview');
                    if (previewDiv && template.renderedHtml) {
                        // ✅ 배지 구조 변경 + HTML 추가 (덮어쓰지 않고 추가)
                        const badgeHtml = '<div class="template-status-badge"><span class="badge bg-success">내 템플릿</span></div>';
                        previewDiv.innerHTML = badgeHtml + template.renderedHtml;
                    }

                    return col;
                }

                function selectPreset(presetId) {
                    window.location.href = '/contracts/new?templateId=' + presetId;
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