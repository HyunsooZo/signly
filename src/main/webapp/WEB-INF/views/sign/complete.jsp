<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title><c:out value="${pageTitle}"/> - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lobster&family=Noto+Sans+KR:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/signature.css" rel="stylesheet">


</head>
<body class="complete-page">

<!-- 간단한 헤더 (서명 페이지용) -->
<header class="header">
    <div class="header-container">
        <div class="header-logo">
            <a href="<c:url value='/' />" class="logo-link">
                <span class="logo-text">Signly</span>
            </a>
        </div>
        <div class="header-info">
            <span class="text-muted">안전한 전자서명</span>
        </div>
    </div>
</header>

<!-- 메인 컨텐츠 -->
<div class="container mt-4">
    <div class="main-content-card">
        <div class="row justify-content-center">
            <div class="col-lg-10">
                <div class="text-center mb-5">
                    <div class="success-icon mb-4">
                        <i class="bi bi-check-circle-fill" style="font-size: 5rem; color: var(--success-color);"></i>
                    </div>
                    <h2 class="fw-bold mb-3" style="color: var(--success-color);">서명 완료!</h2>
                    <p class="text-muted">계약서 전자서명이 성공적으로 완료되었습니다.</p>
                </div>

                <div class="card mb-4">
                    <div class="card-header" style="background-color: var(--success-color); color: white;">
                        <h5 class="mb-0">
                            <i class="bi bi-file-earmark-check me-2"></i>
                            서명 완료된 계약서
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-8">
                                <h5 class="mb-3"><c:out value="${contract.title}"/></h5>
                                <p class="text-muted mb-3">계약서가 정상적으로 서명되어 모든 당사자에게 통보되었습니다.</p>

                                <div class="row mb-4">
                                    <div class="col-sm-6 mb-3">
                                        <div class="p-3 bg-light rounded">
                                            <h6 class="fw-bold mb-2" style="color: var(--primary-color);">
                                                <i class="bi bi-building me-2"></i>갑 (계약당사자)
                                            </h6>
                                            <p class="mb-1"><c:out value="${contract.firstParty.name}"/></p>
                                            <small class="text-muted"><c:out value="${contract.firstParty.email}"/></small>
                                        </div>
                                    </div>
                                    <div class="col-sm-6 mb-3">
                                        <div class="p-3 bg-light rounded">
                                            <h6 class="fw-bold mb-2" style="color: var(--success-color);">
                                                <i class="bi bi-person-check me-2"></i>을 (서명 완료)
                                            </h6>
                                            <p class="mb-1"><c:out value="${contract.secondParty.name}"/></p>
                                            <small class="text-muted"><c:out value="${contract.secondParty.email}"/></small>
                                        </div>
                                    </div>
                                </div>

                                <div class="p-3 bg-light rounded">
                                    <h6 class="fw-bold mb-3">
                                        <i class="bi bi-info-circle me-2"></i>서명 정보
                                    </h6>
                                    <div class="row">
                                        <div class="col-sm-6">
                                            <p class="mb-2">
                                                <strong>서명 일시:</strong><br>
                                                <fmt:formatDate value="${now}" pattern="yyyy년 MM월 dd일 HH:mm:ss"/>
                                            </p>
                                        </div>
                                        <div class="col-sm-6">
                                            <p class="mb-2">
                                                <strong>서명 방식:</strong><br>
                                                전자서명 (디지털 서명)
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-md-4">
                                <div class="text-center">
                                    <div class="mb-3">
                                        <span class="badge bg-success fs-6 p-3">
                                            <i class="bi bi-check-circle me-2"></i>서명 완료
                                        </span>
                                    </div>
                                    <div class="border rounded p-4 bg-light">
                                        <i class="bi bi-file-earmark-pdf display-4 mb-3" style="color: var(--danger-color);"></i>
                                        <h6 class="fw-bold mb-3">계약서 PDF</h6>
                                        <a href="/contracts/<c:out value='${contract.id}'/>/pdf/download"
                                           class="btn btn-danger w-100 mb-2"
                                           download>
                                            <i class="bi bi-download me-2"></i>PDF 다운로드
                                        </a>
                                        <a href="/contracts/<c:out value='${contract.id}'/>/pdf/inline"
                                           class="btn btn-outline-secondary btn-sm w-100"
                                           target="_blank">
                                            <i class="bi bi-eye me-2"></i>미리보기
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="alert alert-info mb-4">
                    <h6 class="fw-bold mb-3">
                        <i class="bi bi-info-circle me-2"></i>서명 완료 안내
                    </h6>
                    <ul class="mb-0">
                        <li>서명된 계약서는 모든 당사자에게 이메일로 발송됩니다.</li>
                        <li>계약서는 법적 효력을 가지며, 전자서명법에 따라 보호됩니다.</li>
                        <li>서명 기록은 안전하게 보관되며, 필요 시 검증이 가능합니다.</li>
                        <li>계약서 PDF는 위의 다운로드 버튼을 통해 언제든지 내려받을 수 있습니다.</li>
                    </ul>
                </div>

                <div class="text-center">
                    <button type="button" class="btn btn-primary btn-lg me-3" onclick="window.print()">
                        <i class="bi bi-printer me-2"></i>인쇄하기
                    </button>
                    <button type="button" class="btn btn-secondary" onclick="window.close()">
                        <i class="bi bi-x-circle me-2"></i>창 닫기
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/js/common.js"></script>
<script>
    // 페이지 로드 시 성공 애니메이션
    document.addEventListener('DOMContentLoaded', function () {
        // 성공 아이콘 애니메이션
        setTimeout(function () {
            const icon = document.querySelector('.success-icon i');
            if (icon) {
                icon.style.animation = 'pulse 1s ease-in-out';
            }
        }, 500);

        // 5초 후 자동으로 알림창 숨김 (있는 경우)
        setTimeout(function () {
            const alerts = document.querySelectorAll('.alert:not(.alert-info)');
            alerts.forEach(alert => {
                const bsAlert = new bootstrap.Alert(alert);
                if (bsAlert) {
                    bsAlert.close();
                }
            });
        }, 5000);
    });
</script>

</body>
</html>
