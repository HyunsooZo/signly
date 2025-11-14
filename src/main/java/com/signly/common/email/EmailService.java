package com.signly.common.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.email.from:noreply@signly.com}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendSimpleEmail(
            String to,
            String subject,
            String text
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    public void sendTemplateEmail(
            String to,
            EmailTemplate template,
            Map<String, Object> variables
    ) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // 간단한 HTML 템플릿 생성 (실제로는 파일에서 읽어오거나 별도 템플릿 엔진 사용)
            String htmlContent = generateEmailTemplate(template, variables);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(template.getSubject());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 중 오류가 발생했습니다", e);
        }
    }

    private String generateEmailTemplate(
            EmailTemplate template,
            Map<String, Object> variables
    ) {
        // 간단한 템플릿 생성 (추후 개선 필요)
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'></head><body>");
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        html.append("<h2>").append(template.getSubject()).append("</h2>");

        switch (template) {
            case CONTRACT_SIGNING_REQUEST:
                html.append("<p>안녕하세요, ").append(variables.get("signerName")).append("님</p>");
                html.append("<p>").append(variables.get("contractTitle")).append(" 계약서에 서명을 요청드립니다.</p>");
                html.append("<p><a href='").append(variables.get("contractUrl")).append("'>여기를 클릭하여 서명하기</a></p>");
                break;
            case CONTRACT_SIGNED:
                html.append("<p>").append(variables.get("signerName")).append("님이 계약서에 서명하였습니다.</p>");
                html.append("<p>계약서: ").append(variables.get("contractTitle")).append("</p>");
                break;
            case PASSWORD_RESET:
                html.append("<p>안녕하세요, ").append(variables.get("userName")).append("님</p>");
                html.append("<p>비밀번호 재설정을 요청하셨습니다.</p>");
                html.append("<p>아래 링크를 클릭하여 비밀번호를 재설정해주세요:</p>");
                html.append("<p><a href='").append(variables.get("resetUrl")).append("' style='display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px;'>비밀번호 재설정하기</a></p>");
                html.append("<p>이 링크는 ").append(variables.get("expiryHours")).append("시간 동안 유효합니다.</p>");
                html.append("<p style='color: #666; font-size: 12px;'>본인이 요청하지 않은 경우 이 메일을 무시하셔도 됩니다.</p>");
                break;
            default:
                html.append("<p>Signly에서 알림 메일을 보내드립니다.</p>");
        }

        html.append("<br><p>감사합니다.<br>").append(variables.get("companyName")).append("</p>");
        html.append("</div></body></html>");

        return html.toString();
    }

    public void sendContractSigningRequest(
            String to,
            String contractTitle,
            String signerName,
            String contractUrl
    ) {
        Map<String, Object> variables = Map.of(
                "signerName", signerName,
                "contractTitle", contractTitle,
                "contractUrl", contractUrl,
                "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.CONTRACT_SIGNING_REQUEST, variables);
    }

    public void sendContractSigned(
            String to,
            String contractTitle,
            String signerName
    ) {
        Map<String, Object> variables = Map.of(
                "contractTitle", contractTitle,
                "signerName", signerName,
                "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.CONTRACT_SIGNED, variables);
    }

    public void sendContractCompleted(
            String to,
            String contractTitle
    ) {
        Map<String, Object> variables = Map.of(
                "contractTitle", contractTitle,
                "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.CONTRACT_COMPLETED, variables);
    }

    public void sendContractCancelled(
            String to,
            String contractTitle,
            String reason
    ) {
        Map<String, Object> variables = Map.of(
                "contractTitle", contractTitle,
                "reason", reason,
                "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.CONTRACT_CANCELLED, variables);
    }

    public void sendContractExpired(
            String to,
            String contractTitle
    ) {
        Map<String, Object> variables = Map.of(
                "contractTitle", contractTitle,
                "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.CONTRACT_EXPIRED, variables);
    }

    public void sendWelcomeEmail(
            String to,
            String userName
    ) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.USER_WELCOME, variables);
    }

    public void sendPasswordResetEmail(
            String to,
            String userName,
            String resetToken,
            String baseUrl
    ) {
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "resetUrl", resetUrl,
                "expiryHours", "24",
                "companyName", "Signly"
        );

        sendTemplateEmail(to, EmailTemplate.PASSWORD_RESET, variables);
    }
}