package com.signly.notification.infrastructure;

import com.signly.notification.application.dto.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Component
public class SmtpEmailSender implements EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final String fromEmail;

    public SmtpEmailSender(
            JavaMailSender mailSender,
            @Value("${app.email.from:noreply@signly.com}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.templateEngine = createTemplateEngine();
    }

    @Override
    public void sendEmail(EmailRequest request) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(request.to());
            helper.setSubject(request.template().getSubject());
            helper.setText(renderTemplate(request), true);

            // 첨부파일 추가
            if (request.hasAttachments()) {
                for (var attachment : request.attachments()) {
                    helper.addAttachment(
                            attachment.getFileName(),
                            () -> new ByteArrayInputStream(attachment.getContent()),
                            attachment.getContentType()
                    );
                    logger.debug("첨부파일 추가: fileName={}, size={}bytes",
                            attachment.getFileName(), attachment.getSizeInBytes());
                }
            }

            mailSender.send(mimeMessage);
            logger.info("이메일 전송 완료: template={}, to={} <{}>, attachments={}",
                    request.template(), request.toName(), request.to(),
                    request.hasAttachments() ? request.attachments().size() : 0);
        } catch (MessagingException e) {
            logger.error("이메일 전송 실패: to={} <{}>", request.toName(), request.to(), e);
        }
    }

    private SpringTemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/email/");
        resolver.setSuffix("");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(false);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private String renderTemplate(EmailRequest request) {
        Context context = new Context(Locale.KOREAN);
        Map<String, Object> variables = request.templateVariables();
        if (variables != null) {
            context.setVariables(variables);
        }
        context.setVariable("recipientName", request.toName());
        return templateEngine.process(request.template().getTemplateName(), context);
    }
}
