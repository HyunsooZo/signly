<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>오류 - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/signature.css" rel="stylesheet">
</head>
<body class="error-page">
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="text-center">
                <div class="error-icon mb-4">
                    <i class="bi bi-exclamation-triangle-fill text-danger"></i>
                </div>
                <h1 class="display-6 fw-bold text-danger mb-3">서명 오류</h1>
                <p class="lead text-muted mb-4">
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

            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">
                        <i class="bi bi-info-circle me-2"></i>가능한 원인
                    </h5>
                    <ul class="list-unstyled">
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
                        <li class="mb-2">
                            <i class="bi bi-arrow-right text-primary me-2"></i>
                            잘못된 접근 권한
                        </li>
                    </ul>
                </div>
            </div>

            <div class="text-center mt-4">
                <p class="text-muted">문제가 지속되면 계약 담당자에게 문의하세요.</p>
                <button type="button" class="btn btn-primary me-2" onclick="history.back()">
                    <i class="bi bi-arrow-left me-2"></i>이전 페이지
                </button>
                <button type="button" class="btn btn-outline-secondary" onclick="window.close()">
                    <i class="bi bi-x-circle me-2"></i>창 닫기
                </button>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
