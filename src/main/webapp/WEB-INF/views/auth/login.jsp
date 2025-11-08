<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인 - Signly</title>
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
        // 허용된 키 코드 목록
        const ALLOWED_KEYS = [
            // 백스페이스, 탭, 엔터, ESC
            8, 9, 13, 27,
            // 방향키, Delete, Home, End
            37, 38, 39, 40, 46, 35, 36,
            // 숫자 (0-9)
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
            // 숫자 (Numpad)
            96, 97, 98, 99, 100, 101, 102, 103, 104, 105,
            // 영문 대문자 (A-Z)
            65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
            // 특수문자
            186, 187, 188, 189, 190, 191, 192, 219, 220, 221, 222
        ];

        // 허용된 Ctrl 조합 키
        const ALLOWED_CTRL_KEYS = [65, 67, 86, 88, 90]; // A, C, V, X, Z

        // 한글 입력 감지 함수
        function containsKorean(text) {
            return /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/.test(text);
        }

        // 허용된 문자만 남기기
        function cleanInput(text) {
            return text.replace(/[^a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/g, '');
        }

        // 경고 메시지 표시
        function showWarning(message) {
            if (!window.inputWarningShown) {
                Signly.Alert.warning(message);
                window.inputWarningShown = true;
                setTimeout(() => {
                    window.inputWarningShown = false;
                }, 3000);
            }
        }

        // 입력 상태 업데이트
        function updateInputStatus(input) {
            const status = document.getElementById('password-status');
            const value = input.value;
            
            if (value.length === 0) {
                status.textContent = '';
                status.className = 'form-text';
            } else if (/^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]*$/.test(value)) {
                status.textContent = '✓ 영문/숫자/특수문자만 입력';
                status.className = 'form-text text-success';
            } else {
                status.textContent = '⚠ 허용되지 않는 문자가 제거되었습니다';
                status.className = 'form-text text-warning';
            }
        }

        // 키보드 표시기 토글
        function toggleKeyboardIndicator(show) {
            const indicator = document.getElementById('keyboard-indicator');
            indicator.style.display = show ? 'block' : 'none';
        }

        // IME 제어 설정
        function setupIMEControl(input) {
            const userAgent = navigator.userAgent.toLowerCase();
            
            if (userAgent.includes('chrome') || userAgent.includes('edge')) {
                input.style.imeMode = 'disabled';
            } else if (userAgent.includes('firefox')) {
                input.addEventListener('compositionstart', blockIME);
            } else if (userAgent.includes('safari')) {
                input.setAttribute('inputmode', 'latin');
            }
        }

        // IME 입력 차단
        function blockIME(e) {
            e.preventDefault();
            const input = e.target;
            input.blur();
            input.focus();
            showWarning('IME 입력이 차단되었습니다. 영문만 입력 가능합니다.');
        }

        // 모바일 지원 설정
        function setupMobileSupport(input) {
            const isMobile = /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
            
            if (isMobile) {
                input.setAttribute('inputmode', 'url');
                input.setAttribute('autocomplete', 'new-password');
                input.setAttribute('autocorrect', 'off');
                input.setAttribute('autocapitalize', 'off');
                input.setAttribute('spellcheck', 'false');
            }
        }

        // 비밀번호 입력 필드 초기화
        function initializePasswordField() {
            const passwordInput = document.getElementById('password');
            
            // IME 제어 설정
            setupIMEControl(passwordInput);
            
            // 모바일 지원 설정
            setupMobileSupport(passwordInput);
            
            // keydown 이벤트 - 키 레벨 필터링
            passwordInput.addEventListener('keydown', function(e) {
                // Ctrl 조합 키 허용
                if (e.ctrlKey && ALLOWED_CTRL_KEYS.includes(e.keyCode)) {
                    return true;
                }
                
                // 허용되지 않는 키 차단
                if (!ALLOWED_KEYS.includes(e.keyCode)) {
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                }
            });
            
            // keypress 이벤트 - 문자 레벨 필터링
            passwordInput.addEventListener('keypress', function(e) {
                const charCode = e.charCode;
                
                // 한글 문자 코드 범위 차단
                if (charCode >= 0xAC00 && charCode <= 0xD7A3) { // 가-힣
                    e.preventDefault();
                    return false;
                }
                
                // 한글 자음/모음 차단
                if ((charCode >= 0x3131 && charCode <= 0x3163) || // ㄱ-ㅣ
                    (charCode >= 0x1100 && charCode <= 0x1112)) { // 초성
                    e.preventDefault();
                    return false;
                }
            });
            
            // input 이벤트 - 입력 후처리
            passwordInput.addEventListener('input', function(e) {
                const originalValue = e.target.value;
                const cleanValue = cleanInput(originalValue);
                
                if (originalValue !== cleanValue) {
                    e.target.value = cleanValue;
                    
                    // 커서 위치 유지
                    const cursorPos = e.target.selectionStart;
                    e.target.setSelectionRange(cursorPos, cursorPos);
                    
                    showWarning('허용되지 않는 문자가 제거되었습니다.');
                }
                
                updateInputStatus(e.target);
            });
            
            // paste 이벤트 - 붙여넣기 제어
            passwordInput.addEventListener('paste', function(e) {
                e.preventDefault();
                
                const clipboardData = e.clipboardData || window.clipboardData;
                const pastedText = clipboardData.getData('text');
                const cleanText = cleanInput(pastedText);
                
                const start = this.selectionStart;
                const end = this.selectionEnd;
                const currentValue = this.value;
                
                this.value = currentValue.substring(0, start) + cleanText + currentValue.substring(end);
                this.setSelectionRange(start + cleanText.length, start + cleanText.length);
                
                if (pastedText !== cleanText) {
                    showWarning('붙여넣기에서 허용되지 않는 문자가 제거되었습니다.');
                }
                
                updateInputStatus(this);
            });
            
            // 포커스 이벤트
            passwordInput.addEventListener('focus', function() {
                this.style.imeMode = 'disabled';
                toggleKeyboardIndicator(true);
            });
            
            passwordInput.addEventListener('blur', function() {
                toggleKeyboardIndicator(false);
            });
            
            // compositionstart 이벤트 - IME 시작 차단
            passwordInput.addEventListener('compositionstart', blockIME);
        }

        // 페이지 로드 시 초기화
        document.addEventListener('DOMContentLoaded', function() {
            initializePasswordField();
        });

        // Enter 키로 로그인 처리
        document.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                const passwordInput = document.getElementById('password');
                const cleanValue = cleanInput(passwordInput.value);
                
                if (passwordInput.value !== cleanValue) {
                    e.preventDefault();
                    showWarning('비밀번호에 허용되지 않는 문자가 포함되어 있습니다.');
                    return;
                }
                document.querySelector('.auth-form').submit();
            }
        });
    </script>
</body>
</html>
