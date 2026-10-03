package com.nhom8.vehinhdoany;

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
        SpringApplication.run(Application.class, args);
        System.out.println("[SERVER] ================================================");
        System.out.println("[SERVER]   Vẽ Hình Đoán Ý - Spring Boot Server (Nhóm 8)");
        System.out.println("[SERVER]   Đang lắng nghe tại cổng: 9999");
        System.out.println("[SERVER]   WebSocket endpoint: ws://localhost:9999/ws");
        System.out.println("[SERVER]   Chờ kết nối từ Client React...");
        System.out.println("[SERVER] ================================================");
    }
}
