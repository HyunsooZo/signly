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
<body class="verify-page">

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
            <div class="col-md-8 col-lg-6">
                <div class="text-center mb-4">
                    <div class="mb-3">
                        <i class="bi bi-shield-check" style="font-size: 3rem; color: var(--primary-color);"></i>
                    </div>
                    <h2 class="mb-2">서명자 인증</h2>
                    <p class="text-muted">서명 전 본인 확인을 위해 정보를 입력해주세요.</p>
                </div>

                <!-- 계약서 정보 -->
                <div class="card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">계약서 정보</h5>
                    </div>
                    <div class="card-body">
                        <h6 class="fw-bold"><c:out value="${contract.title}"/></h6>
                        <p class="text-muted mb-0 small">본 계약서에 서명하기 위해 본인 확인이 필요합니다.</p>
                    </div>
                </div>

                <!-- 알림 메시지 -->
                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="bi bi-exclamation-triangle me-2"></i><c:out value="${errorMessage}"/>
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

                <!-- 인증 폼 -->
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">본인 확인</h5>
                    </div>
                    <div class="card-body">
                        <form method="post" action="/sign/<c:out value='${token}'/>/verify">

                            <div class="mb-3">
                                <label for="signerEmail" class="form-label">
                                    <i class="bi bi-envelope me-2"></i>이메일 주소 <span class="text-danger">*</span>
                                </label>
                                <input type="email" class="form-control" id="signerEmail" name="signerEmail"
                                       placeholder="<c:out value='${contract.secondParty.email}'/>" required>
                                <div class="form-text">계약서에 등록된 이메일 주소를 입력하세요.</div>
                            </div>

                            <div class="mb-4">
                                <label for="signerName" class="form-label">
                                    <i class="bi bi-person me-2"></i>성명 <span class="text-danger">*</span>
                                </label>
                                <input type="text" class="form-control" id="signerName" name="signerName"
                                       placeholder="<c:out value='${contract.secondParty.name}'/>" required>
                                <div class="form-text">계약서에 등록된 성명을 입력하세요.</div>
                            </div>

                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-primary btn-lg">
                                    <i class="bi bi-check-circle me-2"></i>인증 완료
                                </button>
                                <button type="button" class="btn btn-secondary" onclick="window.close()">
                                    <i class="bi bi-x-circle me-2"></i>취소
                                </button>
                            </div>
                        </form>
                    </div>
                    <div class="card-footer text-center text-muted">
                        <small>
                            <i class="bi bi-shield-lock me-1"></i>
                            본 인증은 전자서명법에 따른 본인 확인 절차입니다.
                        </small>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/js/common.js"></script>
</body>
</html>
