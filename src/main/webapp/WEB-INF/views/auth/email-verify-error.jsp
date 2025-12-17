<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="ko">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>인증 실패 - Signly</title>
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
                            <i class="bi bi-exclamation-triangle-fill text-danger me-1"></i> 인증 실패
                        </p>
                    </div>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert"
                            data-auto-dismiss="true">
                            <i class="bi bi-exclamation-triangle me-2"></i>${errorMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <div class="email-content" style="text-align: center; padding: 20px 0;">
                        <div class="auth-icon-wrapper danger">
                            <i class="bi bi-alarm"></i>
                        </div>

                        <h3 style="color: #dc3545; margin-bottom: 15px;">이메일 인증에 실패했습니다</h3>

                        <p style="color: #3d5a4f; line-height: 1.6;">
                            인증 링크가 만료되었거나 유효하지 않습니다.<br>
                            아래 버튼을 클릭하여 인증 이메일을 다시 받으세요.
                        </p>

                        <div class="alert alert-warning" style="margin: 25px 0; text-align: left;">
                            <h6 class="alert-heading"><i class="bi bi-lightbulb me-2"></i>다음 사항을 확인해주세요</h6>
                            <ul style="margin-bottom: 0; padding-left: 20px; font-size: 0.9rem;">
                                <li>인증 링크가 24시간 이내에 발송된 것인지 확인하세요.</li>
                                <li>이메일의 최신 인증 링크를 클릭하세요.</li>
                                <li>링크가 완전히 복사되었는지 확인하세요.</li>
                            </ul>
                        </div>

                        <form action="/resend-verification" method="post" style="margin-top: 25px;">
                            <div class="form-group" style="margin-bottom: 15px;">
                                <input type="email" class="form-control" name="email" placeholder="가입하신 이메일을 입력하세요"
                                    required style="text-align: center;">
                            </div>
                            <button type="submit" class="btn btn-primary btn-lg" style="min-width: 200px;">
                                <i class="bi bi-envelope me-2"></i>인증 이메일 재발송
                            </button>
                        </form>
                    </div>

                    <div class="auth-footer" style="margin-top: 25px;">
                        <p>다른 문제가 있으신가요? <a href="/register">새로 가입하기</a></p>
                        <p>이미 인증을 완료하셨나요? <a href="/login">로그인</a></p>
                    </div>
                </div>
            </div>

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
            <script src="/js/common.js"></script>
            <script src="/js/alerts.js"></script>
        </body>

        </html>