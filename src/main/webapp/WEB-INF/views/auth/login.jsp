<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인 - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/auth.css" rel="stylesheet">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <div class="auth-header">
                <h1 class="auth-title">Signly</h1>
                <p class="auth-subtitle">전자계약 서비스 로그인</p>
            </div>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert" data-auto-dismiss="true">
                    ${errorMessage}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <c:if test="${not empty successMessage}">
                <div class="alert alert-success alert-dismissible fade show" role="alert" data-auto-dismiss="true">
                    ${successMessage}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <form action="/login" method="post" class="auth-form">
                <div class="form-group">
                    <label for="email" class="form-label">이메일</label>
                    <input type="email" class="form-control" id="email" name="email"
                           value="${param.email}" required placeholder="your@email.com">
                </div>

                <div class="form-group">
                    <label for="password" class="form-label">비밀번호</label>
                    <input type="password" class="form-control" id="password" name="password"
                           required placeholder="비밀번호를 입력해주세요">
                </div>

                <div class="form-group form-check">
                    <input type="checkbox" class="form-check-input" id="rememberMe" name="rememberMe">
                    <label class="form-check-label" for="rememberMe">
                        자동 로그인
                    </label>
                </div>

                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                <c:if test="${not empty returnUrl}">
                    <input type="hidden" name="returnUrl" value="${returnUrl}"/>
                </c:if>

                <button type="submit" class="btn btn-primary btn-auth">로그인</button>
            </form>

            <div class="auth-footer">
                <div class="auth-links">
                    <a href="/forgot-password">비밀번호를 잊으셨나요?</a>
                </div>
                <hr class="auth-divider">
                <p>아직 계정이 없으신가요? <a href="/register">회원가입</a></p>
            </div>
        </div>
    </div>



    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="/js/common.js"></script>
    <script src="/js/alerts.js"></script>
    <script>
        // Enter 키로 로그인 처리
        document.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                document.querySelector('.auth-form').submit();
            }
        });
    </script>
</body>
</html>
