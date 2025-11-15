/**
 * Authentication Pages JavaScript
 * Handles login, register, and reset password functionality
 */

class AuthPages {
    constructor() {
        this.ALLOWED_KEYS = [
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

        this.ALLOWED_CTRL_KEYS = [65, 67, 86, 88, 90]; // A, C, V, X, Z

        this.init();
    }

    init() {
        this.detectPageType();
        this.setupEventListeners();
    }

    detectPageType() {
        const path = window.location.pathname;
        if (path.includes('/login')) {
            this.pageType = 'login';
            this.initializeLoginPage();
        } else if (path.includes('/register')) {
            this.pageType = 'register';
            this.initializeRegisterPage();
        } else if (path.includes('/reset-password')) {
            this.pageType = 'reset-password';
            this.initializeResetPasswordPage();
        }
    }

    initializeLoginPage() {
        document.addEventListener('DOMContentLoaded', () => {
            this.initializePasswordField('password');
            this.setupEnterKeyHandler();
        });
    }

    initializeRegisterPage() {
        document.addEventListener('DOMContentLoaded', () => {
            this.initializePasswordField('password', 'password-status');
            this.initializePasswordField('confirmPassword', 'confirmPassword-status');
            this.setupPasswordValidation();
        });
    }

    initializeResetPasswordPage() {
        document.addEventListener('DOMContentLoaded', () => {
            this.initializePasswordField('password', 'password-status');
            this.initializePasswordField('confirmPassword', 'confirmPassword-status');
            this.setupPasswordValidation();
        });
    }

    setupEventListeners() {
        // Common event listeners for all pages
    }

    // 한글 입력 감지 함수
    containsKorean(text) {
        return /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/.test(text);
    }

    // 허용된 문자만 남기기
    cleanInput(text) {
        return text.replace(/[^a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/g, '');
    }

    // 경고 메시지 표시
    showWarning(message) {
        if (!window.inputWarningShown) {
            if (this.pageType === 'login' && window.Signly && window.Signly.Alert) {
                window.Signly.Alert.warning(message);
            } else if (window.showAlertModal) {
                window.showAlertModal(message);
            }
            window.inputWarningShown = true;
            setTimeout(() => {
                window.inputWarningShown = false;
            }, 3000);
        }
    }

    // 입력 상태 업데이트
    updateInputStatus(input, statusId = 'password-status') {
        const status = document.getElementById(statusId);
        if (!status) return;

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
    toggleKeyboardIndicator(indicatorId = 'keyboard-indicator', show) {
        const indicator = document.getElementById(indicatorId);
        if (indicator) {
            indicator.style.display = show ? 'block' : 'none';
        }
    }

    // IME 제어 설정
    setupIMEControl(input) {
        const userAgent = navigator.userAgent.toLowerCase();

        if (userAgent.includes('chrome') || userAgent.includes('edge')) {
            input.style.imeMode = 'disabled';
        } else if (userAgent.includes('firefox')) {
            input.addEventListener('compositionstart', (e) => this.blockIME(e));
        } else if (userAgent.includes('safari')) {
            input.setAttribute('inputmode', 'latin');
        }
    }

    // IME 입력 차단
    blockIME(e) {
        e.preventDefault();
        const input = e.target;
        input.blur();
        input.focus();
        this.showWarning('IME 입력이 차단되었습니다. 영문만 입력 가능합니다.');
    }

    // 모바일 지원 설정
    setupMobileSupport(input) {
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
    initializePasswordField(inputId, statusId = 'password-status') {
        const passwordInput = document.getElementById(inputId);
        if (!passwordInput) return;

        // IME 제어 설정
        this.setupIMEControl(passwordInput);

        // 모바일 지원 설정
        this.setupMobileSupport(passwordInput);

        // keydown 이벤트 - 키 레벨 필터링
        passwordInput.addEventListener('keydown', (e) => {
            // Ctrl 조합 키 허용
            if (e.ctrlKey && this.ALLOWED_CTRL_KEYS.includes(e.keyCode)) {
                return true;
            }

            // 허용되지 않는 키 차단
            if (!this.ALLOWED_KEYS.includes(e.keyCode)) {
                e.preventDefault();
                e.stopPropagation();
                return false;
            }
        });

        // keypress 이벤트 - 문자 레벨 필터링
        passwordInput.addEventListener('keypress', (e) => {
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
        passwordInput.addEventListener('input', (e) => {
            const originalValue = e.target.value;
            const cleanValue = this.cleanInput(originalValue);

            if (originalValue !== cleanValue) {
                e.target.value = cleanValue;

                // 커서 위치 유지
                const cursorPos = e.target.selectionStart;
                e.target.setSelectionRange(cursorPos, cursorPos);

                this.showWarning('허용되지 않는 문자가 제거되었습니다.');
            }

            this.updateInputStatus(e.target, statusId);
        });

        // paste 이벤트 - 붙여넣기 제어
        passwordInput.addEventListener('paste', (e) => {
            e.preventDefault();

            const clipboardData = e.clipboardData || window.clipboardData;
            const pastedText = clipboardData.getData('text');
            const cleanText = this.cleanInput(pastedText);

            const start = e.target.selectionStart;
            const end = e.target.selectionEnd;
            const currentValue = e.target.value;

            e.target.value = currentValue.substring(0, start) + cleanText + currentValue.substring(end);
            e.target.setSelectionRange(start + cleanText.length, start + cleanText.length);

            if (pastedText !== cleanText) {
                this.showWarning('붙여넣기에서 허용되지 않는 문자가 제거되었습니다.');
            }

            this.updateInputStatus(e.target, statusId);
        });

        // 포커스 이벤트
        passwordInput.addEventListener('focus', () => {
            e.target.style.imeMode = 'disabled';
            this.toggleKeyboardIndicator(statusId.replace('-status', '-indicator'), true);
        });

        passwordInput.addEventListener('blur', () => {
            this.toggleKeyboardIndicator(statusId.replace('-status', '-indicator'), false);
        });

        // compositionstart 이벤트 - IME 시작 차단
        passwordInput.addEventListener('compositionstart', (e) => this.blockIME(e));
    }

    // Enter 키로 로그인 처리 (login 페이지 전용)
    setupEnterKeyHandler() {
        document.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && this.pageType === 'login') {
                const passwordInput = document.getElementById('password');
                if (!passwordInput) return;

                const cleanValue = this.cleanInput(passwordInput.value);

                if (passwordInput.value !== cleanValue) {
                    e.preventDefault();
                    this.showWarning('비밀번호에 허용되지 않는 문자가 포함되어 있습니다.');
                    return;
                }

                const form = document.querySelector('.auth-form');
                if (form) {
                    form.submit();
                }
            }
        });
    }

    // 비밀번호 유효성 검사 설정 (register, reset-password 페이지 전용)
    setupPasswordValidation() {
        const passwordInput = document.getElementById('password');
        const confirmPasswordInput = document.getElementById('confirmPassword');

        if (!passwordInput || !confirmPasswordInput) return;

        const validatePasswords = () => {
            const password = passwordInput.value;
            const confirmPassword = confirmPasswordInput.value;
            const confirmStatus = document.getElementById('confirmPassword-status');

            if (confirmPassword.length === 0) {
                confirmStatus.textContent = '';
                confirmStatus.className = 'form-text';
                return;
            }

            if (password === confirmPassword) {
                confirmStatus.textContent = '✓ 비밀번호가 일치합니다';
                confirmStatus.className = 'form-text text-success';
            } else {
                confirmStatus.textContent = '⚠ 비밀번호가 일치하지 않습니다';
                confirmStatus.className = 'form-text text-danger';
            }
        };

        passwordInput.addEventListener('input', validatePasswords);
        confirmPasswordInput.addEventListener('input', validatePasswords);
    }
}

// Initialize the auth pages when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.authPages = new AuthPages();
});