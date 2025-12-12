package com.signly.template.domain.model;

/**
 * 템플릿 변수 카테고리
 * 변수들을 논리적으로 그룹화하기 위한 카테고리 정의
 */
public enum VariableCategory {
    /**
     * 근로자 정보
     */
    EMPLOYEE_INFO("근로자 정보"),

    /**
     * 사업주 정보
     */
    EMPLOYER_INFO("사업주 정보"),

    /**
     * 계약 정보
     */
    CONTRACT_INFO("계약 정보"),

    /**
     * 근무 조건
     */
    WORK_CONDITION("근무 조건"),

    /**
     * 임금 정보
     */
    SALARY_INFO("임금 정보"),

    /**
     * 서명 관련
     */
    SIGNATURE("서명");

    private final String displayName;

    VariableCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}