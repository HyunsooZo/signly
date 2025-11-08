<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
                <p class="auth-subtitle">${email}의 새 비밀번호를 입력해주세요</p>
            </div>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert" data-auto-dismiss="true">
                    ${errorMessage}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <form action="/reset-password" method="post" class="auth-form" onsubmit="return validateForm()">
                <input type="hidden" name="token" value="${token}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

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
                showAlertModal(message);
                window.inputWarningShown = true;
                setTimeout(() => {
                    window.inputWarningShown = false;
                }, 3000);
            }
        }

        // 입력 상태 업데이트
        function updateInputStatus(input, statusId) {
            const status = document.getElementById(statusId);
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
        function toggleKeyboardIndicator(indicatorId, show) {
            const indicator = document.getElementById(indicatorId);
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
        function initializePasswordField(inputId, statusId, indicatorId) {
            const input = document.getElementById(inputId);
            
            // IME 제어 설정
            setupIMEControl(input);
            
            // 모바일 지원 설정
            setupMobileSupport(input);
            
            // keydown 이벤트 - 키 레벨 필터링
            input.addEventListener('keydown', function(e) {
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
            input.addEventListener('keypress', function(e) {
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
            input.addEventListener('input', function(e) {
                const originalValue = e.target.value;
                const cleanValue = cleanInput(originalValue);
                
                if (originalValue !== cleanValue) {
                    e.target.value = cleanValue;
                    
                    // 커서 위치 유지
                    const cursorPos = e.target.selectionStart;
                    e.target.setSelectionRange(cursorPos, cursorPos);
                    
                    showWarning('허용되지 않는 문자가 제거되었습니다.');
                }
                
                updateInputStatus(e.target, statusId);
            });
            
            // paste 이벤트 - 붙여넣기 제어
            input.addEventListener('paste', function(e) {
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
                
                updateInputStatus(this, statusId);
            });
            
            // 포커스 이벤트
            input.addEventListener('focus', function() {
                this.style.imeMode = 'disabled';
                toggleKeyboardIndicator(indicatorId, true);
            });
            
            input.addEventListener('blur', function() {
                toggleKeyboardIndicator(indicatorId, false);
            });
            
            // compositionstart 이벤트 - IME 시작 차단
            input.addEventListener('compositionstart', blockIME);
        }

        // 비밀번호 일치 검증
        function checkPasswordMatch() {
            const newPassword = document.getElementById('newPassword');
            const confirmPassword = document.getElementById('confirmPassword');
            const passwordMatch = document.getElementById('passwordMatch');

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
        }

        // 폼 검증
        function validateForm() {
            const newPassword = document.getElementById('newPassword');
            const confirmPassword = document.getElementById('confirmPassword');
            
            // 한글 입력 확인
            const cleanNewPassword = cleanInput(newPassword.value);
            const cleanConfirmPassword = cleanInput(confirmPassword.value);
            
            if (newPassword.value !== cleanNewPassword) {
                showAlertModal('새 비밀번호에 허용되지 않는 문자가 포함되어 있습니다.');
                return false;
            }
            
            if (confirmPassword.value !== cleanConfirmPassword) {
                showAlertModal('비밀번호 확인에 허용되지 않는 문자가 포함되어 있습니다.');
                return false;
            }
            
            if (newPassword.value.length < 8) {
                showAlertModal('비밀번호는 최소 8자 이상이어야 합니다');
                return false;
            }
            
            if (newPassword.value !== confirmPassword.value) {
                showAlertModal('비밀번호가 일치하지 않습니다');
                return false;
            }
            
            return true;
        }

        // 페이지 로드 시 초기화
        document.addEventListener('DOMContentLoaded', function() {
            // 비밀번호 입력 필드 초기화
            initializePasswordField('newPassword', 'newPassword-status', 'newPassword-keyboard-indicator');
            initializePasswordField('confirmPassword', 'confirmPassword-status', 'confirmPassword-keyboard-indicator');
            
            // 비밀번호 일치 검증 이벤트 리스너
            const confirmPassword = document.getElementById('confirmPassword');
            confirmPassword.addEventListener('input', checkPasswordMatch);
        });
    </script>
</body>
</html>
