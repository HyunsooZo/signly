/**
 * CSRF Token Manager for AJAX requests
 * Handles CSRF token retrieval and inclusion in fetch/XHR requests
 */
class CsrfManager {
    constructor() {
        this.cookieName = 'XSRF-TOKEN';
        this.token = null;
        this.headerName = this.getMetaContent('_csrf_header') || 'X-CSRF-TOKEN';
        this.paramName = this.getMetaContent('_csrf_parameter') || '_csrf';
        this.init();
    }

    init() {
        this.token = this.getTokenFromMeta() || this.getTokenFromCookie();
        this.setupTokenRefresh();
        this.bindFormSubmitHandler();
    }

    getMetaContent(name) {
        const metaTag = document.querySelector(`meta[name="${name}"]`);
        return metaTag ? metaTag.getAttribute('content') : null;
    }

    getTokenFromMeta() {
        return this.getMetaContent('_csrf');
    }

    getParamName() {
        return this.paramName || '_csrf';
    }

    getTokenFromCookie() {
        if (typeof document === 'undefined' || !document.cookie) {
            return null;
        }
        const cookies = document.cookie.split(';');
        for (const cookie of cookies) {
            const trimmed = cookie.trim();
            if (!trimmed) {
                continue;
            }
            const [name, ...rest] = trimmed.split('=');
            if (name === this.cookieName) {
                return decodeURIComponent(rest.join('='));
            }
        }
        return null;
    }

    getToken() {
        return this.getTokenFromCookie() || this.token;
    }

    setupTokenRefresh() {
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

    bindFormSubmitHandler() {
        document.addEventListener('submit', (event) => {
            const target = event.target;
            if (target && target.tagName === 'FORM') {
                this.ensureFormToken(target);
            }
        }, true);
    }

    ensureFormToken(form) {
        if (!form) {
            return;
        }
        const token = this.getToken();
        const paramName = this.getParamName();
        if (!token || !paramName) {
            return;
        }
        let input = form.querySelector(`input[name="${paramName}"]`);
        if (!input) {
            input = document.createElement('input');
            input.type = 'hidden';
            input.name = paramName;
            form.appendChild(input);
        }
        input.value = token;
    }

    appendTokenToParams(params) {
        if (!params || typeof params.append !== 'function') {
            return;
        }
        const token = this.getToken();
        const paramName = this.getParamName();
        if (token && paramName) {
            params.append(paramName, token);
        }
    }

    getHeaders() {
        const headers = {};
        const token = this.getToken();
        if (token) {
            headers[this.headerName] = token;
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

window.csrfManager = new CsrfManager();
window.fetchWithCSRF = (url, options) => window.csrfManager.fetchWithCSRF(url, options);
window.ensureCsrfToken = (form) => window.csrfManager.ensureFormToken(form);
