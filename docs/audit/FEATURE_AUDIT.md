# Ma trận kiểm toán chức năng

Trạng thái COMPLETE chỉ dùng khi có đường thực thi và kiểm thử liên quan; PARTIALLY IMPLEMENTED khi tính năng chạy nhưng còn giới hạn/rủi ro. Phần trăm hoàn thành không đưa ra vì chưa có tập yêu cầu nghiệm thu có trọng số hoặc kiểm tra thủ công mọi màn hình; lấy số test làm % chức năng sẽ sai.

| Module | Feature | Status | Backend | Frontend | Database | Issues | Priority |
|---|---|---|---|---|---|---|---|
| Auth | đăng ký/đăng nhập/đăng xuất | PARTIALLY IMPLEMENTED | `AuthService`, `GameService` | `MainApplication` | `users` | đã sửa việc tự đăng ký tên admin được cấp ADMIN; plaintext legacy login vẫn được nâng bcrypt | P0 |
| Auth | lấy lại mật khẩu | MISSING | không thấy quy trình khôi phục | không thấy màn hình | — | cần thiết kế xác minh sở hữu | P2 |
| Profile | avatar/thống kê/điểm hạng | PARTIALLY IMPLEMENTED | `AuthService`, `LeaderboardService` | `ProfileDialog`, `AvatarEditor` | users, match_results | ranking_score là cache không có ledger đầy đủ | P2 |
| Social | kết bạn/chấp nhận/từ chối/chat | PARTIALLY IMPLEMENTED | `FriendService`, `GameService` | `LobbyScreen` | friendships, chat_messages | thiếu unique cặp bạn không hướng | P2 |
| Room | tạo/vào/rời/ready/chủ phòng | COMPLETE | `Room`, `RoomManager` | `RoomScreen`, `LobbyScreen` | RAM | test RoomPlayerLimitsTest, SessionRecoveryTest; giới hạn thực 3–8 | P2 |
| Game | chọn từ/vẽ/đoán/chuyển lượt/kết thúc | COMPLETE | `GameSession` | `GameScreen` | matches, paintings, match_results | mặc định chọn 10s, vẽ 60s, đoán 30s | P2 |
| Drawing | nét vẽ/nộp/nháp/bỏ qua | PARTIALLY IMPLEMENTED | `GameSession` | `GameScreen` | paintings và RAM | khôi phục chỉ khi tiến trình còn sống | P2 |
| Scoring | thời gian/điểm vẽ/phạt rời | COMPLETE | `ScoringService`, `GuessingWindow`, `GameSession` | `GameScreen` | users, matches, match_results | 15+11+2 kiểm thử tính điểm/lưu trữ | P2 |
| Recovery | heartbeat/30s/reconnect | PARTIALLY IMPLEMENTED | `GameService`, `ClientSession` | `ReconnectOverlay` | RAM | 26 test; server restart mất trận đang chạy | P2 |
| Admin | topic/quản trị | PARTIALLY IMPLEMENTED | `GameService` | `AdminScreen` | custom_topics, overrides | demo admin cũ trong DB cần đổi mật khẩu | P0 |

Nguồn code được nêu theo lớp; kết quả test mới nhất xem `FULL_PROJECT_REPORT.md`. Không gọi một nút UI là chức năng hoàn thành nếu không có backend; các phần được đánh COMPLETE ở đây đã có test chạy qua đường game hoặc dịch vụ liên quan. Đối chiếu luật tại `game_rules_nhom8.md`, mục 4.
