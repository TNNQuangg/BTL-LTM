# Bàn giao môi trường phát triển độc lập

Ngày kiểm tra: 23/10/2026. Mã nguồn nằm trên nhánh `feature/independent-local-dev` (commit cấu hình `3ec3d25`, commit báo cáo/test `21b5d8b`), tạo từ `main` đã được đẩy trước đó. Không sao chép hoặc thay đổi database đang dùng của chủ dự án. Môi trường thử nghiệm là checkout riêng ở `target/independent-clone-check`, cổng 8092 và file H2 riêng trong `server/data` của checkout đó.

| Câu hỏi | Kết luận và bằng chứng |
|---|---|
| 1. Mỗi thành viên chạy Backend riêng? | **Có.** `scripts/start-local-server.ps1` build Maven và bật Spring Boot trên cổng local; thử thực tế tại 8092. Mặc định `SERVER_ADDRESS=127.0.0.1`, cổng 8080; phòng/phiên nằm trong bộ nhớ từng Server. |
| 2. DB riêng? | **Có.** Script đặt `DB_URL` tuyệt đối tới `server/data/game_db` của chính clone; không lấy DB từ máy khác. Có thể đặt đường dẫn H2 khác hoặc dùng profile MySQL với `DB_URL`/`DB_USER`/`DB_PASSWORD`. |
| 3. DB trắng tự khởi tạo? | **Có với H2.** Checkout sạch không có file DB; khi chạy script H2 tạo 8 bảng. Trước đăng ký không có user demo; tài khoản được tạo qua client. `TopicPool` chứa từ khóa tích hợp trong source và `GameService` nạp khi khởi động. |
| 4. JavaFX chạy local? | **Có với môi trường Windows/JDK hiện tại.** `mvn verify` build client; chạy `TimeScoringUiCheck` bằng JavaFX 21.0.5 thực tế: PASS. Chưa thao tác thủ công toàn bộ màn hình Client trong lượt này. |
| 5. Client trỏ Backend của mình? | **Có.** `MainApplication` mặc định `ws://localhost:8080/ws/game`, có ô sửa địa chỉ/JVM property. WebSocket client thật kết nối server test tại localhost:8092 sau khi nhập URL. |
| 6. Mọi module Maven build? | **Có.** `mvn.cmd -B verify` trong checkout sạch: common, server, fx-client đều BUILD SUCCESS. |
| 7. Kiểm thử qua? | **Có.** 63 JUnit tests, 0 thất bại/lỗi/skip trong checkout sạch, gồm game flow, reconnect, tính điểm và persistence. WebSocket client test riêng: đăng ký, đăng nhập, tạo phòng thành công. |
| 8. Schema được xác minh 3NF? | **Chưa thể kết luận toàn bộ.** Đã đối chiếu 8 entity và H2 thực; nhiều bảng phù hợp với phụ thuộc đã quan sát, nhưng thiếu bằng chứng về toàn bộ phụ thuộc nghiệp vụ, MySQL và `match_results` chưa có unique cặp match/user. Xem `docs/database/NORMALIZATION_3NF.md`. |
| 9. DB runtime bị Git loại? | **Có.** `.gitignore` loại `/server/data/`, `/data/`, `*.mv.db`, `*.trace.db`, log và `target/`; checkout sạch không chứa DB. |
| 10. Người khác theo README chạy được? | **Đã kiểm chứng trên checkout sạch Windows.** Script README tạo H2 và mở server 8092; client WebSocket đăng ký/đăng nhập/tạo phòng. JavaFX QA PASS. Cần JDK21+, Maven và Git; thử nghiệm trên máy mới hoàn toàn không dùng cache Maven chưa thực hiện. |
| 11. Còn gì chưa xác minh? | **MySQL thực, GUI thao tác thủ công đầy đủ, nhiều máy vật lý, CI GitHub của nhánh mới và tính 3NF toàn cục.** Script `start-local-server.ps1` chỉ được thử trên Windows PowerShell. |
| 12. Nhánh đã xuất bản? | **Có.** GitHub đã nhận `feature/independent-local-dev` ở commit `21b5d8b` và đặt upstream `origin/feature/independent-local-dev`. Chưa tạo PR/merge vào `main`; liên kết tạo PR: `https://github.com/vuhuy2901/BTL-LTM/pull/new/feature/independent-local-dev`. |

## Kiểm tra chi tiết

- `mvn.cmd -B verify` trên checkout sạch từ commit `3ec3d25`: 63/63 tests, BUILD SUCCESS. Bản build trong workspace gốc có 63 test đạt nhưng `repackage` thất bại vì JAR của server cổng 8080 đang chạy bị Windows giữ khóa; đây là lỗi thao tác build trên tiến trình đang chạy, không tái hiện ở checkout sạch.
- `powershell -ExecutionPolicy Bypass -File .\scripts\start-local-server.ps1` trong checkout sạch với `SERVER_PORT=8092`: tự tạo `server/data/game_db.mv.db`, Server `ACCEPTING_TRAFFIC` và JDBC dùng đúng đường dẫn clone. H2 có 8 bảng, không có tài khoản demo trước khi test.
- WebSocket client `LiveServerTest`: đăng ký tài khoản thử, đăng nhập, nhận `ROOM_RESPONSE` success khi tạo phòng. Sau khi dừng/chạy lại cùng script, đăng nhập lại **tài khoản cũ** và tạo phòng thành công. H2 vẫn có 8 bảng/1 tài khoản sau lần dừng thứ hai. Đã dừng server test 8092.
- JavaFX `TimeScoringUiCheck`: PASS. Trận 3 người, reconnect và scoring còn được bao phủ bởi JUnit game/WebSocket; không gọi đó là buổi chơi GUI thủ công.
- Server chủ dự án tại cổng 8080 không bị dừng hoặc đổi DB trong thử nghiệm này.

## Cấu hình và quy trình nhóm

`README.md` có mục “Phát triển độc lập trên máy cá nhân” với lệnh PowerShell, đường dẫn DB, chọn H2/MySQL, test, xử lý lỗi và Git branch/PR. `.env.example` có giá trị mẫu, không được tự load hoặc chứa bí mật thật. Profile `h2` nhận biến `DB_URL`/`DB_USER`/`DB_PASSWORD` như cấu hình mặc định; `SERVER_ADDRESS` mặc định loopback. Gói demo LAN đặt `SERVER_ADDRESS=0.0.0.0` tường minh để giữ chức năng chơi LAN.

Migration hiện dựa vào Hibernate `ddl-auto=update` cho thay đổi cộng thêm; file `docs/database/LOCAL_MIGRATIONS.md` mô tả backup, review và script theo version khi cần đổi dữ liệu/ràng buộc. Không có migration phá hủy nào chạy trên database chủ dự án. Luật chơi 3–8 người và điểm thời gian giữ nguyên; không sửa `game_rules_nhom8.md` vì không có rule nào thay đổi.
