package com.roadtech.service;

import com.roadtech.dto.request.ServiceRequestDto;
import com.roadtech.entity.ServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyNewRequest(ServiceRequest request) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NEW_REQUEST");
        message.put("payload", ServiceRequestDto.fromEntity(request));
        message.put("timestamp", System.currentTimeMillis());

        // Broadcast to all available mechanics
        messagingTemplate.convertAndSend("/topic/mechanic/requests", message);
        log.debug("Notified mechanics about new request: {}", request.getId());
    }

    public void notifyRequestStatusUpdate(ServiceRequest request) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "STATUS_UPDATE");
        message.put("payload", ServiceRequestDto.fromEntityWithDetails(request));
        message.put("timestamp", System.currentTimeMillis());

        // Notify the user
        messagingTemplate.convertAndSend(
                "/topic/user/" + request.getUser().getId(),
                message
        );

        // Notify on request-specific topic
        messagingTemplate.convertAndSend(
                "/topic/request/" + request.getId(),
                message
        );

        log.debug("Sent status update for request {}: {}", request.getId(), request.getStatus());
    }

    public void notifyRequestCancelled(ServiceRequest request) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_CANCELLED");
        message.put("payload", ServiceRequestDto.fromEntity(request));
        message.put("timestamp", System.currentTimeMillis());

        // Notify the assigned mechanic
        if (request.getMechanic() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/mechanic/" + request.getMechanic().getId(),
                    message
            );
        }

        log.debug("Notified about cancelled request: {}", request.getId());
    }

    public void notifyLocationUpdate(Long requestId, Long mechanicId, BigDecimal latitude, BigDecimal longitude) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOCATION_UPDATE");

        Map<String, Object> payload = new HashMap<>();
        payload.put("requestId", requestId);
        payload.put("mechanicId", mechanicId);
        payload.put("latitude", latitude);
        payload.put("longitude", longitude);

        message.put("payload", payload);
        message.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/request/" + requestId, message);
        log.debug("Sent location update for request {}", requestId);
    }
}
