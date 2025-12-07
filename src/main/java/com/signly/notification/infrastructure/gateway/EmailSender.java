package com.signly.notification.infrastructure.gateway;

import com.signly.notification.application.dto.EmailRequest;

public interface EmailSender {
    void sendEmail(EmailRequest request);
}