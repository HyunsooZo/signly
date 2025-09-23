/**
 * 공통 자바스크립트 - Signly
 */

document.addEventListener('DOMContentLoaded', function() {
    // 드롭다운 메뉴 기능
    initDropdowns();

    // 모바일 메뉴 토글
    initMobileMenu();

    // 알림 메시지 자동 숨김
    initAlerts();

    // 폼 유효성 검사
    initFormValidation();

    // 로딩 스피너
    initLoadingSpinner();
});

/**
 * 드롭다운 메뉴 초기화
 */
function initDropdowns() {
    const dropdownToggles = document.querySelectorAll('.dropdown-toggle');

    dropdownToggles.forEach(toggle => {
        toggle.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const dropdown = this.nextElementSibling;
            const isOpen = dropdown.classList.contains('show');

            // 다른 드롭다운 닫기
            closeAllDropdowns();

            // 현재 드롭다운 토글
            if (!isOpen) {
                dropdown.classList.add('show');
            }
        });
    });

    // 문서 클릭시 드롭다운 닫기
    document.addEventListener('click', closeAllDropdowns);
}

/**
 * 모든 드롭다운 닫기
 */
function closeAllDropdowns() {
    const openDropdowns = document.querySelectorAll('.dropdown-menu.show');
    openDropdowns.forEach(dropdown => {
        dropdown.classList.remove('show');
    });
}

/**
 * 모바일 메뉴 초기화
 */
function initMobileMenu() {
    const mobileToggle = document.getElementById('mobileMenuToggle');
    const headerNav = document.querySelector('.header-nav');

    if (mobileToggle && headerNav) {
        mobileToggle.addEventListener('click', function() {
            headerNav.classList.toggle('mobile-open');
            this.classList.toggle('active');
        });
    }
}

/**
 * 알림 메시지 자동 숨김
 */
function initAlerts() {
    const alerts = document.querySelectorAll('.alert');

    alerts.forEach(alert => {
        // 5초 후 자동 숨김
        setTimeout(() => {
            fadeOut(alert);
        }, 5000);

        // 닫기 버튼이 있다면 클릭 이벤트 추가
        const closeBtn = alert.querySelector('.alert-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => {
                fadeOut(alert);
            });
        }
    });
}

/**
 * 요소 페이드 아웃
 */
function fadeOut(element) {
    element.style.opacity = '0';
    element.style.transition = 'opacity 0.3s ease';

    setTimeout(() => {
        element.style.display = 'none';
    }, 300);
}

/**
 * 폼 유효성 검사 초기화
 */
function initFormValidation() {
    const forms = document.querySelectorAll('form[data-validate="true"]');

    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!validateForm(this)) {
                e.preventDefault();
            }
        });

        // 실시간 유효성 검사
        const inputs = form.querySelectorAll('.form-control');
        inputs.forEach(input => {
            input.addEventListener('blur', () => validateField(input));
            input.addEventListener('input', () => clearFieldError(input));
        });
    });
}

/**
 * 폼 유효성 검사
 */
function validateForm(form) {
    let isValid = true;
    const requiredFields = form.querySelectorAll('[required]');

    requiredFields.forEach(field => {
        if (!validateField(field)) {
            isValid = false;
        }
    });

    return isValid;
}

/**
 * 필드 유효성 검사
 */
function validateField(field) {
    const value = field.value.trim();
    const fieldType = field.type;
    const fieldName = field.name;

    // 필수 필드 검사
    if (field.hasAttribute('required') && !value) {
        showFieldError(field, '이 필드는 필수입니다.');
        return false;
    }

    // 이메일 유효성 검사
    if (fieldType === 'email' && value) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) {
            showFieldError(field, '올바른 이메일 주소를 입력해주세요.');
            return false;
        }
    }

    // 비밀번호 유효성 검사
    if (fieldType === 'password' && value) {
        if (value.length < 8) {
            showFieldError(field, '비밀번호는 8자 이상이어야 합니다.');
            return false;
        }

        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/;
        if (!passwordRegex.test(value)) {
            showFieldError(field, '비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다.');
            return false;
        }
    }

    // 비밀번호 확인
    if (fieldName === 'passwordConfirm' && value) {
        const passwordField = field.form.querySelector('input[name="password"]');
        if (passwordField && value !== passwordField.value) {
            showFieldError(field, '비밀번호가 일치하지 않습니다.');
            return false;
        }
    }

    clearFieldError(field);
    return true;
}

/**
 * 필드 에러 표시
 */
function showFieldError(field, message) {
    field.classList.add('is-invalid');

    let errorElement = field.parentNode.querySelector('.invalid-feedback');
    if (!errorElement) {
        errorElement = document.createElement('div');
        errorElement.className = 'invalid-feedback';
        field.parentNode.appendChild(errorElement);
    }

    errorElement.textContent = message;
}

/**
 * 필드 에러 제거
 */
function clearFieldError(field) {
    field.classList.remove('is-invalid');

    const errorElement = field.parentNode.querySelector('.invalid-feedback');
    if (errorElement) {
        errorElement.remove();
    }
}

/**
 * 로딩 스피너 초기화
 */
function initLoadingSpinner() {
    // 폼 제출시 로딩 스피너 표시
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            const submitBtn = this.querySelector('button[type="submit"], input[type="submit"]');
            if (submitBtn) {
                showLoadingSpinner(submitBtn);
            }
        });
    });
}

/**
 * 로딩 스피너 표시
 */
function showLoadingSpinner(button) {
    const originalText = button.textContent;
    button.disabled = true;
    button.innerHTML = '<span class="spinner"></span> 처리중...';

    // 원래 텍스트를 data attribute에 저장
    button.dataset.originalText = originalText;
}

/**
 * 로딩 스피너 숨김
 */
function hideLoadingSpinner(button) {
    button.disabled = false;
    button.textContent = button.dataset.originalText || '확인';
}

/**
 * AJAX 요청 공통 함수
 */
function sendRequest(url, options = {}) {
    const defaultOptions = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'same-origin'
    };

    const mergedOptions = { ...defaultOptions, ...options };

    return fetch(url, mergedOptions)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .catch(error => {
            console.error('Request failed:', error);
            showAlert('요청 처리 중 오류가 발생했습니다.', 'danger');
            throw error;
        });
}

/**
 * 알림 메시지 표시
 */
function showAlert(message, type = 'info') {
    const alertContainer = document.querySelector('.alert-container') || createAlertContainer();

    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.innerHTML = `
        ${message}
        <button type="button" class="alert-close" aria-label="닫기">×</button>
    `;

    alertContainer.appendChild(alert);

    // 자동 숨김
    setTimeout(() => {
        fadeOut(alert);
    }, 5000);

    // 닫기 버튼 이벤트
    alert.querySelector('.alert-close').addEventListener('click', () => {
        fadeOut(alert);
    });
}

/**
 * 알림 컨테이너 생성
 */
function createAlertContainer() {
    const container = document.createElement('div');
    container.className = 'alert-container';
    container.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
        max-width: 400px;
    `;

    document.body.appendChild(container);
    return container;
}

/**
 * 쿠키 관련 함수들
 */
const Cookie = {
    set: function(name, value, days) {
        let expires = '';
        if (days) {
            const date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            expires = '; expires=' + date.toUTCString();
        }
        document.cookie = name + '=' + (value || '') + expires + '; path=/';
    },

    get: function(name) {
        const nameEQ = name + '=';
        const ca = document.cookie.split(';');
        for (let i = 0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) === ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    },

    delete: function(name) {
        document.cookie = name + '=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    }
};

/**
 * 유틸리티 함수들
 */
const Utils = {
    // 디바운스
    debounce: function(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    // 쓰로틀
    throttle: function(func, limit) {
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    },

    // 날짜 포맷팅
    formatDate: function(date, format = 'YYYY-MM-DD') {
        const d = new Date(date);
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');

        return format
            .replace('YYYY', year)
            .replace('MM', month)
            .replace('DD', day);
    },

    // 파일 크기 포맷팅
    formatFileSize: function(bytes) {
        if (bytes === 0) return '0 Bytes';

        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));

        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
};

// 전역 객체로 노출
window.Signly = {
    sendRequest,
    showAlert,
    showLoadingSpinner,
    hideLoadingSpinner,
    Cookie,
    Utils
};