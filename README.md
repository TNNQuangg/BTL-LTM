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

## Phát triển độc lập trên máy cá nhân

Mỗi thành viên clone mã nguồn và chạy một Backend, một JavaFX Client, một database riêng **trên chính máy mình**. Không sao chép `*.mv.db`, tài khoản hoặc kết quả trận qua Git. Mặc định Backend chỉ chạy local trên máy mình tại `127.0.0.1:8080`; Client kết nối bằng `ws://localhost:8080/ws/game`. Để demo qua LAN, người chủ động đặt `SERVER_ADDRESS=0.0.0.0` và nhập IP máy chủ vào Client.

Trên Windows/VS Code, cài JDK 21 trở lên, Maven 3.9 trở lên và Git. Không cần Node.js, Docker hay MySQL nếu dùng H2. Trong PowerShell:

```powershell
java -version
mvn.cmd -version
git clone https://github.com/vuhuy2901/BTL-LTM.git
Set-Location BTL-LTM
powershell -ExecutionPolicy Bypass -File .\scripts\start-local-server.ps1
```

Script build ba module, tạo `server/data/` riêng của clone, đặt `DB_URL` đến H2 ở đó rồi bật Backend. Hãy giữ terminal này mở. H2 tự tạo 8 bảng bằng JPA/Hibernate `ddl-auto=update`; server nạp từ khóa mặc định từ mã nguồn, không cần nhập SQL hay tải DB của ai. Không tạo tài khoản admin/demo mặc định. Database và tài khoản của bạn còn nguyên khi dừng/chạy lại Backend. `.env.example` là tài liệu tham khảo biến môi trường, không được tự nạp; nếu muốn đổi cổng hoặc DB, đặt `$env:SERVER_PORT='8081'` hoặc `$env:DB_URL='jdbc:h2:file:C:/duong-dan-rieng/game_db'` **trước** khi chạy script. Mỗi Backend chỉ mở một file H2 tại một thời điểm.

Mở terminal PowerShell thứ hai ở thư mục clone:

```powershell
mvn.cmd -pl fx-client javafx:run
```

Chọn địa chỉ mặc định `localhost:8080`, đăng ký tài khoản local, rồi đăng nhập. Nếu dùng cổng khác, sửa ô địa chỉ Server trong Client. Có thể kiểm tra Backend bằng `Test-NetConnection localhost -Port 8080`; WebSocket ở `/ws/game`. Mở 3 Client, tạo/cho vào cùng phòng, cả 3 sẵn sàng rồi bắt đầu game. Luật là 3–8 người, chọn từ 10 giây, vẽ mặc định 60 giây và đoán 30 giây.

MySQL là tùy chọn: tự tạo MySQL ở máy mình, đặt `$env:DB_URL`, `$env:DB_USER`, `$env:DB_PASSWORD`, rồi chạy `mvn.cmd -B -DskipTests install` và `mvn.cmd -pl server -Dspring-boot.run.profiles=mysql spring-boot:run`. Chỉ dùng profile `mysql` khi database MySQL đã sẵn sàng. Profile `h2` cũng nhận `DB_URL`/`DB_USER`/`DB_PASSWORD`; không dùng `ddl-auto=create` trên DB cần giữ. Chưa kiểm tra MySQL thực tế trong lượt bàn giao này.

Sau `git pull origin main`, đọc [quy trình migration](docs/database/LOCAL_MIGRATIONS.md), sao lưu DB của riêng mình và chạy `mvn.cmd -B verify`. Thay đổi source theo nhánh `git switch -c feature/ten-tinh-nang`, commit và push nhánh đó, tạo Pull Request; sau review/merge, thành viên khác pull `main`. Git chỉ đồng bộ source/config/test, không đồng bộ dữ liệu local. Không force-push.

Nếu cổng 8080 bị chiếm, chỉ để một Backend chạy hoặc đổi `SERVER_PORT` và ô địa chỉ Client. Nếu H2 báo khóa file, kiểm tra không có hai Server dùng cùng `DB_URL`. Nếu Maven báo thiếu `common`, chạy `mvn.cmd -B -DskipTests install` ở root. Nếu Client báo không kết nối được, bật Backend trước và kiểm tra `Test-NetConnection`. Nếu gõ đuôi `.ps14`, sửa thành `.ps1`. File `.env` và database được `.gitignore` loại khỏi Git.

## Ảnh đại diện

Trong **Trang cá nhân**, chọn **Tải ảnh từ máy…** (PNG/JPG, tối đa 10 MB).
Kéo ảnh trong khung tròn, dùng thanh trượt hoặc cuộn chuột để thu/phóng và xem trước.
Nhấn **Lưu ảnh đại diện** để lưu; **Hủy thay đổi** khôi phục ảnh trước đó.
Các biểu tượng có sẵn cũng chỉ được cập nhật sau khi nhấn Lưu. Ảnh được cắt thành
256 × 256 và lưu trong cơ sở dữ liệu, hiển thị ở sảnh, bạn bè, bảng xếp hạng và phòng chờ.

## Đóng gói và demo LAN

Tắt server đang chạy, rồi chạy `powershell -ExecutionPolicy Bypass -File .\scripts\build-demo.ps1`. Hai file ZIP được tạo trong `dist`: gói chạy Windows và gói mã nguồn. Hướng dẫn máy chủ, firewall và client nằm trong [demo/DEMO.md](demo/DEMO.md).
