<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>비밀번호 재설정 - Signly</title>
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
            <h1 class="auth-title">새 비밀번호 설정</h1>
            <p class="auth-subtitle"><c:out value="${email}"/>의 새 비밀번호를 입력해주세요</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert" data-auto-dismiss="true">
                <c:out value="${errorMessage}"/>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <form action="/reset-password" method="post" class="auth-form" onsubmit="return validateForm()">
            <input type="hidden" name="token" value="<c:out value="${token}"/>">
            <input type="hidden" name="<c:out value="${_csrf.parameterName}"/>"
                   value="<c:out value="${_csrf.token}"/>"/>

            <div class="form-group">
                <label for="newPassword" class="form-label">
                    새 비밀번호
                    <span class="visually-hidden">(영문, 숫자, 특수문자만 입력 가능)</span>
                </label>
                <input type="password"
                       class="form-control"
                       id="newPassword"
                       name="newPassword"
                       required
                       minlength="8"
                       placeholder="8자 이상 입력하세요"
                       inputmode="latin"
                       style="ime-mode: disabled"
                       autocomplete="new-password"
                       autocorrect="off"
                       autocapitalize="off"
                       spellcheck="false"
                       aria-describedby="newPassword-help newPassword-status"
                       aria-label="새 비밀번호 영문 전용 입력 필드">
                <div class="invalid-feedback" id="newPassword-korean-error" style="display: none;">
                    비밀번호에는 한글을 사용할 수 없습니다. 영문, 숫자, 특수문자만 입력 가능합니다.
                </div>
                <div id="newPassword-help" class="form-text text-muted">
                    최소 8자 이상이어야 하며, 영문, 숫자, 특수문자만 입력 가능합니다
                </div>
                <div id="newPassword-status" class="form-text" aria-live="polite"></div>
                <div id="newPassword-keyboard-indicator" class="keyboard-indicator" style="display: none;">
                    <div class="indicator-title">입력 모드</div>
                    <div class="indicator-status">영문 전용</div>
                    <div class="indicator-keys">
                        <span class="key">A-Z</span>
                        <span class="key">0-9</span>
                        <span class="key">!@#$</span>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label for="confirmPassword" class="form-label">
                    새 비밀번호 확인
                    <span class="visually-hidden">(영문, 숫자, 특수문자만 입력 가능)</span>
                </label>
                <input type="password"
                       class="form-control"
                       id="confirmPassword"
                       name="confirmPassword"
                       required
                       placeholder="비밀번호를 다시 입력하세요"
                       inputmode="latin"
                       style="ime-mode: disabled"
                       autocomplete="new-password"
                       autocorrect="off"
                       autocapitalize="off"
                       spellcheck="false"
                       aria-describedby="confirmPassword-help confirmPassword-status passwordMatch"
                       aria-label="새 비밀번호 확인 영문 전용 입력 필드">
                <div class="invalid-feedback" id="confirmPassword-korean-error" style="display: none;">
                    비밀번호에는 한글을 사용할 수 없습니다. 영문, 숫자, 특수문자만 입력 가능합니다.
                </div>
                <div id="confirmPassword-help" class="form-text text-muted">
                    영문, 숫자, 특수문자만 입력 가능합니다
                </div>
                <div id="confirmPassword-status" class="form-text" aria-live="polite"></div>
                <div id="confirmPassword-keyboard-indicator" class="keyboard-indicator" style="display: none;">
                    <div class="indicator-title">입력 모드</div>
                    <div class="indicator-status">영문 전용</div>
                    <div class="indicator-keys">
                        <span class="key">A-Z</span>
                        <span class="key">0-9</span>
                        <span class="key">!@#$</span>
                    </div>
                </div>
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
<script src="/js/alerts.js"></script>
<script src="/js/auth-pages.js"></script>
</body>
</html>
