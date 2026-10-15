# HƯỚNG DẪN KHỞI CHẠY DỰ ÁN VẼ HÌNH ĐOÁN Ý (NHÓM 8)

Dự án được xây dựng theo kiến trúc Client-Server hiện đại trên nền tảng **Java 21** với mô hình Multi-Module Maven:
- **`common`**: Chứa DTO, giao thức thông điệp Envelope và MessageType dùng chung.
- **`server`**: Backend Spring Boot 3.3, Spring WebSocket, Spring Data JPA / Hibernate (Hỗ trợ cả H2 Database nhúng tự động và MySQL).
- **`fx-client`**: Desktop Frontend JavaFX 21 phong cách nét vẽ tay Sketch (Doodle nghệ thuật), tương tác thời gian thực qua WebSocket.

---

## 1. Yêu cầu môi trường (Prerequisites)

1. **Java Development Kit (JDK)**: Phiên bản **Java 21 trở lên**.
   - Kiểm tra bằng lệnh: `java -version`
2. **Apache Maven**: Phiên bản **3.8+**.
   - Kiểm tra bằng lệnh: `mvn -v`

> [!TIP]
> **Không bắt buộc cài đặt MySQL!** Hệ thống đã tích hợp sẵn cơ sở dữ liệu nhúng **H2 Database** lưu trữ bền vững tại thư mục `./data/game_db.mv.db`. Bạn có thể khởi chạy và trải nghiệm ngay lập tức.

---

## 2. Bước 1: Khởi chạy Backend Server (Spring Boot)

### Cách 1: Chạy bằng dòng lệnh (Khuyến nghị - Cắm là chạy ngay 100%)
Mở một **Terminal / PowerShell** tại thư mục gốc dự án (`BTL LTM`) và chạy:

```bash
mvn -pl server spring-boot:run
```
*(Hệ thống sẽ tự động khởi động cơ sở dữ liệu H2, tự sinh schema và lắng nghe WebSocket tại cổng 8080).*

---

### Cách 2: Chạy trực tiếp trong IDE (IntelliJ IDEA / VS Code / Eclipse)
1. Mở file: `server/src/main/java/com/nhom8/server/Application.java`
2. Nhấp chuột phải vào file $\rightarrow$ Chọn **Run 'Application.main()'** (hoặc bấm vào biểu tượng tam giác xanh ▶️ bên cạnh hàm `public static void main`).
   *(Lưu ý: Không gõ đường dẫn file trực tiếp vào terminal PowerShell).*

---

### Cách 3: Sử dụng MySQL (Tùy chọn nâng cao)
Nếu bạn muốn sử dụng MySQL Server cục bộ (XAMPP / Docker / MySQL Workbench):
1. Tạo Database:
   ```sql
   CREATE DATABASE IF NOT EXISTS scribble_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Cấu hình mật khẩu MySQL của máy bạn tại file `server/src/main/resources/application-mysql.properties`.
3. Chạy Server với profile `mysql`:
   ```bash
   mvn -pl server spring-boot:run "-Dspring-boot.run.profiles=mysql"
   ```

---

Khi Server khởi động thành công, màn hình sẽ hiển thị:
```
[SERVER] ================================================
[SERVER]   Vẽ Hình Đoán Ý - Spring Boot Server (Nhóm 8)
[SERVER]   Đang lắng nghe tại cổng: 8080
[SERVER]   WebSocket endpoint: ws://localhost:8080/ws/game
[SERVER]   Chờ kết nối từ Client JavaFX...
[SERVER] ================================================
```

---

## 3. Bước 2: Khởi chạy Client Desktop (JavaFX)

Mở một **cửa sổ Terminal / PowerShell mới** tại thư mục gốc và chạy:

```bash
mvn -pl fx-client javafx:run
```

Nếu Maven báo thiếu thông điệp mới trong module `common` sau khi cập nhật mã nguồn, chạy một lần `mvn -pl common install -DskipTests`, rồi mở lại client.

*Hoặc trong IDE:*
- Nhấp chuột phải vào file `fx-client/src/main/java/com/nhom8/client/Launcher.java` $\rightarrow$ Chọn **Run 'Launcher.main()'**.

Cửa sổ giao diện đăng nhập với phong cách nét vẽ tay Doodle (sắc vàng `#F5C800`, nút bấm hình cây bút chì, font Amatic SC & Patrick Hand) sẽ xuất hiện.

### 👤 Danh sách tài khoản thử nghiệm có sẵn (Mật khẩu: `123456`):
Tất cả tài khoản dưới đây đã được khởi tạo và đồng bộ sẵn với mật khẩu **`123456`**:

| Tên đăng nhập | Mật khẩu | Tên hiển thị | Vai trò |
|---|---|---|---|
| `admin` | `123456` | Quản Trị Viên | ADMIN |
| `123456` | `123456` | vuhuy | USER |
| `vuhuy` | `123456` | Vũ Huy | USER |
| `playerone` | `123456` | PlayerOne | USER |
| `testuser` | `123456` | Test User | USER |
| `player1` | `123456` | Người Chơi 1 | USER |
| `player2` | `123456` | Người Chơi 2 | USER |
| `player3` | `123456` | Người Chơi 3 | USER |

*(Bạn cũng có thể chuyển sang tab **Đăng ký tài khoản** để tạo thêm tài khoản mới bất kỳ lúc nào)*

---

## 4. Hướng dẫn Test nhiều người chơi (Multi-player)

Luật chơi hỗ trợ từ **3 đến 6 người chơi** trong cùng một phòng:

1. **Mở thêm màn hình người chơi:**
   - Mở thêm các cửa sổ Terminal mới và chạy lại lệnh:
     ```bash
     mvn -pl fx-client javafx:run
     ```
   - Mỗi cửa sổ hoạt động như một Client độc lập.

2. **Quy trình trải nghiệm:**
   - Đăng ký tài khoản (ví dụ: `player1`, `player2`, `player3`).
   - Người chơi 1 bấm **➕ Tạo phòng mới** (nhận mã phòng gồm 6 số, ví dụ `516979`).
   - Người chơi 2 và 3 bấm vào phòng trong sảnh chờ hoặc nhập mã phòng để vào.
   - Khi tất cả người chơi bấm **Sẵn sàng**, Chủ phòng sẽ bắt đầu trận đấu!

3. **Các giai đoạn trong trận đấu:**
   - **Chọn chủ đề (10s):** Người vẽ chọn 1 trong 3 từ khóa ngẫu nhiên.
   - **Vẽ tranh (60s):** Người vẽ vẽ trên canvas (bút vẽ, bảng màu, tẩy, hoàn tác, xóa bảng). Nét vẽ đồng bộ tức thì qua WebSocket tới mọi người đoán.
   - **Đoán từ & Tính điểm:** Người đoán nhập đáp án vào khung chat. Đoán đúng nhận điểm thưởng và chuyển lượt vẽ tiếp theo.

---

## 5. Chạy Bộ Kiểm Thử Tự Động Toàn Diện (Continuous Stress Test)

Để kiểm tra tự động toàn bộ 5 kịch bản (Xác thực tiếng Việt, Đa phòng đồng thời, Mô phỏng 3 Bot thi đấu, Chịu tải dồn dập, Chuyển quyền Host khi rời mạng):

```bash
mvn -pl fx-client exec:java "-Dexec.mainClass=com.nhom8.client.ContinuousStressTest" "-Dexec.classpathScope=test"
```
Kết quả đạt chuẩn: **5 / 5 Suites PASS (100%)**.

### Quản trị và công cụ vẽ

- Đăng nhập bằng tài khoản `admin`, chọn **Quản trị** tại sảnh. Các tab hiển thị số tài khoản, người trực tuyến, phòng đang mở, trận và tranh đã lưu; danh sách người chơi; phòng chơi; từ khóa tự thêm.
- Có thể thêm hoặc xóa từ khóa. Dữ liệu được lưu trong bảng `custom_topics` và được dùng từ trận tiếp theo. Có thể giải tán phòng theo mã phòng.
- Trong pha vẽ ở client JavaFX, nút **Tẩy** chuyển sang nét trắng với đầu tẩy lớn hơn. Bấm **Bút vẽ** để quay lại. Nút **Xóa bảng** vẫn xóa toàn bộ tranh.
- Kiểm thử server, gồm ba client chơi một trận và kiểm tra quyền admin: chạy `mvn test` tại thư mục gốc.

---

## 6. Trải nghiệm Hệ thống Bạn bè & Chat Phòng Cô Lập

### 👥 A. Nhắn tin riêng cho bạn bè (Private 1-1 Chat ở sảnh)
1. **Giao diện Cột Phải tại Sảnh Chờ (Lobby):**
   - **Tab 👥 Bạn bè:** Danh sách bạn bè kèm trạng thái Online 🟢 / Offline ⚫, nút **💬 Chat** mở cửa sổ nhắn tin riêng Doodle, nút **➕ Thêm bạn**, click chuột phải để Hủy kết bạn.
   - **Tab 🌐 Online:** Xem toàn bộ người chơi đang online trong sảnh, bấm **💬 Chat** ngay lập tức hoặc gửi lời mời kết bạn.
   - **Tab 🔔 Lời mời:** Xem danh sách lời mời kết bạn gửi đến, nút **✔ Đồng ý** hoặc **✖ Từ chối**.
2. **Cửa sổ Chat 1-1:**
   - Mở cửa sổ vẽ tay Doodle chuyên biệt, tự động nạp **lịch sử trò chuyện** từ cơ sở dữ liệu.
   - Gửi và nhận tin nhắn tức thì bằng WebSocket. Tự động bật lên khi bạn bè gửi tin nhắn đến.

### 🔒 B. Chat nội bộ phòng chơi (Room Chat Isolation)
1. **Cô lập hoàn toàn:**
   - Khung chat trong phòng (`RoomScreen` và `GameScreen`) mang nhãn `💬 CHAT NỘI BỘ PHÒNG #[mã_phòng]` kèm biểu tượng `🔒 Chỉ các thành viên trong phòng này mới nhận được tin nhắn`.
   - Mọi tin nhắn trong phòng chỉ được gửi (`broadcast`) tới những người chơi đang cùng ở trong phòng đó. Người ở ngoài sảnh chờ hoặc ở các phòng khác hoàn toàn không thể thấy nội dung này.
2. **Đoán từ trong trận:**
   - Trong lúc trận đấu diễn ra, khung chat kiêm luôn chức năng đoán từ. Người đoán đúng sẽ nhận điểm thưởng và hệ thống ghi nhận kết quả.

### 🧪 C. Lệnh kiểm thử tự động Chat & Bạn bè:
```bash
mvn -pl fx-client exec:java "-Dexec.mainClass=com.nhom8.client.SocialAndChatValidationTest" "-Dexec.classpathScope=test"
```
Kiểm tra tự động 3 hạng mục:
- `PASS [TEST 1]`: Gửi/nhận tin nhắn riêng 1-1 và tải lịch sử chat từ cơ sở dữ liệu.
- `PASS [TEST 2]`: Chat phòng chơi cách ly 100%, người ở sảnh không nhận được tin nhắn nội bộ phòng.
- `PASS [TEST 3]`: Đồng bộ danh sách bạn bè & xử lý lời mời kết bạn.
