<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>비밀번호 재설정 - Signly</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/common.css" rel="stylesheet">
    <link href="/css/auth.css" rel="stylesheet">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <div class="auth-header">
                <h1 class="auth-title">새 비밀번호 설정</h1>
                <p class="auth-subtitle">${email}의 새 비밀번호를 입력해주세요</p>
            </div>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger" role="alert">
                    ${errorMessage}
                </div>
            </c:if>

            <form action="/reset-password" method="post" class="auth-form" onsubmit="return validateForm()">
                <input type="hidden" name="token" value="${token}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <div class="form-group">
                    <label for="newPassword" class="form-label">새 비밀번호</label>
                    <input type="password" class="form-control" id="newPassword" name="newPassword"
                           required minlength="8" placeholder="8자 이상 입력하세요">
                    <small class="form-text text-muted">최소 8자 이상이어야 합니다</small>
                </div>

                <div class="form-group">
                    <label for="confirmPassword" class="form-label">새 비밀번호 확인</label>
                    <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                           required placeholder="비밀번호를 다시 입력하세요">
                    <small id="passwordMatch" class="form-text"></small>
                </div>

                <button type="submit" class="btn btn-primary btn-auth">비밀번호 변경</button>
            </form>

            <div class="auth-footer">
                <p>로그인 페이지로 <a href="/login">돌아가기</a></p>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const newPassword = document.getElementById('newPassword');
        const confirmPassword = document.getElementById('confirmPassword');
        const passwordMatch = document.getElementById('passwordMatch');

        confirmPassword.addEventListener('input', function() {
            if (confirmPassword.value === '') {
                passwordMatch.textContent = '';
                passwordMatch.className = 'form-text';
            } else if (newPassword.value === confirmPassword.value) {
                passwordMatch.textContent = '비밀번호가 일치합니다';
                passwordMatch.className = 'form-text text-success';
            } else {
                passwordMatch.textContent = '비밀번호가 일치하지 않습니다';
                passwordMatch.className = 'form-text text-danger';
            }
        });

        function validateForm() {
            if (newPassword.value.length < 8) {
                alert('비밀번호는 최소 8자 이상이어야 합니다');
                return false;
            }
            if (newPassword.value !== confirmPassword.value) {
                alert('비밀번호가 일치하지 않습니다');
                return false;
            }
            return true;
        }
    </script>
</body>
</html>
