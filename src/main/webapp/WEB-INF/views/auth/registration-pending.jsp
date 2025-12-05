<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>이메일 확인 - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Lobster&display=swap" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/auth.css" rel="stylesheet">
</head>
<body>
<div class="auth-container">
    <div class="auth-card auth-card--compact">
        <div class="auth-header">
            <h1 class="auth-title">Signly</h1>
            <p class="auth-subtitle">📧 이메일 확인 필요</p>
        </div>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success alert-dismissible fade show" role="alert" data-auto-dismiss="true">
                <i class="bi bi-check-circle me-2"></i>${successMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <div class="email-content" style="text-align: center; padding: 20px 0;">
            <div style="font-size: 4rem; margin-bottom: 20px;">📬</div>
            
            <h3 style="color: #2d4a3f; margin-bottom: 15px;">회원가입이 완료되었습니다!</h3>
            
            <p style="color: #3d5a4f; line-height: 1.6;">
                이메일로 발송된 <strong>인증 링크</strong>를 클릭하여<br>
                가입을 완료해주세요.
            </p>

            <div class="alert alert-info" style="margin: 25px 0; text-align: left;">
                <h6 class="alert-heading"><i class="bi bi-info-circle me-2"></i>안내사항</h6>
                <ul style="margin-bottom: 0; padding-left: 20px;">
                    <li>인증 링크는 <strong>24시간</strong> 동안 유효합니다.</li>
                    <li>이메일을 받지 못하신 경우 스팸함을 확인해주세요.</li>
                    <li>인증을 완료해야 로그인이 가능합니다.</li>
                </ul>
            </div>

            <c:if test="${not empty userEmail}">
                <form action="/resend-verification" method="post" style="margin-top: 25px;">
                    <input type="hidden" name="email" value="${userEmail}">
                    <p style="color: #666; margin-bottom: 10px;">이메일을 받지 못하셨나요?</p>
                    <button type="submit" class="btn btn-outline-primary">
                        <i class="bi bi-envelope me-2"></i>인증 이메일 재발송
                    </button>
                </form>
            </c:if>
        </div>

        <div class="auth-footer" style="margin-top: 25px;">
            <p>이미 인증을 완료하셨나요? <a href="/login">로그인</a></p>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="/js/common.js"></script>
<script src="/js/alerts.js"></script>
</body>
</html>
