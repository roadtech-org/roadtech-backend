package com.roadtech.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.roadtech.entity.User;
import com.roadtech.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookController {

    private final UserRepository userRepository;

    @PostMapping("/webhook")
    public ResponseEntity<String> onUpdate(@RequestBody Map<String, Object> payload) {

        log.info("Telegram update received: {}", payload);

        Map<?, ?> message = (Map<?, ?>) payload.get("message");
        if (message == null) return ResponseEntity.ok("no message");

        Map<?, ?> chat = (Map<?, ?>) message.get("chat");
        String text = (String) message.get("text");

        if (chat == null || text == null) return ResponseEntity.ok("invalid");

        Long chatId = ((Number) chat.get("id")).longValue();

        if (text.startsWith("/link")) {

            String email = text.replace("/link", "").trim();

            userRepository.findByEmail(email).ifPresentOrElse(user -> {

                if (user.getRole() != User.UserRole.MECHANIC) {
                    log.warn("Telegram link rejected: {} not MECHANIC", email);
                    return;
                }

                user.setTelegramChatId(chatId);
                userRepository.save(user);

                log.info("Telegram linked successfully: userId={}, chatId={}",
                        user.getId(), chatId);

            }, () -> log.warn("Telegram link failed: email not found {}", email));
        }

        return ResponseEntity.ok("ok");
    }
}
