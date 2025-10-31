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

            <form action="/login" method="post" class="auth-form" modelAttribute="loginRequest">
                <div class="form-group">
                    <label for="email" class="form-label">이메일</label>
                    <input type="email" class="form-control" id="email" name="email"
                           value="${param.email}" required placeholder="your@email.com">
                </div>

                <div class="form-group">
                    <label for="password" class="form-label">비밀번호</label>
                    <input type="password" class="form-control" id="password" name="password"
                           required placeholder="비밀번호를 입력해주세요">
                    <div class="invalid-feedback" id="password-korean-error" style="display: none;">
                        비밀번호에는 한글을 사용할 수 없습니다. 영문, 숫자, 특수문자만 입력 가능합니다.
                    </div>
                    <small class="form-text text-muted">영문, 숫자, 특수문자만 입력 가능합니다</small>
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
        // 한글 입력 감지 함수
        function containsKorean(text) {
            return /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/.test(text);
        }

        // 비밀번호 한글 입력 검증
        function checkKoreanInput() {
            const passwordInput = document.getElementById('password');
            const koreanError = document.getElementById('password-korean-error');
            const value = passwordInput.value;

            if (containsKorean(value)) {
                // 한글이 포함된 경우
                passwordInput.classList.add('is-invalid');
                passwordInput.classList.remove('is-valid');
                koreanError.style.display = 'block';
                
                // 한글 제거
                const cleanValue = value.replace(/[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/g, '');
                passwordInput.value = cleanValue;
                
                // 경고 메시지 표시
                if (!window.koreanWarningShown) {
                    Signly.Alert.warning('비밀번호에는 한글을 사용할 수 없습니다. 영문, 숫자, 특수문자만 입력 가능합니다.');
                    window.koreanWarningShown = true;
                    
                    // 3초 후 경고 표시 플래그 리셋
                    setTimeout(() => {
                        window.koreanWarningShown = false;
                    }, 3000);
                }
            } else if (value.length > 0) {
                // 한글이 없고 내용이 있는 경우
                passwordInput.classList.remove('is-invalid');
                passwordInput.classList.add('is-valid');
                koreanError.style.display = 'none';
            } else {
                // 내용이 없는 경우
                passwordInput.classList.remove('is-invalid', 'is-valid');
                koreanError.style.display = 'none';
            }
        }

        // 비밀번호 입력 필드에 이벤트 리스너 추가
        document.addEventListener('DOMContentLoaded', function() {
            const passwordInput = document.getElementById('password');
            
            // 입력 시 실시간 검증
            passwordInput.addEventListener('input', checkKoreanInput);
            
            // 포커스 아웃 시 검증
            passwordInput.addEventListener('blur', checkKoreanInput);
            
            // IME 입력 완료 시 검증 (한글 입력 방지)
            passwordInput.addEventListener('compositionend', checkKoreanInput);
        });

        // Enter 키로 로그인 처리
        document.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                const passwordInput = document.getElementById('password');
                // 한글이 포함된 경우 로그인 방지
                if (containsKorean(passwordInput.value)) {
                    e.preventDefault();
                    checkKoreanInput();
                    return;
                }
                document.querySelector('.auth-form').submit();
            }
        });
    </script>
</body>
</html>
