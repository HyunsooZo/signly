package com.signly.user.domain.model;

public enum UserStatus {
    PENDING,    // 이메일 인증 대기
    ACTIVE,     // 활성 (이메일 인증 완료)
    INACTIVE,   // 비활성
    SUSPENDED,  // 정지
    LOCKED      // 잠금 (로그인 5회 실패)
}