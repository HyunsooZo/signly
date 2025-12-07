package com.signly.notification.infrastructure.gateway;

import com.signly.notification.domain.model.ErrorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DiscordWebhookClient {

    private static final Logger logger = LoggerFactory.getLogger(DiscordWebhookClient.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int ERROR_COLOR = 15158332; // 빨간색

    private final String webhookUrl;
    private final RestTemplate restTemplate;

    public DiscordWebhookClient(
            @Value("${notification.discord.webhook-url}") String webhookUrl
    ) {
        this.webhookUrl = webhookUrl;
        this.restTemplate = new RestTemplate();
    }

    public void sendErrorNotification(ErrorContext errorContext) {
        try {
            var payload = this.createPayload(errorContext);

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            var request = new HttpEntity<Map<String, Object>>(payload, headers);

            restTemplate.postForEntity(webhookUrl, request, String.class);

            logger.info("Discord 에러 알림 전송 완료: {}", errorContext.errorType());
        } catch (Exception e) {
            // Discord 알림 실패는 애플리케이션 동작에 영향을 주면 안됨
            logger.error("Discord 알림 전송 실패", e);
        }
    }

    private Map<String, Object> createPayload(ErrorContext errorContext) {
        var embed = new HashMap<String, Object>();
        embed.put("title", "🚨 운영 서버 에러 발생");
        embed.put("color", ERROR_COLOR);
        embed.put("timestamp", errorContext.timestamp().toString());

        var fields = List.of(
                createField("에러 타입", errorContext.errorType(), true),
                createField("시간", errorContext.timestamp().format(FORMATTER), true),
                createField("요청 URL", formatRequestInfo(errorContext), false),
                createField("사용자 IP", errorContext.userIp(), true),
                createField("에러 메시지", truncate(errorContext.message(), 1000), false),
                createField("스택 트레이스", "```java\n" + truncate(errorContext.stackTrace(), 1000) + "\n```", false)
        );
        embed.put("fields", fields);

        var payload = new HashMap<String, Object>();
        payload.put("embeds", List.of(embed));

        return payload;
    }

    private Map<String, Object> createField(
            String name,
            String value,
            boolean inline
    ) {
        var field = new HashMap<String, Object>();
        field.put("name", name);
        field.put("value", value != null && !value.isEmpty() ? value : "N/A");
        field.put("inline", inline);
        return field;
    }

    private String formatRequestInfo(ErrorContext errorContext) {
        return String.format("%s %s",
                errorContext.requestMethod() != null ? errorContext.requestMethod() : "UNKNOWN",
                errorContext.requestUrl() != null ? errorContext.requestUrl() : "N/A"
        );
    }

    private String truncate(
            String text,
            int maxLength
    ) {
        if (text == null) {
            return "N/A";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
