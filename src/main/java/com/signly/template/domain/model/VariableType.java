package com.signly.template.domain.model;

/**
 * 템플릿 변수 타입
 * HTML5 input 타입과 매핑되는 변수 타입 정의
 */
public enum VariableType {
    /**
     * 일반 텍스트
     * HTML: <input type="text">
     */
    TEXT,

    /**
     * 시간 (HH:MM 형식)
     * HTML: <input type="time">
     */
    TIME,

    /**
     * 날짜 (YYYY-MM-DD 형식)
     * HTML: <input type="date">
     */
    DATE,

    /**
     * 날짜+시간
     * HTML: <input type="datetime-local">
     */
    DATETIME,

    /**
     * 이메일 주소
     * HTML: <input type="email">
     */
    EMAIL,

    /**
     * 전화번호
     * HTML: <input type="tel">
     */
    PHONE,

    /**
     * 숫자
     * HTML: <input type="number">
     */
    NUMBER,

    /**
     * 통화 (금액)
     * HTML: <input type="text">
     */
    CURRENCY,

    /**
     * 이미지 (파일 경로)
     * HTML: <input type="file">
     */
    IMAGE,

    /**
     * 긴 텍스트
     * HTML: <textarea>
     */
    TEXTAREA
}