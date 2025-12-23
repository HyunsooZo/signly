package com.signly.common.audit.domain.model;

/**
 * 감사 로그 작업 타입
 */
public enum AuditAction {
    // Contract
    CONTRACT_CREATED("계약서 생성"),
    CONTRACT_UPDATED("계약서 수정"),
    CONTRACT_DELETED("계약서 삭제"),
    CONTRACT_SENT("서명 요청 발송"),
    CONTRACT_SIGNED("서명 완료"),
    CONTRACT_COMPLETED("계약 완료"),
    CONTRACT_CANCELLED("계약 취소"),
    CONTRACT_EXPIRED("계약 만료"),
    CONTRACT_PDF_GENERATED("PDF 생성"),

    // Template
    TEMPLATE_CREATED("템플릿 생성"),
    TEMPLATE_UPDATED("템플릿 수정"),
    TEMPLATE_ACTIVATED("템플릿 활성화"),
    TEMPLATE_ARCHIVED("템플릿 보관"),
    TEMPLATE_DELETED("템플릿 삭제"),

    // User
    USER_REGISTERED("회원가입"),
    USER_EMAIL_VERIFIED("이메일 인증"),
    USER_PASSWORD_CHANGED("비밀번호 변경"),
    USER_PASSWORD_RESET("비밀번호 재설정"),
    USER_PROFILE_UPDATED("프로필 수정"),
    USER_STATUS_CHANGED("상태 변경"),

    // Login & Security
    LOGIN_SUCCESS("로그인 성공"),
    LOGIN_FAILED("로그인 실패"),
    ACCOUNT_LOCKED("계정 잠금"),
    ACCOUNT_UNLOCKED("계정 해제"),
    PASSWORD_REUSE_ATTEMPT("비밀번호 재사용 시도"),

    // Signature
    FIRST_PARTY_SIGNATURE_UPLOADED("갑 서명 업로드"),
    FIRST_PARTY_SIGNATURE_UPDATED("갑 서명 수정");

    private final String description;

    AuditAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}