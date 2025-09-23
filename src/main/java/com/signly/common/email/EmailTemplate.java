package com.signly.common.email;

public enum EmailTemplate {
    CONTRACT_SIGNING_REQUEST("계약서 서명 요청", "contract-signing-request"),
    CONTRACT_SIGNED("계약서 서명 완료", "contract-signed"),
    CONTRACT_COMPLETED("계약서 완료", "contract-completed"),
    CONTRACT_CANCELLED("계약서 취소", "contract-cancelled"),
    CONTRACT_EXPIRED("계약서 만료", "contract-expired"),
    USER_WELCOME("회원가입 환영", "user-welcome"),
    PASSWORD_RESET("비밀번호 재설정", "password-reset");

    private final String subject;
    private final String templateName;

    EmailTemplate(String subject, String templateName) {
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