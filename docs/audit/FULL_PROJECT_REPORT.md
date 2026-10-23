# Báo cáo kiểm toán toàn dự án — 23/10/2026

## 1. Tóm tắt

Dự án có ba module Maven: `common` (giao thức/DTO), `server` (Spring Boot 3.3.5, WebSocket, Spring Data JPA/Hibernate 6.5, H2 2.2.224), `fx-client` (JavaFX 21.0.5). POM yêu cầu Java 21; máy kiểm thử hiện dùng JDK 22. H2 nhúng mặc định, MySQL là profile tùy chọn chưa được kiểm chứng ở lượt này. Luồng: client → WebSocket `/ws/game` → `GameWebSocketHandler` → `GameService` và các service chuyên trách → game engine/room → repository/JPA → H2. Không có Node frontend; `node_modules/` trong workspace không thuộc build ứng dụng.

Code có chức năng tài khoản, hồ sơ/avatar, bạn bè/chat, phòng 3–8 người, trò chơi, tính điểm theo thời gian và reconnect 30 giây. Điểm yếu trước audit: seed demo công khai mặc định, tên `admin` tự được cấp ADMIN khi tạo User, profile H2 có `ddl-auto=create`, thiếu unique match/user và thiếu lịch sử migration. Đã sửa ba điểm đầu; các thay đổi ràng buộc có nguy cơ dữ liệu được giữ dưới dạng đề xuất.

## 2. Ma trận chức năng

Xem [FEATURE_AUDIT.md](FEATURE_AUDIT.md). Có bằng chứng đường chạy và test cho phòng, trò chơi, tính điểm. Khôi phục phiên chỉ lưu trong bộ nhớ; password recovery không thấy được triển khai. Không đưa tỷ lệ % hoàn thành vì chưa có danh sách acceptance có trọng số; 63 test không tương đương 63 yêu cầu.

## 3. Backend

`GameWebSocketHandler` nhận sự kiện, `GameService` định tuyến/quản lý kết nối, `AuthService` xử lý tài khoản, `FriendService` quan hệ xã hội, `RoomManager`/`Room` giữ phòng, `GameSession` điều khiển lượt, `ScoringService`/`GuessingWindow` chấm điểm. Repo có 8 entity JPA. Socket/đồng hồ và chốt điểm được kiểm tra trong các test game/recovery; bộ test không chứng minh khả năng scale nhiều server. Nhiều trạng thái trong RAM, `GameSession` lớn, Hibernate `update` thiếu versioning, `Friendship` chỉ unique cặp có hướng.

An ninh: mật khẩu đăng ký được BCrypt; đường đăng nhập vẫn chấp nhận mật khẩu bản ghi cũ dạng plaintext rồi nâng lên BCrypt. Mã cũ gán ADMIN dựa trên username `admin`; đã đổi User mới thành role USER và kiểm tra trùng username không phân biệt hoa/thường. `DataInitializer` nay chỉ chạy khi bật `GAME_DEMO_SEED_ENABLED=true`, test bật riêng. WebSocket origin có thể cấu hình `WS_ALLOWED_ORIGINS`; mặc định `*` chỉ hợp cho môi trường phát triển. Server không triển khai quy trình tạo quản trị viên an toàn cho database mới; quản trị cần provisioning có kiểm soát. Script `reset_passwords.sql` và lịch sử commit cần kiểm tra trước public và xoay vòng mọi tài khoản demo đã từng lộ. Không ghi mật khẩu thật vào báo cáo.

## 4. Frontend

JavaFX có màn đăng nhập/đăng ký, sảnh, phòng, game, profile/avatar, quản trị và overlay reconnect. Địa chỉ WebSocket do người dùng nhập hoặc JVM property `scribble.server` cung cấp. `GameScreen` hiển thị điểm Server trả về; avatar có crop/preview. UI là ứng dụng desktop nên không có frontend HTTP port, không có yêu cầu Docker cho GUI. Đã có JavaFX smoke check điểm từ phần việc trước; chưa kiểm tra thủ công toàn bộ kích cỡ màn hình hoặc mọi nhánh lỗi mạng ở lượt audit này.

## 5. Database và 3NF

H2 bản sao thực tế có 8 bảng: `users`, `friendships`, `chat_messages`, `matches`, `match_results`, `paintings`, `custom_topics`, `built_in_topic_overrides`. PK/FK và index chi tiết ở [SCHEMA.md](../database/SCHEMA.md), ERD ở [ERD.md](../database/ERD.md). Hai cột tính điểm mới được xác nhận qua log migration của server hoạt động, nhưng không đọc trực tiếp metadata của file DB đang bị khóa. MySQL chưa kiểm tra thực tế.

**Database có đạt 3NF không? UNVERIFIED — INSUFFICIENT EVIDENCE.** Các bảng dữ liệu nguyên tử, không có khóa ghép được cưỡng chế gây phụ thuộc bộ phận; nhiều bảng phù hợp 3NF với tập phụ thuộc đã quan sát. `match_results` thiếu unique `(match_id,user_id)`, friendship không cưỡng chế tính duy nhất của cặp vô hướng, và `users.ranking_score` là tổng dẫn xuất không có ledger đầy đủ để đối soát. Đây là các vấn đề ràng buộc/nhất quán, chưa đủ chứng minh một vi phạm 3NF hình thức. Không tuyên bố toàn database đạt 3NF. Xem [NORMALIZATION_3NF.md](../database/NORMALIZATION_3NF.md) và [DATABASE_AUDIT.md](../database/DATABASE_AUDIT.md). Không thêm unique có thể thất bại trên database hiện hành trước khi rà dữ liệu.

## 6. Chất lượng mã

Điểm tốt: tách common, server, client; nhiều integration test WebSocket; tính điểm có hàm thuần và test ranh giới; dùng repository tham số thay vì ghép SQL đầu vào. Rủi ro: `GameSession` và `GameService` ôm nhiều trách nhiệm, `loginAttempts` ở RAM tăng theo username nhập sai và reset sau restart, EAGER ở hai FK của Friendship/Chat có thể tăng số query, `ddl-auto=update` không có rollback. `DataInitializer` chứa account demo với mật khẩu biết trước, nhưng nay bị vô hiệu trừ khi phát triển bật rõ.

## 7. Git và khả năng triển khai

Nhánh lúc bắt đầu audit: `review/pr-3-without-chat-limit`; remote `origin` trỏ `https://github.com/vuhuy2901/BTL-LTM.git`. Đã tạo nhánh riêng `audit/clone-ready-2026-10-23` với commit mã nguồn `30a6435` và commit báo cáo clean checkout `f1d4fb5`. Dữ liệu DB, log và `node_modules` không nằm trong commit. Script reset mọi mật khẩu bằng một hash cố định đã xóa khỏi HEAD mới; file vẫn có thể lấy lại từ Git history và mật khẩu từng bị chia sẻ cần thay. `.gitignore` đã bổ sung DB/log/node_modules/.env, giữ `.env.example`. `.github/workflows/ci.yml` chạy `mvn -B verify` trên JDK21, chưa có kết quả CI từ GitHub. Docker Compose không được thêm vì JavaFX là desktop và H2 nhúng; setup Maven là cách hỗ trợ.

**Push bị chặn bởi hệ thống duyệt tự động:** lệnh đẩy toàn bộ payload lên remote chưa có xác nhận đủ tin cậy cho chính đích/payload này. Không thử lệnh thay thế để vượt chặn. Do đó nhánh audit chỉ ở local, chưa có PR/merge và người dùng `git pull` từ `main` chưa nhận thay đổi. Cần người dùng chấp thuận rõ việc gửi nhánh này tới remote đã nêu; sau đó chạy `git push -u origin audit/clone-ready-2026-10-23`, tạo PR và xác minh CI/merge.

## 8. Kết quả kiểm thử

- `mvn.cmd -B test` (giảm logging Spring/Hibernate): **PASS**, 63 test server, 0 fail/error/skip gồm `UserRoleTest`; log `target/full-audit-final-tests.log`.
- Đọc schema H2 2.2.224 từ bản sao offline: **PASS**, 8 bảng/38 cột trước hai cột migration; 0 cặp match/user trùng trong bản sao. DB live/MySQL: **NOT VERIFIED** trực tiếp.
- Checkout sạch `target/clean-clone-check` từ commit `30a6435`: `mvn.cmd -B verify` **PASS**, 63 test và cả ba module build thành công; log `target/clean-clone-check/clean-check.log`. Đây là checkout riêng nhưng dùng Maven cache của máy, chưa phải máy mới hoàn toàn.
- Từ checkout sạch, khởi động JAR server cổng 8091 với DB H2 disposable riêng: **PASS**, WebSocket bắt tay HTTP 101; sau khi dừng server, DB có 8 bảng và 0 tài khoản, chứng minh không cần DB cũ và seed mặc định tắt. Đã dừng đúng tiến trình server thử nghiệm. Trận multiplayer/reconnect qua WebSocket được `GameIntegrationTest` và `SessionRecoveryTest` kiểm tra trong bộ `verify`; chưa chơi thủ công 3 client trên checkout sạch.
- `mvn.cmd -B -DskipTests package` ở workspace gốc: **PASS**. Đã xác nhận cổng 8080 không có client, dừng đúng JAR cũ, sao lưu DB đã đóng tại `server/target/game_db.before-audit-restart.mv.db` (cùng kích cỡ bản gốc), rồi bật JAR mới. Server báo `ACCEPTING_TRAFFIC`, WebSocket trả HTTP 101 tại cổng 8080. DB cũ không bị xóa hoặc reset; tài khoản demo cũ vẫn cần đổi mật khẩu.
- GitHub push: **BLOCKED bởi auto-review**, không phải lỗi mạng hay xác thực. CI/clone từ remote: **NOT RUN** vì nhánh chưa được đẩy.

## 9. Rủi ro

| Mức | Vấn đề | Tác động / hướng giải quyết |
|---|---|
| P0 | Tài khoản demo cũ đã nằm trong DB hoặc commit history | thay mật khẩu, kiểm tra lịch sử trước public; code mới không tự seed mặc định |
| P0 | quyền admin từ username trong bản cũ | đã sửa; phải triển khai JAR mới và kiểm soát tài khoản admin hiện hữu |
| P1 | profile H2 cũ tạo lại schema | đã chuyển `update`; vẫn phải backup và thêm migration versioned trước production |
| P1 | thiếu unique match/user | có thể có kết quả trùng; đối soát DB hiện hành trước ràng buộc |
| P2 | không lưu trận/reconnect qua restart | thiết kế lưu trạng thái nếu cần HA |
| P2 | cần CI và clone từ remote xác minh | checkout sạch cục bộ đã PASS; tiếp theo chạy Actions và clone remote sau push |
| P3 | query/index và kích cỡ `GameSession` | đo tải và chia nhỏ theo luồng nghiệp vụ |

## 10. Lộ trình

Ngay: build lại/deploy sửa quyền, đổi mật khẩu các tài khoản demo cũ; dùng seed chỉ trong test. Trước bàn giao GitHub: rà toàn bộ diff, kết quả clean checkout, xác minh CI, chuẩn hóa script PowerShell. Trước triển khai: migration có version, snapshot/rollback, TLS, WS origins cụ thể, cung cấp quản trị viên an toàn. Sau đó: ledger điểm và lưu session/trận nhiều tiến trình.
