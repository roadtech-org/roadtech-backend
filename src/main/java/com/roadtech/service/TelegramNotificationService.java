package com.roadtech.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TelegramNotificationService {

    // Safety: empty default prevents startup failure
    @Value("${telegram.bot.token:}")
    private String botToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(Long chatId, String message) {

        // 🛡️ Safety 1: chat not linked
        if (chatId == null) {
            log.debug("Telegram chatId is null, skipping message");
            return;
        }

        // 🛡️ Safety 2: bot token missing
        if (botToken == null || botToken.isBlank()) {
            log.warn("Telegram bot token not configured, skipping Telegram notification");
            return;
        }

        try {
            String url =
                    "https://api.telegram.org/bot" + botToken + "/sendMessage";

            Map<String, Object> body = Map.of(
                    "chat_id", chatId,
                    "text", message
            );

            restTemplate.postForObject(url, body, String.class);

            log.debug("Telegram message sent to chatId={}", chatId);

        } catch (RestClientException ex) {
            // 🛡️ Safety 3: network / Telegram API issues
            log.error("Failed to send Telegram message to chatId={}", chatId, ex);
        }
    }
}
