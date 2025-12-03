package com.signly.notification.domain.model;

public enum EmailOutboxStatus {
    PENDING,    // 발송 대기
    SENT,       // 발송 완료
    FAILED      // 발송 실패 (최대 재시도 초과)
}
