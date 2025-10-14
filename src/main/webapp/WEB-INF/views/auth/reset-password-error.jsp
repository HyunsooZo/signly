<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>비밀번호 재설정 오류 - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/auth.css" rel="stylesheet">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <div class="auth-header">
                <h1 class="auth-title">비밀번호 재설정 오류</h1>
            </div>

            <div class="alert alert-danger" role="alert">
                <h5 class="alert-heading">오류가 발생했습니다</h5>
                <p>${not empty errorMessage ? errorMessage : '유효하지 않거나 만료된 비밀번호 재설정 링크입니다'}</p>
                <hr>
                <p class="mb-0">비밀번호 재설정 링크는 24시간 동안만 유효합니다.</p>
            </div>

            <div class="text-center mt-4">
                <a href="/forgot-password" class="btn btn-primary">비밀번호 재설정 다시 요청</a>
                <a href="/login" class="btn btn-outline-secondary">로그인 페이지로</a>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
