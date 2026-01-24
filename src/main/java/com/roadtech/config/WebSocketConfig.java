package com.roadtech.config;

// Custom interceptor used to authenticate WebSocket/STOMP messages
import com.roadtech.websocket.WebSocketAuthInterceptor;

// Lombok annotation to auto-generate a constructor for final fields
import lombok.RequiredArgsConstructor;

// Marks this class as a Spring configuration class
import org.springframework.context.annotation.Configuration;

// Used to configure message channels (inbound/outbound)
import org.springframework.messaging.simp.config.ChannelRegistration;

// Used to configure message broker settings
import org.springframework.messaging.simp.config.MessageBrokerRegistry;

// Enables STOMP-based WebSocket message handling
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

// Used to register WebSocket/STOMP endpoints
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

// Interface that provides callback methods to customize WebSocket config
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Interceptor that will handle authentication for incoming WebSocket messages
    private final WebSocketAuthInterceptor authInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enables a simple in-memory message broker
        // /topic → for broadcasting messages
        // /queue → for point-to-point (user-specific) messaging
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages sent from clients that should be routed
        // to @MessageMapping methods in controllers
        config.setApplicationDestinationPrefixes("/app");

        // Prefix used for user-specific destinations
        // Example: /user/{username}/queue/notifications
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registers the WebSocket endpoint that clients will connect to
        // Example connection URL: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                // Allows WebSocket connections only from these origins
                .setAllowedOrigins("http://localhost:5173", "http://localhost:80", "http://localhost")
                // Enables SockJS fallback options for browsers that don’t support WebSocket
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Registers a custom interceptor for inbound client messages
        // Typically used for authentication, authorization, or logging
        registration.interceptors(authInterceptor);
    }
}
