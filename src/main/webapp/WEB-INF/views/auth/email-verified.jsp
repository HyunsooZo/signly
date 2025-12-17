<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="ko">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>인증 완료 - Signly</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
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
                        <p class="auth-subtitle">
                            <i class="bi bi-check-circle-fill text-success me-1"></i> 인증 완료!
                        </p>
                    </div>

                    <c:if test="${not empty successMessage}">
                        <div class="alert alert-success alert-dismissible fade show" role="alert"
                            data-auto-dismiss="true">
                            <i class="bi bi-check-circle me-2"></i>${successMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <div class="email-content" style="text-align: center; padding: 20px 0;">
                        <div class="auth-icon-wrapper success" style="animation: bounce 1s;">
                            <i class="bi bi-stars"></i>
                        </div>

                        <h3 style="color: #28a745; margin-bottom: 15px;">이메일 인증이 완료되었습니다!</h3>

                        <p style="color: #3d5a4f; line-height: 1.6; margin-bottom: 30px;">
                            이제 Signly의 모든 서비스를 이용하실 수 있습니다.<br>
                            로그인하여 전자계약을 시작해보세요.
                        </p>

                        <a href="/login" class="btn btn-primary btn-lg" style="min-width: 200px;">
                            <i class="bi bi-box-arrow-in-right me-2"></i>로그인하기
                        </a>
                    </div>

                    <div class="auth-footer" style="margin-top: 30px;">
                        <p style="color: #666; font-size: 0.9rem;">
                            <i class="bi bi-shield-check me-1"></i>
                            계정이 안전하게 활성화되었습니다
                        </p>
                    </div>
                </div>
            </div>

            <style>
                @keyframes bounce {

                    0%,
                    20%,
                    50%,
                    80%,
                    100% {
                        transform: translateY(0);
                    }

                    40% {
                        transform: translateY(-15px);
                    }

                    60% {
                        transform: translateY(-7px);
                    }
                }
            </style>

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
            <script src="/js/common.js"></script>
            <script src="/js/alerts.js"></script>
        </body>

        </html>