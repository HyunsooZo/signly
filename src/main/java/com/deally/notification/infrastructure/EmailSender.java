package com.deally.notification.infrastructure;

import com.deally.notification.application.dto.EmailRequest;

public interface EmailSender {
    void sendEmail(EmailRequest request);
}