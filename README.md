# Dự án Vẽ Hình Đoán Ý (Nhóm 8)

Hệ thống trò chơi "Vẽ Hình Đoán Ý" nhiều người chơi thời gian thực (Pictionary Online) sử dụng kiến trúc WebSocket.

- **Backend**: Java 21 trở lên, Spring Boot 3.3.5, WebSocket, Hibernate/JPA, H2 2.2.224 (MySQL tùy chọn), BCrypt.
- **Client**: JavaFX 21.0.5 Desktop. Build bằng Maven, không cần Node.js.

Luồng: JavaFX → WebSocket `/ws/game` → `GameWebSocketHandler` → `GameService`/`AuthService`/`FriendService` → `GameSession`/`Room` → Spring Data JPA → H2. `common` chứa message/DTO dùng chung; `server` chứa nghiệp vụ và DB; `fx-client` chứa giao diện desktop. Phòng và trận đang chơi lưu trong RAM, kết quả cuối lưu DB.

---

## 🚀 HƯỚNG DẪN KHỞI CHẠY (QUICK START)

Chi tiết đầy đủ xem tại: [HUONG_DAN_CHAY.md](HUONG_DAN_CHAY.md).

### 1. Bật Backend Server (Java Maven)
Mở Terminal tại thư mục gốc dự án:
```bash
mvn -B -DskipTests install
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

Máy mới cần JDK 21+ (`java -version`), Maven 3.9+ (`mvn -version`) và Git. Trên Windows PowerShell, ví dụ `git clone https://github.com/vuhuy2901/BTL-LTM.git`, `Set-Location BTL-LTM`, chạy `mvn.cmd -B -DskipTests install`, rồi hai lệnh ở trên trong hai terminal khác nhau. Nếu đang đứng tại thư mục `server` và chạy JAR trực tiếp thì H2 lưu ở `server/data/game_db.mv.db`; với `spring-boot:run` từ root, đường dẫn H2 tương đối với root. Dùng `DB_URL` đường dẫn tuyệt đối nếu cần thống nhất hai cách chạy.

---

## ⚙️ CẤU HÌNH DATABASE
Mặc định dự án dùng H2 lưu tại `server/data/game_db.mv.db`. Cấu hình MySQL tùy chọn nằm trong `server/src/main/resources/application-mysql.properties`.

H2 nhúng tự tạo schema qua Hibernate `ddl-auto=update`; không cần khởi động DB riêng hay nhập SQL bằng tay. Các từ khóa tích hợp nằm trong mã server; topic do quản trị viên thêm được lưu trong DB. Trên máy mới mặc định **không tạo tài khoản demo**: đăng ký tài khoản qua client. Để chạy thử với 3 người, đăng ký 3 tài khoản khác nhau, tạo phòng, cho đủ 3 người sẵn sàng rồi bắt đầu. Phòng có tối đa 8 người; chọn từ 10 giây, vẽ mặc định 60 giây, đoán 30 giây. Test tự động bật seed riêng.

Xem [.env.example](.env.example) để biết các biến cấu hình. PowerShell không tự nạp `.env`: ví dụ `$env:SERVER_PORT='8081'`, `$env:DB_URL='jdbc:h2:file:C:/data/game_db'`, sau đó khởi động server trong cùng terminal. `SERVER_PORT`, `SERVER_ADDRESS`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `GAME_DEMO_SEED_ENABLED`, `WS_ALLOWED_ORIGINS` có default cho phát triển. Không bật `GAME_DEMO_SEED_ENABLED=true` trên máy có thể truy cập công khai. Client dùng ô địa chỉ server hoặc JVM `-Dscribble.server=host:port`; không có HTTP frontend port/API REST cho trò chơi. Nếu dùng MySQL, chọn profile `mysql`, cấu hình `DB_URL`, `DB_USER`, `DB_PASSWORD` và tự chạy MySQL trước; profile này chưa được kiểm chứng bằng clean install.

Chạy kiểm thử: `mvn.cmd -B test` trong root. Có thể mở ba client bằng `mvn.cmd -pl fx-client javafx:run` ở ba terminal sau khi server sẵn sàng. Script `powershell -ExecutionPolicy Bypass -File .\scripts\run-game-with-3-clients.ps1` dùng khi **cổng 8080 chưa có server**; đuôi file là `.ps1`, không phải `.ps14`. Script này tự build và bật server. Nếu server đã chạy, chỉ mở client.

Chưa có Docker Compose do client JavaFX cần desktop/display và H2 đã nhúng trong server; quy trình Maven ở trên là cách chạy được hỗ trợ. Không có Maven Wrapper trong repo nên phải cài Maven. Bản demo đóng gói cho Windows nằm ở `demo/` và `scripts/build-demo.ps1`.

Lỗi thường gặp: port 8080 đã dùng thì tắt tiến trình cũ hoặc đặt `SERVER_PORT` và nhập địa chỉ mới trong client; lỗi khóa file H2 thường do hai server cùng mở một DB, nên chỉ giữ một server/DB; lỗi build thiếu common thì chạy `mvn.cmd -B -DskipTests install` từ root. Không dùng profile `h2` cũ với bản JAR trước lần sửa này trên DB cần giữ vì profile trước dùng `ddl-auto=create`.

An toàn: tài khoản demo chỉ bật theo tùy chọn, mật khẩu thực tế không đưa vào `.env.example` hoặc Git. Nếu DB cũ có tài khoản demo, cần đổi mật khẩu trước khi mở ra mạng. Với triển khai ngoài LAN tin cậy, đặt `WS_ALLOWED_ORIGINS` cụ thể, dùng TLS và quản lý tài khoản admin riêng. Dữ liệu trận đang chạy không sống qua restart; migration hiện dựa vào Hibernate `update`, chưa có lịch sử migration đánh số.

Đóng góp: tạo nhánh riêng, chạy test, xem `git diff --check`, gửi PR; cập nhật `game_rules_nhom8.md` mỗi khi sửa luật. Các kết quả audit chi tiết ở [docs/audit/FULL_PROJECT_REPORT.md](docs/audit/FULL_PROJECT_REPORT.md) và [docs/database/DATABASE_AUDIT.md](docs/database/DATABASE_AUDIT.md).

## Ảnh đại diện

Trong **Trang cá nhân**, chọn **Tải ảnh từ máy…** (PNG/JPG, tối đa 10 MB).
Kéo ảnh trong khung tròn, dùng thanh trượt hoặc cuộn chuột để thu/phóng và xem trước.
Nhấn **Lưu ảnh đại diện** để lưu; **Hủy thay đổi** khôi phục ảnh trước đó.
Các biểu tượng có sẵn cũng chỉ được cập nhật sau khi nhấn Lưu. Ảnh được cắt thành
256 × 256 và lưu trong cơ sở dữ liệu, hiển thị ở sảnh, bạn bè, bảng xếp hạng và phòng chờ.

## Đóng gói và demo LAN

Tắt server đang chạy, rồi chạy `powershell -ExecutionPolicy Bypass -File .\scripts\build-demo.ps1`. Hai file ZIP được tạo trong `dist`: gói chạy Windows và gói mã nguồn. Hướng dẫn máy chủ, firewall và client nằm trong [demo/DEMO.md](demo/DEMO.md).
