# Dự án Vẽ Hình Đoán Ý (Nhóm 8)

Hệ thống trò chơi "Vẽ Hình Đoán Ý" nhiều người chơi thời gian thực (Pictionary Online) sử dụng kiến trúc WebSocket.

- **Backend**: Java 21, Spring Boot WebSocket, Hibernate ORM, H2/MySQL, BCrypt.
- **Client**: JavaFX Desktop.

---

## 🚀 HƯỚNG DẪN KHỞI CHẠY (QUICK START)

Chi tiết đầy đủ xem tại: [HUONG_DAN_CHAY.md](HUONG_DAN_CHAY.md).

### 1. Bật Backend Server (Java Maven)
Mở Terminal tại thư mục gốc dự án:
```bash
mvn -pl common install -DskipTests
mvn -pl server spring-boot:run
```
*(Server chạy tại `ws://localhost:8080/ws/game`.)*

---

### 2. Bật Client JavaFX
Mở Terminal mới:
```bash
mvn -pl fx-client javafx:run
```
Mở thêm terminal và chạy lại lệnh để thử nhiều cửa sổ. Tài khoản `admin` mở màn **Quản trị** trong sảnh.

---

## ⚙️ CẤU HÌNH DATABASE
Mặc định dự án dùng H2 lưu tại `server/data/game_db.mv.db`. Cấu hình MySQL tùy chọn nằm trong `server/src/main/resources/application-mysql.properties`.

## Đóng gói và demo LAN

Tắt server đang chạy, rồi chạy `powershell -ExecutionPolicy Bypass -File .\scripts\build-demo.ps1`. Hai file ZIP được tạo trong `dist`: gói chạy Windows và gói mã nguồn. Hướng dẫn máy chủ, firewall và client nằm trong [demo/DEMO.md](demo/DEMO.md).
