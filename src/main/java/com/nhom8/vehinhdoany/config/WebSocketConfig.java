package com.nhom8.vehinhdoany.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.nhom8.vehinhdoany.websocket.GameWebSocketHandler;

/**
 * Cấu hình Spring WebSocket.
 * Đăng ký endpoint /ws để React Client kết nối.
 * Thay thế cơ chế WebSocketServer thuần (Java-WebSocket library) trong phiên bản cũ.
 *
 * React Client kết nối tới: ws://localhost:9999/ws
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameWebSocketHandler gameWebSocketHandler;

    public WebSocketConfig(GameWebSocketHandler gameWebSocketHandler) {
        this.gameWebSocketHandler = gameWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameWebSocketHandler, "/ws")
                .setAllowedOrigins("*"); // Cho phép mọi origin (React dev server)
    }
}
