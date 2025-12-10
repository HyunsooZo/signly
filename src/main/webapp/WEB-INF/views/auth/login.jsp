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
            <c:choose>
                <c:when test="${isPendingUser}">
                    <div class="alert alert-warning alert-dismissible fade show" role="alert" data-auto-dismiss="false">
                        <strong>⚠️ 이메일 인증 필요</strong>
                        <p class="mb-2"><c:out value="${errorMessage}"/></p>
                        <c:if test="${showResendButton}">
                            <form action="/resend-verification" method="post" style="display: inline;">
                                <input type="hidden" name="email" value="<c:out value="${email}"/>"/>
                                <button type="submit" class="btn btn-sm btn-outline-primary">
                                    📧 인증 메일 재전송
                                </button>
                            </form>
                        </c:if>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-danger alert-dismissible fade show" role="alert" data-auto-dismiss="true">
                        <c:out value="${errorMessage}"/>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:otherwise>
            </c:choose>
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


            <c:if test="${not empty returnUrl}">
                <input type="hidden" name="returnUrl" value="<c:out value="${returnUrl}"/>"/>
            </c:if>

            <button type="submit" class="btn btn-primary btn-auth">로그인</button>
        </form>

        <div class="oauth-divider">
            <span>또는</span>
        </div>

        <a href="/oauth2/authorization/google" class="btn btn-google btn-auth">
            <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg" style="margin-right: 8px;">
                <path fill="#4285F4" d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.874 2.684-6.615z"/>
                <path fill="#34A853" d="M9 18c2.43 0 4.467-.806 5.956-2.184l-2.908-2.258c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z"/>
                <path fill="#FBBC05" d="M3.964 10.707c-.18-.54-.282-1.117-.282-1.707s.102-1.167.282-1.707V4.961H.957C.347 6.175 0 7.55 0 9s.348 2.825.957 4.039l3.007-2.332z"/>
                <path fill="#EA4335" d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.961L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z"/>
            </svg>
            Google로 계속하기
        </a>

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
