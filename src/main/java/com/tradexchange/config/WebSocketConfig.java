package com.tradexchange.config;

import com.tradexchange.websocket.SocketIoWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SocketIoWebSocketHandler socketIoWebSocketHandler;

    public WebSocketConfig(SocketIoWebSocketHandler socketIoWebSocketHandler) {
        this.socketIoWebSocketHandler = socketIoWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Socket.IO v4 client WebSocket transport endpoint
        registry.addHandler(socketIoWebSocketHandler, "/socket.io/", "/socket.io/**")
                .setAllowedOriginPatterns("*");

        // Standard WebSocket endpoint
        registry.addHandler(socketIoWebSocketHandler, "/ws", "/ws/**")
                .setAllowedOriginPatterns("*");
    }
}
