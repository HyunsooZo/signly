package com.signly.common.audit.domain.model;

/**
 * 감사 로그 대상 엔티티 타입
 */
public enum EntityType {
    CONTRACT,
    TEMPLATE,
    USER,
    FIRST_PARTY_SIGNATURE,
    CONTRACT_SIGNATURE
}