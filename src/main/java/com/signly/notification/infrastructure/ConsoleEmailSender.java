package com.signly.notification.infrastructure;

import com.signly.notification.application.dto.EmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mock-mail")
public class ConsoleEmailSender implements EmailSender {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleEmailSender.class);

    @Override
    public void sendEmail(EmailRequest request) {
        logger.info("=== EMAIL SENT (Console Mock) ===");
        logger.info("To: {} <{}>", request.toName(), request.to());
        logger.info("Template: {}", request.template().getSubject());
        logger.info("Variables: {}", request.templateVariables());
        if (request.hasAttachments()) {
            logger.info("Attachments: {}", request.attachments().size());
            request.attachments().forEach(attachment ->
                    logger.info("  - {} ({} bytes)", attachment.getFileName(), attachment.getSizeInBytes())
            );
        }
        logger.info("================================");
    }
}
