package com.signly.notification.domain.model;

public enum EmailTemplate {
    CONTRACT_SIGNING_REQUEST("계약서 서명 요청", "contract-signing-request.html"),
    CONTRACT_COMPLETED("계약서 완료 알림", "contract-completed.html"),
    CONTRACT_CANCELLED("계약서 취소 알림", "contract-cancelled.html"),
    CONTRACT_EXPIRED("계약서 만료 알림", "contract-expired.html"),
    EXPIRATION_WARNING("계약서 만료 임박 알림", "expiration-warning.html"),
    CONTRACT_REMINDER("계약서 서명 독촉", "contract-reminder.html"),
    EMAIL_VERIFICATION("이메일 인증", "email-verification.html"),
    ACCOUNT_LOCKED("계정 잠금 안내", "account-locked.html"),
    ACCOUNT_UNLOCKED("계정 해제 및 임시 비밀번호", "account-unlocked.html");

    private final String subject;
    private final String templateName;

    EmailTemplate(
            String subject,
            String templateName
    ) {
        this.subject = subject;
        this.templateName = templateName;
    }

    public String getSubject() {
        return subject;
    }

    public String getTemplateName() {
        return templateName;
    }
}
