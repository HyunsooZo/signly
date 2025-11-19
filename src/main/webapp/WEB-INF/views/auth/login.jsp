<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<jsp:include page="../common/header.jsp">
    <jsp:param name="pageTitle" value="로그인"/>
    <jsp:param name="additionalCss" value="/css/auth.css"/>
</jsp:include>
<body class="auth-page">
<div class="auth-container">
    <div class="auth-card">
        <div class="auth-header">
            <h1 class="auth-title">Signly</h1>
            <p class="auth-subtitle">전자계약 서비스 로그인</p>
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

        <form action="/login" method="post" class="auth-form" modelAttribute="loginRequest">
            <div class="form-group">
                <label for="email" class="form-label">이메일</label>
                <input type="email" class="form-control" id="email" name="email"
                       value="<c:out value="${param.email}"/>" required placeholder="your@email.com">
            </div>

            <div class="form-group">
                <label for="password" class="form-label">
                    비밀번호
                    <span class="visually-hidden">(영문, 숫자, 특수문자만 입력 가능)</span>
                </label>
                <input type="password"
                       class="form-control"
                       id="password"
                       name="password"
                       required
                       placeholder="비밀번호를 입력해주세요"
                       inputmode="latin"
                       style="ime-mode: disabled"
                       autocomplete="new-password"
                       autocorrect="off"
                       autocapitalize="off"
                       spellcheck="false"
                       aria-describedby="password-help password-status"
                       aria-label="비밀번호 영문 전용 입력 필드">
                <div class="invalid-feedback" id="password-korean-error" style="display: none;">
                    비밀번호에는 한글을 사용할 수 없습니다. 영문, 숫자, 특수문자만 입력 가능합니다.
                </div>
                <div id="password-help" class="form-text text-muted">
                    영문, 숫자, 특수문자만 입력 가능합니다
                </div>
                <div id="password-status" class="form-text" aria-live="polite"></div>
                <div id="keyboard-indicator" class="keyboard-indicator" style="display: none;">
                    <div class="indicator-title">입력 모드</div>
                    <div class="indicator-status">영문 전용</div>
                    <div class="indicator-keys">
                        <span class="key">A-Z</span>
                        <span class="key">0-9</span>
                        <span class="key">!@#$</span>
                    </div>
                </div>
            </div>

            <div class="form-group form-check">
                <input type="checkbox" class="form-check-input" id="rememberMe" name="rememberMe" checked>
                <label class="form-check-label" for="rememberMe">
                    자동 로그인
                </label>
            </div>

            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
            <c:if test="${not empty returnUrl}">
                <input type="hidden" name="returnUrl" value="<c:out value="${returnUrl}"/>"/>
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

<jsp:include page="../common/footer.jsp"/>
<script src="/js/auth-pages.js"></script>
</body>
</html>
