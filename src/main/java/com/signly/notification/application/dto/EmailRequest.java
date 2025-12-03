package com.signly.notification.application.dto;

import com.signly.notification.domain.model.EmailAttachment;
import com.signly.notification.domain.model.EmailTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record EmailRequest(
        String to,
        String toName,
        EmailTemplate template,
        Map<String, Object> templateVariables,
        List<EmailAttachment> attachments
) {
    /**
     * 첨부파일이 없는 이메일 요청 생성자
     */
    public EmailRequest(
            String to,
            String toName,
            EmailTemplate template,
            Map<String, Object> templateVariables
    ) {
        this(to, toName, template, templateVariables, Collections.emptyList());
    }

    /**
     * 첨부파일이 있는지 확인
     */
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }
}