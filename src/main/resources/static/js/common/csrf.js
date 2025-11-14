/**
 * CSRF Token Manager for AJAX requests
 * Handles CSRF token retrieval and inclusion in fetch/XHR requests
 */
class CsrfManager {
    constructor() {
        this.token = null;
        this.headerName = 'X-CSRF-TOKEN';
        this.init();
    }

    init() {
        // Get token from meta tag or cookie
        this.token = this.getTokenFromMeta() || this.getTokenFromCookie();

        // Auto-refresh token from response headers
        this.setupTokenRefresh();
    }

    getTokenFromMeta() {
        const metaTag = document.querySelector('meta[name="_csrf"]');
        return metaTag ? metaTag.getAttribute('content') : null;
    }

    getTokenFromCookie() {
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (name === 'XSRF-TOKEN') {
                return decodeURIComponent(value);
            }
        }
        return null;
    }

    setupTokenRefresh() {
        // Listen for response headers to update token
        const originalFetch = window.fetch;
        window.fetch = function (...args) {
            return originalFetch.apply(this, args).then(response => {
                const newToken = response.headers.get('X-CSRF-TOKEN');
                if (newToken) {
                    window.csrfManager.token = newToken;
                }
                return response;
            });
        };
    }

    getHeaders() {
        const headers = {};
        if (this.token) {
            headers[this.headerName] = this.token;
        }
        return headers;
    }

    fetchWithCSRF(url, options = {}) {
        const headers = {
            ...options.headers,
            ...this.getHeaders()
        };

        return fetch(url, {
            ...options,
            headers
        });
    }
}

// Initialize CSRF manager
window.csrfManager = new CsrfManager();

// Helper function for common usage
window.fetchWithCSRF = (url, options) => window.csrfManager.fetchWithCSRF(url, options);