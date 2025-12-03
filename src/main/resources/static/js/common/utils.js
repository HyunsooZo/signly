/**
 * 공통 유틸리티 함수 모음
 * 전역적으로 사용되는 헬퍼 함수들
 */

window.SignlyUtils = (function () {
    'use strict';

    /**
     * 날짜를 로컬 날짜시간 값으로 변환
     * @param {Date} date - 변환할 날짜
     * @returns {string} YYYY-MM-DDTHH:mm 형식의 문자열
     */
    function toLocalDateTimeValue(date) {
        const pad = (value) => String(value).padStart(2, '0');
        return [
            date.getFullYear(),
            '-', pad(date.getMonth() + 1),
            '-', pad(date.getDate()),
            'T', pad(date.getHours()),
            ':', pad(date.getMinutes())
        ].join('');
    }

    /**
     * HTML 엔티티를 디코딩
     * @param {string} value - 디코딩할 값
     * @returns {string} 디코딩된 문자열
     */
    function decodeHtmlEntities(value) {
        const textarea = document.createElement('textarea');
        textarea.innerHTML = value;
        return textarea.value;
    }

    /**
     * 값이 문자열인지 확인하고 보장
     * @param {*} value - 확인할 값
     * @returns {string} 문자열 값
     */
    function ensureString(value) {
        return value != null ? String(value) : '';
    }

    /**
     * 플레이스홀더 정규식
     */
    const PLACEHOLDER_REGEX = /\{([^{}]+)\}|\[([^\[\]]+)\]/g;

    /**
     * 텍스트의 모든 플레이스홀더를 순회하며 콜백 실행
     * @param {string} text - 검색할 텍스트
     * @param {Function} callback - 콜백 함수 (name, match, index)
     */
    function forEachPlaceholder(text, callback) {
        if (!text) {
            return;
        }
        PLACEHOLDER_REGEX.lastIndex = 0;
        let match;
        while ((match = PLACEHOLDER_REGEX.exec(text)) !== null) {
            const name = (match[1] ? match[1].trim() : (match[2] ? match[2].trim() : ''));
            if (!name) {
                continue;
            }
            callback(name, match[0], match.index);
        }
    }

    /**
     * 안전한 JSON 파싱
     * @param {string} jsonString - 파싱할 JSON 문자열
     * @param {*} defaultValue - 파싱 실패 시 반환할 기본값
     * @returns {*} 파싱된 객체 또는 기본값
     */
    function safeJsonParse(jsonString, defaultValue = null) {
        try {
            return JSON.parse(jsonString);
        } catch (error) {
            console.warn('[WARN] JSON 파싱 실패:', error);
            return defaultValue;
        }
    }

    /**
     * 요소가 존재하는지 확인
     * @param {string} selector - CSS 선택자
     * @returns {boolean} 존재 여부
     */
    function elementExists(selector) {
        return document.querySelector(selector) !== null;
    }

    /**
     * 요소에 이벤트 리스너 추가 (안전하게)
     * @param {Element} element - 대상 요소
     * @param {string} event - 이벤트 이름
     * @param {Function} handler - 이벤트 핸들러
     * @param {Object} options - 이벤트 옵션
     */
    function addEventListener(element, event, handler, options = {}) {
        if (element && typeof handler === 'function') {
            element.addEventListener(event, handler, options);
        }
    }

    /**
     * 디바운스 함수
     * @param {Function} func - 디바운스할 함수
     * @param {number} wait - 대기 시간 (ms)
     * @returns {Function} 디바운스된 함수
     */
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    /**
     * 스로틀 함수
     * @param {Function} func - 스로틀할 함수
     * @param {number} limit - 제한 시간 (ms)
     * @returns {Function} 스로틀된 함수
     */
    function throttle(func, limit) {
        let inThrottle;
        return function executedFunction(...args) {
            if (!inThrottle) {
                func(...args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }

    /**
     * 쿠키 값 가져오기
     * @param {string} name - 쿠키 이름
     * @returns {string|null} 쿠키 값
     */
    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) {
            return parts.pop().split(';').shift();
        }
        return null;
    }

    /**
     * 쿠키 설정
     * @param {string} name - 쿠키 이름
     * @param {string} value - 쿠키 값
     * @param {number} days - 만료일 (일)
     */
    function setCookie(name, value, days) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        const expires = `expires=${date.toUTCString()}`;
        document.cookie = `${name}=${value};${expires};path=/`;
    }

    /**
     * 쿠키 삭제
     * @param {string} name - 쿠키 이름
     */
    function deleteCookie(name) {
        document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:01 GMT;path=/`;
    }

    // 공개 API
    return {
        toLocalDateTimeValue,
        decodeHtmlEntities,
        ensureString,
        forEachPlaceholder,
        safeJsonParse,
        elementExists,
        addEventListener,
        debounce,
        throttle,
        getCookie,
        setCookie,
        deleteCookie,
        PLACEHOLDER_REGEX
    };
})();