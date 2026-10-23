# Lược đồ dữ liệu đã quan sát

Nguồn: bản sao H2 `server/target/game_db.before-time-scoring-v2.mv.db` (đọc qua H2 2.2.224), đối chiếu 8 JPA entity và log migration của lần chạy server mới ngày 23/10/2026. Bản sao có 8 bảng, 38 cột. Bản sao được tạo trước khi thêm `scoring_version` và `points_adjustment`; log khởi động server xác nhận hai cột đã được thêm vào database hoạt động. Không thể tuyên bố đã đọc trực tiếp metadata của database đang bị tiến trình server giữ khóa.

| Bảng | PK | Cột dữ liệu chính | FK thực tế | Unique |
|---|---|---|---|---|
| `users` | `user_id` | `username`, `password`, `display_name`, `ranking_score`, `role`, `avatar`, `created_at` | — | `username`, `display_name` |
| `friendships` | `friendship_id` | `sender_id`, `receiver_id`, `status`, `created_at` | `sender_id`, `receiver_id` → users | cặp có hướng `(sender_id,receiver_id)` |
| `chat_messages` | `message_id` | `sender_id`, `receiver_id`, `content`, `is_read`, `created_at` | cả hai → users | — |
| `matches` | `match_id` | `room_id`, `started_at`, `ended_at`, `scoring_version`* | — | — |
| `match_results` | `result_id` | `match_id`, `user_id`, `points_drawn`, `points_guess`, `points_adjustment`* | matches, users | thiếu unique `(match_id,user_id)` |
| `paintings` | `painting_id` | `match_id`, `painter_id`, `topic`, `stroke_data` | matches, users | — |
| `custom_topics` | `id` | `word` | — | `word` |
| `built_in_topic_overrides` | `id` | `original_word`, `replacement_word` | — | `original_word` |

`*` Hai cột mới có giá trị mặc định tương ứng 1 và 0. Các FK có chỉ mục H2 tạo tự động. `room_id` là mã phòng đã tồn tại trong bộ nhớ lúc chơi, không có bảng rooms. Kết nối, bản nháp, lượt đoán và lần thử đoán lưu trong bộ nhớ, không phải bảng SQL. `stroke_data` là một payload tranh được xử lý như một giá trị, không phải nhóm cột lặp.

Schema được tạo/thêm bởi Hibernate `spring.jpa.hibernate.ddl-auto=update`, không có migration được đánh số. `application-h2.properties` cũ dùng `ddl-auto=create`: **không dùng profile này trên database cần giữ**, vì có nguy cơ mất dữ liệu. MySQL là profile tùy chọn, chưa xác minh schema vật lý của MySQL.
