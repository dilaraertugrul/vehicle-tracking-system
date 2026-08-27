package com.example.vehicletrackingbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {


    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry config
    ) {

        // Backend'in React'e mesaj göndereceği kanal.
        config.enableSimpleBroker("/topic");

        // Frontend'den backend'e mesaj gönderilecekse kullanılacak prefix.
        config.setApplicationDestinationPrefixes("/app");
    }


    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry
    ) {

        // React'in WebSocket bağlantısı kuracağı endpoint.
        registry
                .addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                );
    }
}