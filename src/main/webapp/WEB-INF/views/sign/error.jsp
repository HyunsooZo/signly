<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>오류 - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lobster&family=Noto+Sans+KR:wght@400;500;700&display=swap" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/signature.css" rel="stylesheet">

    <c:if test="${not empty _csrf}">
        <meta name="_csrf" content="${_csrf.token}"/>
        <meta name="_csrf_header" content="${_csrf.headerName}"/>
        <meta name="_csrf_parameter" content="${_csrf.parameterName}"/>
    </c:if>
    <script src="/js/common/csrf.js" defer></script>
</head>
<body class="error-page">

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
                    <div class="error-icon mb-4">
                        <i class="bi bi-exclamation-triangle-fill" style="font-size: 4rem; color: var(--danger-color);"></i>
                    </div>
                    <h2 class="mb-3" style="color: var(--danger-color);">서명 오류</h2>
                    <p class="text-muted mb-4">
                        <c:choose>
                            <c:when test="${not empty errorMessage}">
                                <c:out value="${errorMessage}"/>
                            </c:when>
                            <c:otherwise>
                                요청하신 계약서에 접근할 수 없습니다.
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>

                <div class="card mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">
                            <i class="bi bi-info-circle me-2"></i>가능한 원인
                        </h5>
                    </div>
                    <div class="card-body">
                        <ul class="list-unstyled mb-0">
                            <li class="mb-2">
                                <i class="bi bi-arrow-right text-primary me-2"></i>
                                만료된 서명 링크
                            </li>
                            <li class="mb-2">
                                <i class="bi bi-arrow-right text-primary me-2"></i>
                                이미 서명이 완료된 계약서
                            </li>
                            <li class="mb-2">
                                <i class="bi bi-arrow-right text-primary me-2"></i>
                                취소된 계약서
                            </li>
                            <li class="mb-0">
                                <i class="bi bi-arrow-right text-primary me-2"></i>
                                잘못된 접근 권한
                            </li>
                        </ul>
                    </div>
                </div>

                <div class="alert alert-info mb-4">
                    <i class="bi bi-info-circle me-2"></i>
                    문제가 지속되면 계약 담당자에게 문의하세요.
                </div>

                <div class="text-center">
                    <button type="button" class="btn btn-primary btn-lg me-2" onclick="history.back()">
                        <i class="bi bi-arrow-left me-2"></i>이전 페이지
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
</body>
</html>
