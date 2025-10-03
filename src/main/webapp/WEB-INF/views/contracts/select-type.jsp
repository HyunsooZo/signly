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

    <div class="container mt-5">
        <div class="text-center mb-5">
            <h2 class="mb-3">
                <i class="bi bi-file-earmark-plus text-primary me-2"></i>
                계약서 유형 선택
            </h2>
            <p class="text-muted">작성하실 계약서 유형을 선택해주세요</p>
        </div>

        <div class="row g-4">
            <!-- 표준 양식들 -->
            <c:forEach var="preset" items="${presets}">
                <div class="col-md-6 col-lg-4">
                    <a href="/contracts/new?preset=${preset.id}" class="contract-type-card">
                        <div class="contract-type-icon" id="preview-${preset.id}">
                            <i class="bi bi-file-earmark-text"></i>
                        </div>
                        <div class="contract-type-title">${preset.name}</div>
                        <p class="contract-type-desc">${preset.description}</p>
                    </a>
                </div>
            </c:forEach>

            <!-- 직접 작성 -->
            <div class="col-md-6 col-lg-4">
                <a href="/contracts/new?direct=true" class="contract-type-card">
                    <div class="contract-type-icon">
                        <i class="bi bi-pencil-square"></i>
                    </div>
                    <div class="contract-type-title">직접 작성</div>
                    <p class="contract-type-desc">빈 양식으로 시작하여 자유롭게 작성</p>
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
        // 프리셋 미리보기 로드
        <c:forEach var="preset" items="${presets}">
        (async function() {
            try {
                const response = await fetch('/api/template-presets/${preset.id}');
                if (response.ok) {
                    const data = await response.json();
                    if (data.sections && data.sections.length > 0) {
                        const previewDiv = document.getElementById('preview-${preset.id}');
                        const wrapper = document.createElement('div');
                        wrapper.className = 'contract-preview';
                        wrapper.innerHTML = data.sections[0].content;
                        previewDiv.innerHTML = '';
                        previewDiv.appendChild(wrapper);
                    }
                }
            } catch (error) {
                console.warn('프리셋 미리보기 로드 실패:', error);
            }
        })();
        </c:forEach>
    </script>
</body>
</html>
