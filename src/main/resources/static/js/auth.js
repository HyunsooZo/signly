/**
 * JWT 인증 관리 - Signly
 */

const AuthManager = {
    /**
     * 토큰 삭제 (서버에서 HttpOnly 쿠키로 처리하므로 클라이언트에서 직접 삭제 불필요)
     */
    clearTokens() {
        // HttpOnly 쿠키는 서버에서만 삭제 가능
        // 로그아웃 API 호출로 처리
    },

    /**
     * 인증된 요청 보내기 (서버 필터에서 자동 토큰 갱신 처리)
     */
    async authenticatedFetch(url, options = {}) {
        const headers = {
            ...options.headers,
            'Content-Type': 'application/json'
        };

        try {
            const response = await fetch(url, {
                ...options,
                credentials: 'include', // HttpOnly 쿠키 자동 전송
                headers
            });

            // 401 응답 시 로그인 페이지로 이동 (서버에서 자동 갱신 실패한 경우)
            if (response.status === 401) {
                let errorMessage = '인증이 필요합니다';
                try {
                    const errorData = await response.json();
                    if (errorData.error === 'TOKEN_REFRESH_FAILED') {
                        errorMessage = '세션이 만료되었습니다. 다시 로그인해주세요.';
                    } else if (errorData.error === 'NO_REFRESH_TOKEN') {
                        errorMessage = '자동 로그인이 만료되었습니다. 다시 로그인해주세요.';
                    }
                } catch (e) {
                    // JSON 파싱 실패 시 기본 메시지 사용
                }

                console.log('인증 실패:', errorMessage);
                this.redirectToLogin();
                throw new Error(errorMessage);
            }

            return response;
        } catch (error) {
            console.error('API 요청 실패:', error);
            throw error;
        }
    },

    /**
     * 로그인 페이지로 리다이렉트
     */
    redirectToLogin() {
        const currentPath = window.location.pathname;
        const returnUrl = encodeURIComponent(currentPath);
        window.location.href = `/login?returnUrl=${returnUrl}`;
    },

    /**
     * 로그인 상태 확인 (서버 필터에서 처리하므로 단순화)
     */
    async checkAuthStatus() {
        try {
            // 간단한 인증 체크 API 호출
            const response = await this.authenticatedFetch('/api/users/profile');
            return response.ok;
        } catch (error) {
            console.error('인증 상태 확인 실패:', error);
            return false;
        }
    }
};

// 전역으로 노출
window.AuthManager = AuthManager;

/**
 * 로그인 실패 횟수 관리
 */
const LoginAttemptTracker = {
    /**
     * 로그인 실패 횟수 저장
     */
    saveFailureCount(count) {
        sessionStorage.setItem('loginFailureCount', count.toString());
    },

    /**
     * 로그인 실패 횟수 가져오기
     */
    getFailureCount() {
        const count = sessionStorage.getItem('loginFailureCount');
        return count ? parseInt(count) : 0;
    },

    /**
     * 로그인 실패 횟수 초기화
     */
    resetFailureCount() {
        sessionStorage.removeItem('loginFailureCount');
    },

    /**
     * 남은 로그인 시도 횟수 계산
     */
    getRemainingAttempts() {
        return Math.max(0, 5 - this.getFailureCount());
    }
};

// 전역으로 노출
window.LoginAttemptTracker = LoginAttemptTracker;

/**
 * 로그인 폼 처리
 */
function setupLoginForm() {
    const loginForm = document.querySelector('form[action="/login"]');
    if (!loginForm) return;

    loginForm.addEventListener('submit', function(e) {
        const emailInput = document.querySelector('#email');
        const email = emailInput ? emailInput.value : '';
        
        // 이메일이 변경되면 실패 횟수 초기화
        const lastEmail = sessionStorage.getItem('lastLoginEmail');
        if (lastEmail && lastEmail !== email) {
            LoginAttemptTracker.resetFailureCount();
        }
        sessionStorage.setItem('lastLoginEmail', email);
    });

    // 남은 시도 횟수 표시
    const remainingAttempts = LoginAttemptTracker.getRemainingAttempts();
    if (remainingAttempts < 5) {
        updateRemainingAttemptsDisplay(remainingAttempts);
    }
}

/**
 * 남은 로그인 시도 횟수 표시 업데이트
 */
function updateRemainingAttemptsDisplay(remaining) {
    const existingElement = document.querySelector('.remaining-attempts');
    if (existingElement) {
        existingElement.remove();
    }

    if (remaining > 0 && remaining < 5) {
        const alertElement = document.querySelector('.alert-danger');
        if (alertElement) {
            const remainingDiv = document.createElement('div');
            remainingDiv.className = 'remaining-attempts';
            remainingDiv.innerHTML = `
                <small class="d-block mt-2 text-muted">
                    남은 로그인 시도 횟수: <strong>${remaining}</strong> 회
                </small>
            `;
            alertElement.appendChild(remainingDiv);
        }
    }
}

/**
 * 로그인 실패 처리
 */
function handleLoginFailure(errorMessage) {
    // 계정 잠금 메시지 확인
    if (errorMessage.includes('잠겨있습니다') || errorMessage.includes('계정이 잠겨있습니다')) {
        // 잠금 상태면 실패 횟수 초기화
        LoginAttemptTracker.resetFailureCount();
        return;
    }

    // 일반 로그인 실패 처리
    let currentCount = LoginAttemptTracker.getFailureCount();
    currentCount++;
    LoginAttemptTracker.saveFailureCount(currentCount);

    const remainingAttempts = LoginAttemptTracker.getRemainingAttempts();
    
    if (remainingAttempts > 0) {
        console.warn(`로그인 실패: ${currentCount}회 실패, 남은 시도: ${remainingAttempts}회`);
        updateRemainingAttemptsDisplay(remainingAttempts);
    }

    // 5회 실패 시 경고
    if (remainingAttempts === 0) {
        console.error('5회 로그인 실패 - 계정 잠금 예정');
    }
}

/**
 * 페이지 로드 시 자동 인증 체크 및 로그인 폼 설정
 */
document.addEventListener('DOMContentLoaded', function () {
    // 로그인 폼 설정
    setupLoginForm();

    // 로그인 페이지가 아닌 경우에만 체크
    const isLoginPage = window.location.pathname === '/login';
    const isPublicPage = window.location.pathname.startsWith('/sign/');

    if (!isLoginPage && !isPublicPage) {
        // 백그라운드에서 토큰 검증 (실패해도 사용자에게 보이지 않음)
        AuthManager.checkAuthStatus().catch(() => {
            // 인증 실패 시 조용히 처리
            console.log('인증 상태 확인 실패');
        });
    }

    // 로그인 페이지인 경우 실패 횟수 표시
    if (isLoginPage) {
        const errorMessageElement = document.querySelector('.alert-danger');
        if (errorMessageElement) {
            const errorText = errorMessageElement.textContent || '';
            handleLoginFailure(errorText);
        }
    }
});
