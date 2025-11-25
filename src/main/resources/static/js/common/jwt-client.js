/**
 * JWT 전용 인증 클라이언트
 * CSRF 없이 JWT 토큰만으로 인증 처리
 */
class JwtClient {
    constructor() {
        this.accessToken = null;
        this.refreshToken = null;
        this.isRefreshing = false;
        this.refreshPromise = null;
        
        // 초기화 시 쿠키에서 토큰 로드
        this.loadTokensFromCookies();
    }

    /**
     * 쿠키에서 토큰 로드
     */
    loadTokensFromCookies() {
        this.accessToken = this.getCookie('authToken');
        this.refreshToken = this.getCookie('refreshToken');
    }

    /**
     * 쿠키 값 가져오기
     */
    getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) {
            return parts.pop().split(';').shift();
        }
        return null;
    }

    /**
     * 유효한 Access Token 가져오기 (자동 재발급 포함)
     */
    async getValidToken() {
        if (this.isTokenValid(this.accessToken)) {
            return this.accessToken;
        }

        // 토큰이 없거나 만료된 경우 재발급
        if (this.refreshToken && !this.isRefreshing) {
            return await this.refreshAccessToken();
        }

        return null;
    }

    /**
     * 토큰 유효성 검사 (단순화된 버전)
     */
    isTokenValid(token) {
        if (!token) return false;
        
        try {
            // JWT 페이로드 파싱
            const payload = JSON.parse(atob(token.split('.')[1]));
            const now = Date.now() / 1000;
            return payload.exp > now + 60; // 1분 여유
        } catch (e) {
            return false;
        }
    }

    /**
     * Access Token 재발급
     */
    async refreshAccessToken() {
        if (this.isRefreshing && this.refreshPromise) {
            return this.refreshPromise;
        }

        this.isRefreshing = true;
        this.refreshPromise = this.performTokenRefresh();

        try {
            const newToken = await this.refreshPromise;
            this.accessToken = newToken;
            return newToken;
        } finally {
            this.isRefreshing = false;
            this.refreshPromise = null;
        }
    }

    /**
     * 토큰 재발급 API 호출
     */
    async performTokenRefresh() {
        try {
            const response = await fetch('/api/auth/refresh', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    refreshToken: this.refreshToken
                }),
                credentials: 'include' // 쿠키 포함
            });

            if (!response.ok) {
                throw new Error('Token refresh failed');
            }

            const data = await response.json();
            return data.accessToken;
        } catch (error) {
            console.error('Token refresh failed:', error);
            // 재발급 실패 시 로그인 페이지로 리다이렉트
            window.location.href = '/login?error=token_expired';
            return null;
        }
    }

    /**
     * 요청 헤더에 JWT 인증 정보 주입
     */
    async injectAuthHeaders(headers = {}) {
        const token = await this.getValidToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        return headers;
    }

    /**
     * JWT 인증이 포함된 fetch 요청
     */
    async fetchWithAuth(url, options = {}) {
        const headers = await this.injectAuthHeaders(options.headers || {});
        
        const response = await fetch(url, {
            ...options,
            headers,
            credentials: 'include'
        });

        // 401 응답 시 토큰 재발급 후 재시도
        if (response.status === 401 && this.refreshToken && !this.isRefreshing) {
            await this.refreshAccessToken();
            
            // 재시도
            const retryHeaders = await this.injectAuthHeaders(options.headers || {});
            return fetch(url, {
                ...options,
                headers: retryHeaders,
                credentials: 'include'
            });
        }

        return response;
    }

    /**
     * 폼 제출 시 JWT 인증 처리
     */
    async submitFormWithAuth(formElement, options = {}) {
        const formData = new FormData(formElement);
        const headers = await this.injectAuthHeaders({
            // FormData를 사용할 때 Content-Type은 브라우저가 자동 설정
        });

        const response = await fetch(formElement.action, {
            method: formElement.method || 'POST',
            headers,
            body: formData,
            credentials: 'include'
        });

        if (response.status === 401 && this.refreshToken && !this.isRefreshing) {
            await this.refreshAccessToken();
            
            // 재시도
            const retryHeaders = await this.injectAuthHeaders({});
            return fetch(formElement.action, {
                method: formElement.method || 'POST',
                headers: retryHeaders,
                body: formData,
                credentials: 'include'
            });
        }

        return response;
    }

    /**
     * 로그아웃 처리
     */
    async logout() {
        try {
            await this.fetchWithAuth('/api/auth/logout', {
                method: 'POST'
            });
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            // 클라이언트 토큰 정리
            this.accessToken = null;
            this.refreshToken = null;
            
            // 로그인 페이지로 리다이렉트
            window.location.href = '/login';
        }
    }

    /**
     * 현재 인증 상태 확인
     */
    isAuthenticated() {
        return this.accessToken && this.isTokenValid(this.accessToken);
    }
}

// 전역 인스턴스 생성
window.jwtClient = new JwtClient();

// 기존 CSRF 관련 코드 제거를 위한 호환성 래퍼
window.csrfManager = {
    getToken: () => Promise.resolve(null),
    injectToken: () => {},
    refresh: () => Promise.resolve(null)
};

// 기존 fetchWithCSRF 함수를 fetchWithAuth로 대체
window.fetchWithCSRF = window.jwtClient.fetchWithAuth.bind(window.jwtClient);

// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 모든 폼에 자동 JWT 처리 추가
    const forms = document.querySelectorAll('form[data-jwt-auth="true"]');
    forms.forEach(form => {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            try {
                const response = await window.jwtClient.submitFormWithAuth(form);
                
                if (response.ok) {
                    // 성공 처리 (리다이렉트 등)
                    const redirectUrl = form.dataset.redirect;
                    if (redirectUrl) {
                        window.location.href = redirectUrl;
                    }
                } else {
                    // 에러 처리
                    console.error('Form submission failed:', response.status);
                }
            } catch (error) {
                console.error('Form submission error:', error);
            }
        });
    });
});