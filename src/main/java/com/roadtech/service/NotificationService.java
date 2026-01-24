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

/**
 * Service responsible for sending real-time notifications
 * to users and mechanics using WebSockets (STOMP).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    /**
     * Spring-provided helper class to send messages
     * to WebSocket destinations (topics/queues).
     */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Notifies all available mechanics when a new service request is created.
     * This is typically used to broadcast a new job request.
     */
    public void notifyNewRequest(ServiceRequest request) {

        // Create a generic message structure
        Map<String, Object> message = new HashMap<>();

        // Type helps frontend identify how to handle this message
        message.put("type", "NEW_REQUEST");

        // Convert entity to DTO to avoid exposing internal fields
        message.put("payload", ServiceRequestDto.fromEntity(request));

        // Timestamp helps frontend with ordering / freshness
        message.put("timestamp", System.currentTimeMillis());

        // Broadcast message to all mechanics subscribed to this topic
        messagingTemplate.convertAndSend("/topic/mechanic/requests", message);

        log.debug("Notified mechanics about new request: {}", request.getId());
    }

    /**
     * Notifies relevant parties when the status of a service request changes
     * (e.g., ACCEPTED, IN_PROGRESS, COMPLETED).
     */
    public void notifyRequestStatusUpdate(ServiceRequest request) {

        Map<String, Object> message = new HashMap<>();
        message.put("type", "STATUS_UPDATE");

        // Detailed DTO usually contains mechanic, cost, or status info
        message.put("payload", ServiceRequestDto.fromEntityWithDetails(request));
        message.put("timestamp", System.currentTimeMillis());

        // Notify the user who created the request
        messagingTemplate.convertAndSend(
                "/topic/user/" + request.getUser().getId(),
                message
        );

        // Notify anyone subscribed to this specific request
        messagingTemplate.convertAndSend(
                "/topic/request/" + request.getId(),
                message
        );

        log.debug("Sent status update for request {}: {}", request.getId(), request.getStatus());
    }

    /**
     * Notifies the assigned mechanic when a service request is cancelled.
     */
    public void notifyRequestCancelled(ServiceRequest request) {

        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_CANCELLED");
        message.put("payload", ServiceRequestDto.fromEntity(request));
        message.put("timestamp", System.currentTimeMillis());

        // Send cancellation only if a mechanic was assigned
        if (request.getMechanic() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/mechanic/" + request.getMechanic().getId(),
                    message
            );
        }

        log.debug("Notified about cancelled request: {}", request.getId());
    }

    /**
     * Sends live location updates of the mechanic to the client.
     * Useful for tracking mechanic movement in real time.
     */
    public void notifyLocationUpdate(Long requestId,
                                     Long mechanicId,
                                     BigDecimal latitude,
                                     BigDecimal longitude) {

        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOCATION_UPDATE");

        // Payload contains only location-specific data
        Map<String, Object> payload = new HashMap<>();
        payload.put("requestId", requestId);
        payload.put("mechanicId", mechanicId);
        payload.put("latitude", latitude);
        payload.put("longitude", longitude);

        message.put("payload", payload);
        message.put("timestamp", System.currentTimeMillis());

        // Send update to all subscribers of this request
        messagingTemplate.convertAndSend("/topic/request/" + requestId, message);

        log.debug("Sent location update for request {}", requestId);
    }
}
