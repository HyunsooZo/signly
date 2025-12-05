package com.signly.common.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailTemplate {
    CONTRACT_SIGNING_REQUEST("계약서 서명 요청", "contract-signing-request"),
    CONTRACT_SIGNED("계약서 서명 완료", "contract-signed"),
    CONTRACT_COMPLETED("계약서 완료", "contract-completed"),
    CONTRACT_CANCELLED("계약서 취소", "contract-cancelled"),
    CONTRACT_EXPIRED("계약서 만료", "contract-expired"),
    USER_WELCOME("회원가입 환영", "user-welcome"),
    PASSWORD_RESET("비밀번호 재설정", "password-reset"),
    EMAIL_VERIFICATION("이메일 인증", "email-verification");

    private final String subject;
    private final String templateName;

}