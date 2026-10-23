# Kiểm toán database và kế hoạch cải thiện

Đã đọc entity, cấu hình và metadata H2 2.2.224 từ bản sao DB trước cập nhật điểm. Có 8 bảng, 22 ràng buộc PK/FK/unique; FK của chat, friendship, painting, match_result đều hiện diện. Bản sao có 87 users, 17 matches, 8 match_results, 37 paintings. Không thấy cặp match/user trùng, friendship đối xứng trùng hoặc self-link trong bản sao; database đang chạy có thể thay đổi sau thời điểm sao lưu. Hai cột điểm mới được Hibernate `update` thêm theo log server. MySQL chưa chạy kiểm tra thực tế.

| Bảng | Vấn đề / phụ thuộc | Chuẩn / constraint | Sửa đề xuất | Tác động migration | Rủi ro | Tự sửa? |
|---|---|---|---|---|---|---|
| `match_results` | nghiệp vụ dự kiến `(match_id,user_id) → points_*` nhưng DB chỉ cưỡng chế `result_id` | thiếu unique, nguy cơ cộng điểm trùng; không đủ chứng cứ để tuyên bố lỗi 3NF hình thức | rà toàn bộ dữ liệu hiện hành, giải quyết trùng có chủ đích, thêm UNIQUE `(match_id,user_id)` bằng migration đánh số | có thể thất bại nếu có trùng | cao | chưa |
| `friendships` | quan hệ bạn bè không hướng, schema chỉ unique cặp có hướng | có thể tồn tại A→B và B→A; thiếu CHECK sender != receiver | chuẩn hóa cặp bằng lower/higher ID hoặc ứng dụng đặt khóa, sau đó thêm ràng buộc | hợp nhất trạng thái hai hướng cần quyết định nghiệp vụ | cao | chưa |
| `users` | `ranking_score` là tổng cộng dồn, không chỉ phụ thuộc vào `match_results` vì điểm rời/hủy | denormalization có chủ đích; thiếu sổ điểm đầy đủ để tính lại | đối soát tổng, thêm ledger bất biến trong phiên bản sau | không được viết lại lịch sử | cao | chưa |
| `matches` | `room_id` tham chiếu phòng trong RAM | không thể FK; lịch sử vẫn đọc được | giữ nguyên, tài liệu hóa | không | thấp | đã ghi tài liệu |
| `paintings` | `painter_id` không được DB buộc thuộc trận | toàn vẹn nghiệp vụ | validate tại dịch vụ, cân nhắc FK ghép khi có khóa ứng viên kết quả | cần dữ liệu đầy đủ | vừa | chưa |
| `chat_messages` | chỉ có index FK đơn lẻ | truy vấn hội thoại có thể chậm | index `(sender_id,receiver_id,created_at)` nếu đo tải xác nhận | index thêm, không sửa dữ liệu | thấp | sau đo tải |
| `custom_topics`, `built_in_topic_overrides` | không thấy vấn đề chuẩn hóa | khóa unique hoạt động | giữ nguyên | không | thấp | không cần |

**Schema drift:** bản sao cũ thiếu `scoring_version`, `points_adjustment`, trong khi log server mới xác nhận Hibernate thêm chúng. Bản sao không phải ảnh tức thời của DB hiện hành. `application-h2.properties` trước đây dùng `ddl-auto=create`, đã đổi sang `update` để tránh xóa bảng khi vô tình bật profile. Cơ chế `update` không quản lý phiên bản migration hay rollback; trước triển khai thực cần snapshot và migration đánh số. Không xóa/reset dữ liệu trong lượt audit này.

Xem [SCHEMA.md](SCHEMA.md), [NORMALIZATION_3NF.md](NORMALIZATION_3NF.md), [ERD.md](ERD.md).
