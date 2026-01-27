package com.roadtech.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

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
    public void onUpdate(@RequestBody Map<String, Object> payload) {

        Map<?, ?> message = (Map<?, ?>) payload.get("message");
        if (message == null) return;

        Map<?, ?> chat = (Map<?, ?>) message.get("chat");
        String text = (String) message.get("text");

        if (chat == null || text == null) return;

        Long chatId = ((Number) chat.get("id")).longValue();

        // Expect: /link mechanic@email.com
        if (text.startsWith("/link")) {

            String email = text.replace("/link", "").trim();

            userRepository.findByEmail(email).ifPresentOrElse(user -> {

                if (user.getRole() != User.UserRole.MECHANIC) {
                    log.warn("User {} tried Telegram link but is not MECHANIC", email);
                    return;
                }

                user.setTelegramChatId(chatId);
                userRepository.save(user);

                log.info("Telegram linked: userId={}, chatId={}", user.getId(), chatId);

            }, () -> log.warn("Telegram link failed: email not found {}", email));
        }
    }
}
