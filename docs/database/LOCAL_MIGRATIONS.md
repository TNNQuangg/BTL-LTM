# Database riêng của từng thành viên và quy trình thay đổi schema

Mặc định `scripts/start-local-server.ps1` dùng H2 tại `server/data/game_db.mv.db` **trong clone của người chạy**. Thư mục này bị Git bỏ qua. `application.properties` và profile `h2` dùng `ddl-auto=update`: database mới tạo bảng/cột theo entity; lần chạy sau giữ bản ghi. `update` không có thứ tự migration/rollback, không xử lý đáng tin cậy đổi tên cột, siết NOT NULL, thêm unique khi dữ liệu cũ bị trùng, hoặc biến đổi dữ liệu. Profile MySQL có cùng giới hạn và chưa được kiểm chứng tại môi trường này.

Khi sửa schema sau này:

1. Kiểm tra entity và DB thực; ghi SQL mới có version tăng dần vào `docs/database/migrations/` cùng hướng dẫn tương thích dữ liệu cũ. Review phụ thuộc, khóa và số liệu trước khi merge.
2. Mỗi thành viên dừng Backend và sao lưu **database của mình**. Với H2 file, chỉ sao chép `server/data/game_db.mv.db` sau khi Server dừng hoàn toàn. Không đưa bản sao lên Git.
3. Nếu thay đổi chỉ thêm cột/bảng và đã xác minh tương thích, Hibernate `update` có thể áp dụng lúc chạy bản code mới. Thay đổi dữ liệu hay ràng buộc phải có script migration idempotent, điều kiện kiểm tra dữ liệu và kế hoạch rollback; chạy trên bản sao trước.
4. Sau khi pull, theo hướng dẫn của migration tương ứng, bật Backend và chạy `mvn.cmd -B verify`. Không dùng `ddl-auto=create` hoặc `create-drop` ngoài database test tự hủy.

Hiện chưa có migration được đánh số ngoài thay đổi cộng thêm của hệ thống điểm. Xem `DATABASE_AUDIT.md` cho các đề xuất unique/đối soát chưa được phép áp trực tiếp lên dữ liệu hiện tại. Việc chia sẻ source không chia sẻ tài khoản, lịch sử trận hoặc phòng đang chơi; phòng và reconnect còn nằm trong RAM Server riêng.
