package com.nhom8.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Điểm khởi chạy (Entry Point) của ứng dụng Spring Boot.
 * Thay thế class ServerCore.main() trong phiên bản cũ.
 * Spring Boot tự động quản lý:
 * - Kết nối Database (Spring Data JPA)
 * - WebSocket Server (Spring WebSocket)
 * - Dependency Injection (@Autowired)
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        var ctx = SpringApplication.run(Application.class, args);
        String port = ctx.getEnvironment().getProperty("server.port", "8080");
        System.out.println("[SERVER] ================================================");
        System.out.println("[SERVER]   Vẽ Hình Đoán Ý - Spring Boot Server (Nhóm 8)");
        System.out.println("[SERVER]   Đang lắng nghe tại cổng: " + port);
        System.out.println("[SERVER]   WebSocket endpoint: ws://localhost:" + port + "/ws/game");
        System.out.println("[SERVER]   Chờ kết nối từ Client JavaFX...");
        System.out.println("[SERVER] ================================================");
    }
}
