<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle}"/> - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/pdf-viewer.css" rel="stylesheet">
</head>
<body>
<div class="pdf-viewer-header">
    <h1 class="pdf-viewer-title">
        <i class="bi bi-file-pdf me-2"></i><c:out value="${contract.title}"/>
    </h1>
    <div class="pdf-viewer-actions">
        <a href="/contracts/<c:out value='${contract.id}'/>/pdf/download"
           class="pdf-viewer-btn pdf-viewer-btn-primary"
           download>
            <i class="bi bi-download"></i>
            <span class="pdf-viewer-btn-text">다운로드</span>
        </a>
        <a href="/contracts/<c:out value='${contract.id}'/>"
           class="pdf-viewer-btn">
            <i class="bi bi-arrow-left"></i>
            <span class="pdf-viewer-btn-text">돌아가기</span>
        </a>
    </div>
</div>

<div class="pdf-viewer-container" id="pdfContainer">
    <div class="pdf-loading" id="pdfLoading">
        <div class="spinner-border text-light mb-3" role="status">
            <span class="visually-hidden">로딩 중...</span>
        </div>
        <p>PDF를 불러오는 중입니다...</p>
    </div>

    <!-- PDF 뷰어 - iframe 사용 (모든 브라우저에서 가장 안정적) -->
    <iframe id="pdfFrame"
            src="/contracts/<c:out value='${contract.id}'/>/pdf/inline"
            class="pdf-viewer-iframe">
    </iframe>
</div>

<script>
    // PDF 로딩 처리
    const pdfFrame = document.getElementById('pdfFrame');
    const pdfLoading = document.getElementById('pdfLoading');
    const pdfContainer = document.getElementById('pdfContainer');

    // PDF 로딩 타임아웃 (30초로 증가)
    const loadingTimeout = setTimeout(() => {
        showError('PDF 로딩 시간이 초과되었습니다.');
    }, 30000);

    // 로딩 시작 후 2초 뒤에 iframe 표시 (PDF 생성 시간 고려)
    setTimeout(() => {
        pdfFrame.style.display = 'block';
    }, 2000);

    // iframe 로드 완료 시
    pdfFrame.onload = function () {
        clearTimeout(loadingTimeout);
        // 로딩 표시를 3초 후에 숨김 (PDF 렌더링 시간 고려)
        setTimeout(() => {
            pdfLoading.style.display = 'none';
        }, 3000);
    };

    // iframe 로드 실패 시
    pdfFrame.onerror = function () {
        clearTimeout(loadingTimeout);
        showError('PDF를 불러올 수 없습니다.');
    };

    function showError(message) {
        pdfLoading.style.display = 'none';
        pdfFrame.style.display = 'none';

        const errorDiv = document.createElement('div');
        errorDiv.className = 'pdf-error';
        errorDiv.innerHTML = `
                <i class="bi bi-exclamation-triangle-fill text-warning"></i>
                <h4>` + message + `</h4>
                <p class="mb-3">PDF 파일을 직접 다운로드하여 확인해주세요.</p>
                <a href="/contracts/<c:out value='${contract.id}'/>/pdf/download"
                   class="btn btn-primary"
                   download>
                    <i class="bi bi-download me-2"></i>PDF 다운로드
                </a>
            `;
        pdfContainer.appendChild(errorDiv);
    }

    // 키보드 단축키 (ESC: 돌아가기)
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            window.location.href = '/contracts/${contract.id}';
        }
    });
</script>
</body>
</html>
