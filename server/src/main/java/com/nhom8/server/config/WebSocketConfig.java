package com.nhom8.server.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.nhom8.server.ws.GameWebSocketHandler;

/**
 * Cấu hình Spring WebSocket.
 * Đăng ký endpoint /ws/game để Client kết nối và cấu hình buffer 10MB
 * để truyền tải tranh vẽ vector không bị quá tải buffer làm ngắt kết nối.
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
        registry.addHandler(gameWebSocketHandler, "/ws/game")
                .setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(10 * 1024 * 1024); // 10MB
        container.setMaxBinaryMessageBufferSize(10 * 1024 * 1024); // 10MB
        container.setMaxSessionIdleTimeout(30 * 60 * 1000L); // 30 phút
        return container;
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatWebSocketCustomizer() {
        return factory -> {
            factory.addInitializers(servletContext -> {
                servletContext.setInitParameter("org.apache.tomcat.websocket.textBufferSize", "10485760");
                servletContext.setInitParameter("org.apache.tomcat.websocket.binaryBufferSize", "10485760");
            });
            factory.addContextCustomizers(context -> {
                context.addServletContainerInitializer((sci, servletContext) -> {
                    if (servletContext != null) {
                        servletContext.setAttribute("org.apache.tomcat.websocket.textBufferSize", 10 * 1024 * 1024);
                        servletContext.setAttribute("org.apache.tomcat.websocket.binaryBufferSize", 10 * 1024 * 1024);
                        try {
                            org.apache.tomcat.websocket.server.WsServerContainer container =
                                    (org.apache.tomcat.websocket.server.WsServerContainer) servletContext
                                            .getAttribute("jakarta.websocket.server.ServerContainer");
                            if (container != null) {
                                container.setDefaultMaxTextMessageBufferSize(10 * 1024 * 1024);
                                container.setDefaultMaxBinaryMessageBufferSize(10 * 1024 * 1024);
                            }
                        } catch (Exception ignored) {}
                    }
                }, java.util.Collections.emptySet());
            });
        };
    }
}

