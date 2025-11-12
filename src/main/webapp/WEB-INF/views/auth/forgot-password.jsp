<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>비밀번호 찾기 - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lobster&display=swap" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/auth.css" rel="stylesheet">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <div class="auth-header">
                <h1 class="auth-title">비밀번호 찾기</h1>
                <p class="auth-subtitle">가입하신 이메일로 비밀번호 재설정 링크를 보내드립니다</p>
            </div>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert" data-auto-dismiss="true">
                    <c:out value="${errorMessage}"/>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <c:if test="${not empty successMessage}">
                <div class="alert alert-success alert-dismissible fade show" role="alert" data-auto-dismiss="true">
                    <c:out value="${successMessage}"/>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <form action="/forgot-password" method="post" class="auth-form">
                <div class="form-group">
                    <label for="email" class="form-label">이메일</label>
                    <input type="email" class="form-control" id="email" name="email"
                           value="<c:out value="${param.email}"/>" required placeholder="가입하신 이메일을 입력해주세요">
                </div>

                <input type="hidden" name="<c:out value="${_csrf.parameterName}"/>" value="<c:out value="${_csrf.token}"/>"/>

                <button type="submit" class="btn btn-primary btn-auth">비밀번호 재설정 링크 발송</button>
            </form>

            <div class="auth-footer">
                <p>로그인 페이지로 <a href="/login">돌아가기</a></p>
            </div>
        </div>
    </div>

    <div class="auth-container mt-4">
        <div class="auth-card auth-card--compact">
            <div class="alert alert-info" role="alert">
                <strong>개발 모드 안내</strong><br>
                현재 개발 중이므로 실제 이메일 발송은 되지 않습니다.<br>
                테스트 계정을 이용해 로그인해주세요.
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="/js/common.js"></script>
    <script src="/js/alerts.js"></script>
</body>
</html>
