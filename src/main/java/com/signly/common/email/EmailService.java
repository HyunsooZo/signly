package com.signly.common.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                       TemplateEngine templateEngine,
                       @Value("${app.email.from:noreply@signly.com}") String fromEmail) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromEmail = fromEmail;
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    public void sendTemplateEmail(String to, EmailTemplate template, Map<String, Object> variables) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process("email/" + template.getTemplateName(), context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(template.getSubject());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 중 오류가 발생했습니다", e);
        }
    }

    public void sendContractSigningRequest(String to, String contractTitle, String signerName, String contractUrl) {
        Map<String, Object> variables = Map.of(
            "signerName", signerName,
            "contractTitle", contractTitle,
            "contractUrl", contractUrl,
            "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.CONTRACT_SIGNING_REQUEST, variables);
    }

    public void sendContractSigned(String to, String contractTitle, String signerName) {
        Map<String, Object> variables = Map.of(
            "contractTitle", contractTitle,
            "signerName", signerName,
            "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.CONTRACT_SIGNED, variables);
    }

    public void sendContractCompleted(String to, String contractTitle) {
        Map<String, Object> variables = Map.of(
            "contractTitle", contractTitle,
            "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.CONTRACT_COMPLETED, variables);
    }

    public void sendContractCancelled(String to, String contractTitle, String reason) {
        Map<String, Object> variables = Map.of(
            "contractTitle", contractTitle,
            "reason", reason,
            "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.CONTRACT_CANCELLED, variables);
    }

    public void sendContractExpired(String to, String contractTitle) {
        Map<String, Object> variables = Map.of(
            "contractTitle", contractTitle,
            "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.CONTRACT_EXPIRED, variables);
    }

    public void sendWelcomeEmail(String to, String userName) {
        Map<String, Object> variables = Map.of(
            "userName", userName,
            "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.USER_WELCOME, variables);
    }
}