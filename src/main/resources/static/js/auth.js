/**
 * JWT 인증 관리 - Signly
 */

const AuthManager = {
    /**
     * 액세스 토큰을 쿠키에서 가져오기
     */
    getAccessToken() {
        return Signly.Cookie.get('authToken');
    },

    /**
     * 리프레시 토큰을 쿠키에서 가져오기
     */
    getRefreshToken() {
        return Signly.Cookie.get('refreshToken');
    },

    /**
     * 토큰 저장
     */
    setTokens(accessToken, refreshToken, expiresIn) {
        // 액세스 토큰 (1시간)
        const accessTokenExpiry = expiresIn ? expiresIn / 1000 / 60 / 60 / 24 : 1/24; // 밀리초를 일로 변환
        Signly.Cookie.set('authToken', accessToken, accessTokenExpiry);

        // 리프레시 토큰 (자동 로그인 활성화 시에만 저장, 30일)
        if (refreshToken) {
            Signly.Cookie.set('refreshToken', refreshToken, 30);
        }
    },

    /**
     * 토큰 삭제
     */
    clearTokens() {
        Signly.Cookie.delete('authToken');
        Signly.Cookie.delete('refreshToken');
    },

    /**
     * 액세스 토큰 갱신
     */
    async refreshAccessToken() {
        const refreshToken = this.getRefreshToken();

        // 자동 로그인이 비활성화된 경우 (리프레시 토큰 없음)
        if (!refreshToken) {
            console.log('리프레시 토큰이 없습니다. 자동 로그인이 비활성화되어 있습니다.');
            throw new Error('자동 로그인이 비활성화되어 있습니다');
        }

        try {
            const response = await fetch('/api/auth/refresh', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ refreshToken })
            });

            if (!response.ok) {
                throw new Error('토큰 갱신 실패');
            }

            const data = await response.json();

            // 새로운 액세스 토큰과 리프레시 토큰 모두 저장 (무제한 자동 로그인)
            this.setTokens(data.accessToken, data.refreshToken, data.expiresIn);

            console.log('토큰 갱신 성공 (자동 로그인 연장)');
            return data.accessToken;
        } catch (error) {
            console.error('토큰 갱신 중 오류:', error);
            this.clearTokens();
            throw error;
        }
    },

    /**
     * 인증된 요청 보내기 (자동 토큰 갱신 포함)
     */
    async authenticatedFetch(url, options = {}) {
        let token = this.getAccessToken();

        // 첫 번째 시도
        const makeRequest = async (authToken) => {
            const headers = {
                ...options.headers,
                'Authorization': `Bearer ${authToken}`
            };

            return fetch(url, {
                ...options,
                headers
            });
        };

        try {
            let response = await makeRequest(token);

            // 401 응답이면 토큰 갱신 후 재시도
            if (response.status === 401) {
                console.log('액세스 토큰 만료, 갱신 시도...');

                try {
                    token = await this.refreshAccessToken();
                    console.log('토큰 갱신 성공, 요청 재시도...');
                    response = await makeRequest(token);
                } catch (refreshError) {
                    console.error('토큰 갱신 실패, 로그인 페이지로 이동');
                    this.redirectToLogin();
                    throw refreshError;
                }
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
     * 로그인 상태 확인
     */
    isAuthenticated() {
        return !!this.getAccessToken();
    },

    /**
     * 자동 로그인 체크 (페이지 로드 시)
     */
    async checkAuthStatus() {
        if (!this.isAuthenticated()) {
            return false;
        }

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
document.addEventListener('DOMContentLoaded', function() {
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
