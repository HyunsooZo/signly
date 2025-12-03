/**
 * 로컬 스토리지 관리 유틸리티
 * 사용자 정보, 서명 데이터 등을 안전하게 저장/조회
 */

window.DeallyStorage = (function () {
    'use strict';

    const STORAGE_KEYS = {
        USER_INFO: 'signly_user_info',
        OWNER_SIGNATURE: 'signly_owner_signature',
        FORM_DATA: 'signly_form_data',
        PREFERENCES: 'signly_preferences'
    };

    /**
     * 사용자 정보 읽기
     * @returns {Object|null} 사용자 정보 객체
     */
    function readOwnerInfo() {
        try {
            const raw = localStorage.getItem(STORAGE_KEYS.USER_INFO);
            if (!raw) {
                return null;
            }
            const parsed = JSON.parse(raw);
            if (!parsed || typeof parsed !== 'object') {
                return null;
            }
            return {
                name: parsed.name || '',
                email: parsed.email || '',
                userId: parsed.userId || '',
                companyName: parsed.companyName || '',
                businessPhone: parsed.businessPhone || '',
                businessAddress: parsed.businessAddress || ''
            };
        } catch (error) {
            console.warn('[WARN] 사용자 정보를 불러올 수 없습니다:', error);
            return null;
        }
    }

    /**
     * 사용자 정보 저장
     * @param {Object} userInfo - 저장할 사용자 정보
     */
    function saveOwnerInfo(userInfo) {
        try {
            if (userInfo && typeof userInfo === 'object') {
                localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(userInfo));
            }
        } catch (error) {
            console.warn('[WARN] 사용자 정보 저장 실패:', error);
        }
    }

    /**
     * 사용자 서명 정보 읽기
     * @returns {Object|null} 서명 정보 객체
     */
    function readOwnerSignature() {
        try {
            const raw = localStorage.getItem(STORAGE_KEYS.OWNER_SIGNATURE);
            if (!raw) {
                return null;
            }
            const parsed = JSON.parse(raw);
            if (!parsed || typeof parsed !== 'object') {
                return null;
            }
            return {
                dataUrl: parsed.dataUrl || '',
                updatedAt: parsed.updatedAt || ''
            };
        } catch (error) {
            console.warn('[WARN] 서명 정보를 불러올 수 없습니다:', error);
            return null;
        }
    }

    /**
     * 사용자 서명 정보 저장
     * @param {Object} signatureInfo - 저장할 서명 정보
     */
    function saveOwnerSignature(signatureInfo) {
        try {
            if (signatureInfo && typeof signatureInfo === 'object') {
                localStorage.setItem(STORAGE_KEYS.OWNER_SIGNATURE, JSON.stringify(signatureInfo));
            }
        } catch (error) {
            console.warn('[WARN] 서명 정보 저장 실패:', error);
        }
    }

    /**
     * 폼 데이터 저장 (임시 저장용)
     * @param {string} formId - 폼 ID
     * @param {Object} data - 저장할 데이터
     */
    function saveFormData(formId, data) {
        try {
            const key = `${STORAGE_KEYS.FORM_DATA}_${formId}`;
            localStorage.setItem(key, JSON.stringify({
                data: data,
                timestamp: Date.now()
            }));
        } catch (error) {
            console.warn('[WARN] 폼 데이터 저장 실패:', error);
        }
    }

    /**
     * 폼 데이터 읽기
     * @param {string} formId - 폼 ID
     * @param {number} maxAge - 최대 보관 시간 (ms)
     * @returns {Object|null} 저장된 데이터
     */
    function readFormData(formId, maxAge = 24 * 60 * 60 * 1000) { // 기본 24시간
        try {
            const key = `${STORAGE_KEYS.FORM_DATA}_${formId}`;
            const raw = localStorage.getItem(key);
            if (!raw) {
                return null;
            }
            const parsed = JSON.parse(raw);
            if (!parsed || typeof parsed !== 'object') {
                return null;
            }

            // 만료 시간 체크
            if (maxAge && parsed.timestamp && (Date.now() - parsed.timestamp > maxAge)) {
                localStorage.removeItem(key);
                return null;
            }

            return parsed.data;
        } catch (error) {
            console.warn('[WARN] 폼 데이터 읽기 실패:', error);
            return null;
        }
    }

    /**
     * 폼 데이터 삭제
     * @param {string} formId - 폼 ID
     */
    function removeFormData(formId) {
        try {
            const key = `${STORAGE_KEYS.FORM_DATA}_${formId}`;
            localStorage.removeItem(key);
        } catch (error) {
            console.warn('[WARN] 폼 데이터 삭제 실패:', error);
        }
    }

    /**
     * 사용자 설정 저장
     * @param {Object} preferences - 설정 객체
     */
    function savePreferences(preferences) {
        try {
            if (preferences && typeof preferences === 'object') {
                localStorage.setItem(STORAGE_KEYS.PREFERENCES, JSON.stringify(preferences));
            }
        } catch (error) {
            console.warn('[WARN] 설정 저장 실패:', error);
        }
    }

    /**
     * 사용자 설정 읽기
     * @returns {Object|null} 설정 객체
     */
    function readPreferences() {
        try {
            const raw = localStorage.getItem(STORAGE_KEYS.PREFERENCES);
            return raw ? JSON.parse(raw) : null;
        } catch (error) {
            console.warn('[WARN] 설정 읽기 실패:', error);
            return null;
        }
    }

    /**
     * 스토리지 정리 (오래된 데이터 제거)
     */
    function cleanup() {
        try {
            const keys = Object.keys(localStorage);
            const now = Date.now();
            const maxAge = 7 * 24 * 60 * 60 * 1000; // 7일

            keys.forEach(key => {
                if (key.startsWith(STORAGE_KEYS.FORM_DATA)) {
                    try {
                        const raw = localStorage.getItem(key);
                        if (raw) {
                            const parsed = JSON.parse(raw);
                            if (parsed.timestamp && (now - parsed.timestamp > maxAge)) {
                                localStorage.removeItem(key);
                            }
                        }
                    } catch (error) {
                        // 손상된 데이터는 삭제
                        localStorage.removeItem(key);
                    }
                }
            });
        } catch (error) {
            console.warn('[WARN] 스토리지 정리 실패:', error);
        }
    }

    /**
     * 스토리지 용량 체크
     * @returns {Object} 용량 정보
     */
    function getStorageInfo() {
        try {
            let totalSize = 0;
            const keys = Object.keys(localStorage);

            keys.forEach(key => {
                const value = localStorage.getItem(key);
                if (value) {
                    totalSize += key.length + value.length;
                }
            });

            return {
                totalKeys: keys.length,
                totalSize: totalSize,
                totalSizeKB: Math.round(totalSize / 1024 * 100) / 100,
                quota: 5 * 1024 * 1024, // 대부분 브라우저는 5MB
                usagePercent: Math.round((totalSize / (5 * 1024 * 1024)) * 100)
            };
        } catch (error) {
            console.warn('[WARN] 스토리지 정보 조회 실패:', error);
            return null;
        }
    }

    // 공개 API
    return {
        readOwnerInfo,
        saveOwnerInfo,
        readOwnerSignature,
        saveOwnerSignature,
        saveFormData,
        readFormData,
        removeFormData,
        savePreferences,
        readPreferences,
        cleanup,
        getStorageInfo,
        STORAGE_KEYS
    };
})();