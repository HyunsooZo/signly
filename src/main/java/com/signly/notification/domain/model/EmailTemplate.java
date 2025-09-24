package com.signly.notification.domain.model;

public enum EmailTemplate {
    CONTRACT_SIGNING_REQUEST("계약서 서명 요청", "contract-signing-request.html"),
    CONTRACT_COMPLETED("계약서 완료 알림", "contract-completed.html"),
    CONTRACT_CANCELLED("계약서 취소 알림", "contract-cancelled.html"),
    CONTRACT_EXPIRED("계약서 만료 알림", "contract-expired.html");

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