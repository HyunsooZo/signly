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
    <style>
        body {
            margin: 0;
            padding: 0;
            overflow: hidden;
            background-color: #525659;
        }

        .pdf-viewer-header {
            background-color: #323639;
            color: white;
            padding: 12px 16px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 4px rgba(0,0,0,0.2);
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            z-index: 1000;
        }

        .pdf-viewer-title {
            font-size: 16px;
            font-weight: 500;
            margin: 0;
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            padding-right: 16px;
        }

        .pdf-viewer-actions {
            display: flex;
            gap: 8px;
            flex-shrink: 0;
        }

        .pdf-viewer-btn {
            background-color: transparent;
            border: 1px solid rgba(255,255,255,0.3);
            color: white;
            padding: 6px 12px;
            border-radius: 4px;
            font-size: 14px;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            gap: 6px;
            text-decoration: none;
        }

        .pdf-viewer-btn:hover {
            background-color: rgba(255,255,255,0.1);
            border-color: rgba(255,255,255,0.5);
            color: white;
        }

        .pdf-viewer-btn-primary {
            background-color: #0d6efd;
            border-color: #0d6efd;
        }

        .pdf-viewer-btn-primary:hover {
            background-color: #0b5ed7;
            border-color: #0b5ed7;
        }

        .pdf-viewer-container {
            position: fixed;
            top: 60px;
            left: 0;
            right: 0;
            bottom: 0;
            width: 100%;
            height: calc(100vh - 60px);
        }

        .pdf-viewer-container iframe,
        .pdf-viewer-container embed,
        .pdf-viewer-container object {
            width: 100%;
            height: 100%;
            border: none;
        }

        .pdf-loading {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            text-align: center;
            color: white;
        }

        .pdf-loading .spinner-border {
            width: 3rem;
            height: 3rem;
        }

        .pdf-error {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            text-align: center;
            color: white;
            max-width: 400px;
            padding: 20px;
        }

        .pdf-error i {
            font-size: 48px;
            margin-bottom: 16px;
        }

        @media (max-width: 768px) {
            .pdf-viewer-header {
                padding: 10px 12px;
            }

            .pdf-viewer-title {
                font-size: 14px;
            }

            .pdf-viewer-btn {
                padding: 5px 10px;
                font-size: 13px;
            }

            .pdf-viewer-btn-text {
                display: none;
            }

            .pdf-viewer-container {
                top: 52px;
                height: calc(100vh - 52px);
            }
        }
    </style>
</head>
<body>
    <div class="pdf-viewer-header">
        <h1 class="pdf-viewer-title">
            <i class="bi bi-file-pdf me-2"></i>${contract.title}
        </h1>
        <div class="pdf-viewer-actions">
            <a href="/contracts/${contract.id}/pdf/download"
               class="pdf-viewer-btn pdf-viewer-btn-primary"
               download>
                <i class="bi bi-download"></i>
                <span class="pdf-viewer-btn-text">다운로드</span>
            </a>
            <a href="/contracts/${contract.id}"
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
                src="/contracts/${contract.id}/pdf/inline"
                style="display: none; width: 100%; height: 100%; border: none;">
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
        pdfFrame.onload = function() {
            clearTimeout(loadingTimeout);
            // 로딩 표시를 3초 후에 숨김 (PDF 렌더링 시간 고려)
            setTimeout(() => {
                pdfLoading.style.display = 'none';
            }, 3000);
        };

        // iframe 로드 실패 시
        pdfFrame.onerror = function() {
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
                <h4>${message}</h4>
                <p class="mb-3">PDF 파일을 직접 다운로드하여 확인해주세요.</p>
                <a href="/contracts/${contract.id}/pdf/download"
                   class="btn btn-primary"
                   download>
                    <i class="bi bi-download me-2"></i>PDF 다운로드
                </a>
            `;
            pdfContainer.appendChild(errorDiv);
        }

        // 키보드 단축키 (ESC: 돌아가기)
        document.addEventListener('keydown', function(event) {
            if (event.key === 'Escape') {
                window.location.href = '/contracts/${contract.id}';
            }
        });
    </script>
</body>
</html>
