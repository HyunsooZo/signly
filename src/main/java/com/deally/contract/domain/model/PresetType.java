package com.deally.contract.domain.model;

/**
 * 계약서 프리셋 타입
 */
public enum PresetType {
    /**
     * 프리셋을 사용하지 않는 일반 계약서
     */
    NONE,

    /**
     * 표준 근로계약서
     */
    LABOR_STANDARD;

    /**
     * 문자열을 PresetType으로 변환
     */
    public static PresetType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return NONE;
        }
        try {
            return PresetType.valueOf(value.toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }

    /**
     * PresetType을 문자열로 변환 (JSP/프론트엔드용)
     */
    public String toDisplayString() {
        return name().toLowerCase().replace('_', '-');
    }
}
