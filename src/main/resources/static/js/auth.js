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
 * 페이지 로드 시 자동 인증 체크
 */
document.addEventListener('DOMContentLoaded', function () {
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
});
