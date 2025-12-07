package com.signly.notification.application;

import com.signly.notification.domain.model.ErrorContext;
import com.signly.notification.infrastructure.gateway.DiscordWebhookClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DiscordNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(DiscordNotificationService.class);

    private final DiscordWebhookClient webhookClient;
    private final boolean enabled;

    public DiscordNotificationService(
            DiscordWebhookClient webhookClient,
            @Value("${notification.discord.enabled:false}") boolean enabled
    ) {
        this.webhookClient = webhookClient;
        this.enabled = enabled;
    }

    @Async
    public void sendErrorNotification(ErrorContext errorContext) {
        if (!enabled) {
            logger.debug("Discord 알림이 비활성화되어 있습니다.");
            return;
        }

        try {
            webhookClient.sendErrorNotification(errorContext);
        } catch (Exception e) {
            logger.error("Discord 에러 알림 전송 중 예외 발생", e);
        }
    }
}
