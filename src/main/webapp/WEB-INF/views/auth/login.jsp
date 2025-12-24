<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="ko">
        <jsp:include page="../common/header.jsp">
            <jsp:param name="pageTitle" value="로그인" />
            <jsp:param name="additionalCss" value="/css/auth.css" />
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
                            <c:when test="${isAccountLocked}">
                                <div class="alert alert-danger alert-dismissible fade show" role="alert"
                                    data-auto-dismiss="false">
                                    <strong>🔒 계정 잠금</strong>
                                    <p class="mb-2">
                                        <c:out value="${errorMessage}" />
                                    </p>
                                    <div class="mt-3">
                                        <a href="/account-locked" class="btn btn-sm btn-outline-danger">
                                            잠금 해제 방법 확인
                                        </a>
                                    </div>
                                    <button type="button" class="btn-close" data-bs-dismiss="alert"
                                        aria-label="Close"></button>
                                </div>
                            </c:when>
                            <c:when test="${isPendingUser}">
                                <div class="alert alert-warning alert-dismissible fade show" role="alert"
                                    data-auto-dismiss="false">
                                    <strong>⚠️ 이메일 인증 필요</strong>
                                    <p class="mb-2">
                                        <c:out value="${errorMessage}" />
                                    </p>
                                    <c:if test="${showResendButton}">
                                        <form action="/resend-verification" method="post" style="display: inline;">
                                            <input type="hidden" name="email" value="<c:out value=" ${email}" />"/>
                                            <button type="submit" class="btn btn-sm btn-outline-primary">
                                                📧 인증 메일 재전송
                                            </button>
                                        </form>
                                    </c:if>
                                    <button type="button" class="btn-close" data-bs-dismiss="alert"
                                        aria-label="Close"></button>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="alert alert-danger alert-dismissible fade show" role="alert"
                                    data-auto-dismiss="true">
                                    <c:out value="${errorMessage}" />
                                    <c:if test="${not empty remainingAttempts and remainingAttempts > 0}">
                                        <small class="d-block mt-2 text-muted">
                                            남은 로그인 시도 횟수: <strong>${remainingAttempts}</strong> 회
                                        </small>
                                    </c:if>
                                    <button type="button" class="btn-close" data-bs-dismiss="alert"
                                        aria-label="Close"></button>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:if>

                    <c:if test="${not empty successMessage}">
                        <div class="alert alert-success alert-dismissible fade show" role="alert"
                            data-auto-dismiss="true">
                            <c:out value="${successMessage}" />
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <form action="/login" method="post" class="auth-form" modelAttribute="loginRequest">
                        <div class="form-group">
                            <label for="email" class="form-label">이메일</label>
                            <input type="email" class="form-control" id="email" name="email" value="<c:out value="
                                ${param.email}" />" required placeholder="your@email.com">
                        </div>

                        <div class="form-group">
                            <label for="password" class="form-label">
                                비밀번호
                                <span class="visually-hidden">(영문, 숫자, 특수문자만 입력 가능)</span>
                            </label>
                            <input type="password" class="form-control" id="password" name="password" required
                                placeholder="비밀번호를 입력해주세요" inputmode="latin" style="ime-mode: disabled"
                                autocomplete="new-password" autocorrect="off" autocapitalize="off" spellcheck="false"
                                aria-describedby="password-help password-status" aria-label="비밀번호 영문 전용 입력 필드">
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
                            <input type="hidden" name="returnUrl" value="<c:out value=" ${returnUrl}" />"/>
                        </c:if>

                        <button type="submit" class="btn btn-primary btn-auth">로그인</button>
                    </form>

                    <div class="oauth-divider">
                        <span>또는</span>
                    </div>

                    <a href="/oauth2/authorization/google" class="btn btn-google btn-auth"
                        onclick="handleGoogleLogin(event)">
                        <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg"
                            style="margin-right: 8px;">
                            <path fill="#4285F4"
                                d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.874 2.684-6.615z" />
                            <path fill="#34A853"
                                d="M9 18c2.43 0 4.467-.806 5.956-2.184l-2.908-2.258c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z" />
                            <path fill="#FBBC05"
                                d="M3.964 10.707c-.18-.54-.282-1.117-.282-1.707s.102-1.167.282-1.707V4.961H.957C.347 6.175 0 7.55 0 9s.348 2.825.957 4.039l3.007-2.332z" />
                            <path fill="#EA4335"
                                d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.961L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z" />
                        </svg>
                        Google로 계속하기
                    </a>

                    <a href="/oauth2/authorization/naver" class="btn btn-naver btn-auth"
                        onclick="handleNaverLogin(event)">
                        <svg width="18" height="18" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"
                            style="margin-right: 8px;">
                            <rect width="24" height="24" rx="4" fill="#03C75A" />
                            <path d="M16.273 12.845L7.376 0H0v18h7.727V5.155L16.624 18H24V0h-7.727v12.845z" fill="white"
                                transform="translate(4.8, 6.6) scale(0.6)" />
                        </svg>
                        네이버로 계속하기
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

            <jsp:include page="../common/footer.jsp" />
            <script src="/js/auth-pages.js"></script>
            <script>
                // 전역 스토리지 정리 함수
                function clearAllAuthStorage() {
                    console.log('[INFO] 인증 스토리지 정리 시작');

                    // LocalStorage 정리
                    const keysToRemove = [];
                    for (let i = 0; i < localStorage.length; i++) {
                        const key = localStorage.key(i);
                        // signly_ 로 시작하는 모든 키 + 기타 인증 관련 키
                        if (key && (key.startsWith('signly_') ||
                                   key.includes('token') ||
                                   key.includes('auth') ||
                                   key.includes('user'))) {
                            keysToRemove.push(key);
                        }
                    }
                    keysToRemove.forEach(key => localStorage.removeItem(key));

                    // SessionStorage 완전 초기화
                    sessionStorage.clear();

                    console.log('[INFO] 인증 스토리지 정리 완료:', keysToRemove);
                }

                // OAuth2 로그인 전 스토리지 정리 공통 함수
                function clearAuthStorageForOAuth2(provider) {
                    console.log('[INFO] ' + provider + ' OAuth2 로그인 - 스토리지 정리 시작');
                    clearAllAuthStorage();
                    console.log('[INFO] ' + provider + ' OAuth2 로그인 - 스토리지 정리 완료');
                    return true;
                }

                // 페이지 로드 시 자동 정리
                document.addEventListener('DOMContentLoaded', function() {
                    const successMessage = '<c:out value="${successMessage}" />';
                    const errorMessage = '<c:out value="${errorMessage}" />';

                    // 로그아웃 시 스토리지 정리
                    if (successMessage && successMessage.includes('로그아웃')) {
                        console.log('[INFO] 로그아웃 감지 - 스토리지 정리');
                        clearAllAuthStorage();
                    }

                    // 로그인 실패 시 스토리지 정리 (오래된 인증 정보 제거)
                    if (errorMessage && errorMessage.length > 0) {
                        console.log('[INFO] 로그인 실패 감지 - 스토리지 정리');
                        clearAllAuthStorage();

                        // 쿠키도 정리
                        document.cookie = 'authToken=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
                        document.cookie = 'refreshToken=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
                        console.log('[INFO] 쿠키 및 스토리지 정리 완료');
                    }
                });

                // Google 로그인 전 스토리지 정리
                function handleGoogleLogin(event) {
                    return clearAuthStorageForOAuth2('Google');
                }

                // 네이버 로그인 전 스토리지 정리
                function handleNaverLogin(event) {
                    return clearAuthStorageForOAuth2('Naver');
                }
            </script>
        </body>

        </html>