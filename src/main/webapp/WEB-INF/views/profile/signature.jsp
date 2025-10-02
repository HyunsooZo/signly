<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
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
                <a class="nav-link" href="/contracts">계약서</a>
                <a class="nav-link active" href="/profile/signature">서명 관리</a>
                <a class="nav-link" href="/logout">로그아웃</a>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <div class="row mb-4">
            <div class="col-12">
                <h2 class="mb-2">
                    <i class="bi bi-pencil-square text-primary me-2"></i>
                    갑 서명 관리
                </h2>
                <p class="text-muted mb-0">계약서에 자동으로 삽입될 서명을 등록하고 관리하세요.</p>
            </div>
        </div>

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

        <div class="row g-4">
            <div class="col-lg-6">
                <div class="card h-100">
                    <div class="card-header">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-image me-2"></i>등록된 서명
                        </h5>
                    </div>
                    <div class="card-body d-flex flex-column justify-content-center align-items-center text-center">
                        <c:choose>
                            <c:when test="${hasSignature}">
                                <img src="${signatureDataUrl}" alt="등록된 서명" class="img-fluid border rounded" style="max-width: 320px; background-color: #fff; padding: 12px;">
                                <div class="mt-3 text-muted small">
                                    <div><strong>파일명:</strong> ${signature.originalFilename()}</div>
                                    <div><strong>형식:</strong> ${signature.mimeType()}</div>
                                    <div><strong>파일 크기:</strong> <fmt:formatNumber value="${signature.fileSize() / 1024}" maxFractionDigits="0"/> KB</div>
                                    <div><strong>업로드일:</strong> <fmt:formatDate value="${signature.updatedAtDate}" pattern="yyyy-MM-dd HH:mm"/></div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="text-muted">
                                    <i class="bi bi-pencil display-5 d-block mb-3"></i>
                                    아직 등록된 서명이 없습니다.<br>
                                    아래에서 서명 이미지를 업로드해 주세요.
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <div class="col-lg-6">
                <div class="card h-100">
                    <div class="card-header">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-pencil-square me-2"></i>서명 작성
                        </h5>
                    </div>
                    <div class="card-body">
                        <form method="post" id="signatureForm" class="d-flex flex-column h-100">
                            <c:if test="${not empty _csrf}">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            </c:if>
                            <input type="hidden" name="signatureData" id="signatureData">

                            <div class="canvas-wrapper mb-3">
                                <canvas id="signatureCanvas"></canvas>
                            </div>

                            <div class="mb-3 text-muted small">
                                마우스 또는 터치로 서명을 작성한 뒤 저장 버튼을 눌러주세요. <br>
                                서명 저장 후 계약 진행 시 자동으로 문서에 포함됩니다.
                            </div>

                            <div class="d-flex gap-2 mt-auto">
                                <button type="button" class="btn btn-outline-secondary" id="clearSignature">
                                    <i class="bi bi-eraser me-2"></i>지우기
                                </button>
                                <button type="submit" class="btn btn-primary ms-auto" id="saveSignature">
                                    <i class="bi bi-cloud-upload me-2"></i>서명 저장
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/signature_pad@4.1.5/dist/signature_pad.umd.min.js"></script>
    <c:if test="${hasSignature}">
        <fmt:formatDate value="${signature.updatedAtDate}" pattern="yyyy-MM-dd'T'HH:mm:ssXXX" var="ownerSignatureUpdatedAt" />
    </c:if>
    <c:choose>
        <c:when test="${hasSignature}">
            <script type="application/json" id="ownerSignatureDataJson">{"dataUrl":"${fn:escapeXml(signatureDataUrl)}","updatedAt":"${ownerSignatureUpdatedAt}"}</script>
        </c:when>
        <c:otherwise>
            <script type="application/json" id="ownerSignatureDataJson">null</script>
        </c:otherwise>
    </c:choose>
    <script>
        const canvas = document.getElementById('signatureCanvas');
        const signaturePad = new SignaturePad(canvas, {
            backgroundColor: 'rgba(0, 0, 0, 0)',
            penColor: '#1d4ed8',
        });

        function resizeCanvas() {
            const ratio = Math.max(window.devicePixelRatio || 1, 1);
            const wrapper = document.querySelector('.canvas-wrapper');
            const width = wrapper.clientWidth;
            const height = Math.max(220, wrapper.clientHeight);

            canvas.width = width * ratio;
            canvas.height = height * ratio;
            canvas.getContext('2d').scale(ratio, ratio);
            canvas.style.width = width + 'px';
            canvas.style.height = height + 'px';
            signaturePad.clear();
        }

        window.addEventListener('resize', resizeCanvas);
        resizeCanvas();

        document.getElementById('clearSignature').addEventListener('click', function () {
            signaturePad.clear();
        });

        const form = document.getElementById('signatureForm');
        form.addEventListener('submit', function (event) {
            if (signaturePad.isEmpty()) {
                event.preventDefault();
                alert('서명을 입력한 후 저장해 주세요.');
                return;
            }
            const dataUrl = signaturePad.toDataURL('image/png');
            document.getElementById('signatureData').value = dataUrl;
        });

        (function syncOwnerSignatureToStorage() {
            try {
                const signatureJsonElement = document.getElementById('ownerSignatureDataJson');
                if (!signatureJsonElement) {
                    localStorage.removeItem('signly_owner_signature');
                    return;
                }

                const raw = (signatureJsonElement.textContent || '').trim();
                if (!raw || raw === 'null') {
                    localStorage.removeItem('signly_owner_signature');
                    return;
                }

                const parsed = JSON.parse(raw);
                if (parsed && typeof parsed === 'object' && parsed.dataUrl) {
                    localStorage.setItem('signly_owner_signature', JSON.stringify(parsed));
                } else {
                    localStorage.removeItem('signly_owner_signature');
                }
            } catch (error) {
                console.warn('[WARN] 사업주 서명 정보를 localStorage에 저장하는 중 오류 발생:', error);
            }
        })();
    </script>

    <style>
        .canvas-wrapper {
            border: 2px dashed #cbd5f5;
            border-radius: 12px;
            background-color: #f8fafc;
            height: 260px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        #signatureCanvas {
            width: 100%;
            height: 100%;
        }
    </style>
</body>
</html>
