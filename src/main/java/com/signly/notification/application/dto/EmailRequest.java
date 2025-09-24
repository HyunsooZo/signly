package com.signly.notification.application.dto;

import com.signly.notification.domain.model.EmailTemplate;

import java.util.Map;

public record EmailRequest(
    String to,
    String toName,
    EmailTemplate template,
    Map<String, Object> templateVariables
) {}