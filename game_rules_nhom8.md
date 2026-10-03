1. Mô tả Kiến trúc chung của hệ thống
1.1. Mô hình kiến trúc Client-Server
Hệ thống "Game Vẽ Hình Đoán Ý" được xây dựng theo mô hình kiến trúc Client-Server hiện đại, chia tách rõ ràng trách nhiệm giữa máy chủ trung tâm và các ứng dụng khách:
Server (Máy chủ trung tâm): Đóng vai trò trung tâm điều phối toàn bộ hệ thống. Server chịu trách nhiệm lưu trữ cơ sở dữ liệu, quản lý và xác thực tài khoản, xử lý toàn bộ logic nghiệp vụ, đồng bộ dữ liệu real-time giữa các client, điều phối phòng chơi, trận đấu, tính toán điểm số và xếp hạng.
Client (Máy khách): Là ứng dụng giao diện chạy trên thiết bị của người chơi. Client tương tác trực tiếp với người dùng, cung cấp đồ họa giao diện (UI) trực quan cho các chức năng: đăng nhập/đăng ký, sảnh chờ, kết bạn, nhắn tin, quản lý phòng, công cụ vẽ tranh canvas, gửi đáp án đoán và xem kết quả bảng xếp hạng.
1.2. Các thành phần chính của hệ thống



Thành phần
Vai trò
Mô tả chi tiết
Server
Trung tâm xử lý
Lưu trữ và xử lý thông tin người chơi, phòng chơi, kho từ khóa, hình vẽ và kết quả trận đấu. Quản lý trạng thái kết nối, logic trận đấu, tính điểm và cập nhật xếp hạng.
Client
Giao diện người dùng
Cung cấp giao diện trực quan cho người chơi: đăng nhập, xem danh sách online, kết bạn, nhắn tin, tạo/tham gia phòng, tương tác vẽ tranh trên canvas, đoán đáp án và theo dõi kết quả.
Cơ sở dữ liệu
Lưu trữ dữ liệu
Lưu trữ bền vững thông tin tài khoản, lịch sử trận đấu, bảng xếp hạng, kho từ khóa, danh sách bạn bè và lịch sử tin nhắn. Được quản lý và truy xuất thông qua Hibernate (JPA).
Giao thức truyền thông
Kết nối Mạng
Truyền dữ liệu real-time hai chiều giữa Server và Client (hình vẽ, đáp án, trạng thái phòng, tin nhắn). Hỗ trợ cả cơ chế Request-Response (Client → Server) và Push/Broadcast (Server → Client) để đồng bộ trạng thái phòng và chat.
Module tính điểm
Xử lý nghiệp vụ điểm
Áp dụng thuật toán tính điểm người đoán dựa trên thứ hạng thời gian đoán đúng: Điểm(r) = 100 - (r-1) × [80/(m-1)]; và điểm người vẽ dựa trên tỷ lệ người đoán thành công: (số người đoán đúng / tổng số người đoán) × 100. Nếu không có ai đoán đúng, người vẽ sẽ bị trừ 20 điểm.

1.3. Luồng dữ liệu chính trong hệ thống
Đăng nhập & Xác thực: Client gửi thông tin tài khoản → Server xác thực thông tin với Cơ sở dữ liệu → Server khởi tạo phiên làm việc, trả về kết quả đăng nhập kèm danh sách người chơi đang online.
Tương tác xã hội & Chat: Client gửi yêu cầu kết bạn hoặc tin nhắn cá nhân → Server xử lý, lưu vết vào CSDL và định tuyến (route) trực tiếp tới người nhận nếu đang online.
Tạo & Quản lý phòng: Client tạo hoặc xin vào phòng → Server khởi tạo/thêm người chơi vào danh sách phòng → Server phát broadcast cập nhật danh sách và trạng thái phòng tới tất cả Client liên quan.
Khởi tạo trận đấu: Tất cả thành viên trong phòng nhấn Sẵn sàng (đủ tối thiểu 3 người) → Server kích hoạt chuyển trạng thái phòng sang thi đấu
Phân phối từ khóa: Server ngẫu nhiên chọn trước bộ từ khóa không trùng lặp (3 × số người chơi) → Gửi 3 lựa chọn riêng cho từng người chơi → Người chơi có 10 giây để chọn (nếu quá giờ Server tự động chọn) → Bắt đầu pha vẽ (60 giây).
Vẽ & Tải tranh: Người chơi thực hiện nét vẽ trên canvas → Chọn Gửi (Send) hoặc hết giờ 60s → Client gửi dữ liệu hình vẽ hoàn thiện về Server.
Trình chiếu & Đoán đáp án: Server chiếu từng bức tranh kèm gợi ý số ký tự (dạng gạch dưới) tới các người đoán → Người chơi nhập đáp án trong 30 giây (gửi nhiều lần) → Server kiểm tra đáp án, ghi nhận thứ hạng thời gian và tính điểm.
Tổng kết & Cập nhật: Sau khi kết thúc tất cả lượt đoán, Server tổng kết điểm số từng bức tranh → Hiển thị bảng xếp hạng trận đấu → Cập nhật điểm tích lũy tổng vào CSDL.
2. Mô tả Thiết kế chung của hệ thống
2.1. Các module chức năng chính
Module Xác thực (Authentication): Quản lý toàn bộ quy trình đăng ký, đăng nhập, đăng xuất và khôi phục tài khoản. Đảm bảo an toàn thông tin và duy trì phiên làm việc cho người chơi.
Module Quản lý người chơi (Player Management): Theo dõi danh sách người chơi online/offline, cập nhật trạng thái (rỗi, trong phòng, đang đấu), quản lý hồ sơ cá nhân, cấp độ và điểm số tích lũy.
Module Xã hội (Social): Quản lý các mối quan hệ bạn bè, gửi/nhận lời mời kết bạn, tìm kiếm người chơi, xem profile chi tiết và lịch sử các trận đấu đã tham gia.
Module Giao tiếp (Chat): Đảm nhận hai kênh giao tiếp riêng biệt: Kênh chat cá nhân (Private Chat 1-1 giữa bạn bè, có lưu lịch sử) và Kênh chat phòng (Room Chat dành cho thành viên phòng chờ).
Module Quản lý phòng chơi (Room Management): Xử lý việc tạo phòng, tìm phòng, vào/ra phòng. Kiểm tra điều kiện bắt đầu (3–6 người, 100% sẵn sàng). Xử lý sự cố mất kết nối hoặc thoát đột ngột (giải tán phòng nếu còn dưới 3 người, tự động chuyển quyền chủ phòng cho thành viên tiếp theo).
Module Trận đấu (Game Engine): Đóng vai trò bộ máy điều phối luồng chơi: trích xuất bộ từ khóa ngẫu nhiên từ CSDL (tối thiểu 3 × số người chơi), quản lý bộ đếm thời gian chọn từ (10s), thời gian vẽ (60s) và thời gian đoán (30s/bức tranh), kiểm soát việc nộp tranh tự động khi hết giờ.
Module Vẽ tranh (Drawing): Cung cấp bộ công cụ vẽ phong phú trên canvas Client (bút vẽ, tẩy, chọn màu, kích thước nét vẽ, xóa tất cả) và đóng gói dữ liệu hình vẽ chuyển về Server.
Module Đoán đáp án (Guessing): Hiển thị tranh vẽ kèm gợi ý dạng gạch dưới độ dài đáp án. Xử lý nhận đáp án nhập từ người chơi, đối chiếu với đáp án gốc và phản hồi kết quả chính xác ngay lập tức.
Module Tính điểm (Scoring): Thực thi công thức tính điểm chính xác: Người đoán đúng nhận Điểm(r) = 100 - (r-1) × [80/(m-1)] (tối thiểu 20 điểm); Người vẽ nhận điểm = (số người đoán đúng / tổng số người đoán) × 100 (nếu không có ai đoán đúng, người vẽ bị trừ 20 điểm).
Điểm phạt cho người vẽ: Trong trường hợp không có người chơi nào đoán đúng đáp án của bức tranh, người vẽ sẽ bị trừ 20 điểm.
Module Kết quả & Xếp hạng (Results & Ranking): Hiển thị bảng tổng kết chi tiết sau trận đấu và quản lý Bảng xếp hạng toàn hệ thống theo điểm tích lũy người chơi.
2.2. Thiết kế giao diện tổng quan
Màn hình Đăng nhập / Đăng ký: Giao diện cho phép nhập tài khoản, mật khẩu, chuyển đổi đăng ký tài khoản mới và khôi phục mật khẩu.
Màn hình Sảnh chính (Lobby): Hiển thị danh sách người chơi online, danh sách bạn bè, thông báo lời mời kết bạn, cửa sổ chat 1-1, danh sách các phòng hiện có, nút Tạo phòng, nút vào Bảng xếp hạng và Hồ sơ cá nhân.
Màn hình Phòng chờ: Hiển thị danh sách người chơi trong phòng kèm trạng thái Sẵn sàng, nút Sẵn sàng/Bắt đầu, nút Thoát phòng và khung Room Chat nội bộ.
Màn hình Chọn từ khóa: Giao diện hiển thị 3 lựa chọn từ khóa ngẫu nhiên kèm bộ đếm ngược 10 giây để người chơi chọn từ muốn vẽ.
Màn hình Vẽ tranh: Khu vực Canvas trung tâm, hiển thị từ khóa được giao, đồng hồ đếm ngược 60s, thanh công cụ vẽ (màu sắc, kích thước nét, xóa) và nút Gửi tranh.
Màn hình Đoán tranh: Trình chiếu bức tranh vẽ, gợi ý số ký tự (dạng gạch dưới "_ _ _"), khung nhập đáp án, bộ đếm 30s và lịch sử các câu đoán đã gửi.
Màn hình Kết quả trận đấu: Bảng vinh danh thứ hạng trận đấu, tổng điểm từng người chơi thu được và danh sách chi tiết các bức tranh trong lượt.
Màn hình Bảng xếp hạng: Bảng tổng hợp điểm tích lũy toàn hệ thống, xếp hạng danh hiệu người chơi.
2.3. Các quy tắc nghiệp vụ quan trọng
Quy mô phòng chơi: Tối thiểu 3 người chơi và tối đa 6 người chơi trong một phòng.
Điều kiện bắt đầu: Trận đấu chỉ được khởi tạo khi 100% thành viên trong phòng nhấn Sẵn sàng.
Quy tắc chọn từ khóa: Mỗi người nhận 3 từ khóa gợi ý riêng không trùng lắp trong cùng phòng, có 10 giây để chọn (quá giờ Server tự chọn).
Pha vẽ tranh: Thời gian tối đa 60 giây. Không được dùng chữ hoặc số để trực tiếp ghi đáp án lên tranh vẽ.
Pha đoán tranh: Thời gian 30 giây cho mỗi bức tranh. Người đoán được phép nhập và gửi đáp án nhiều lần.
Quyền đoán: Tác giả của bức tranh không được tham gia đoán bức tranh do chính mình vẽ.
Gợi ý đáp án: Hệ thống hiển thị dạng gạch dưới tương ứng với độ dài và số lượng từ của đáp án (ví dụ: "___ ____" cho "con mèo").
Xử lý sự cố người chơi rời phòng: Nếu số lượng người chơi giảm xuống dưới 3 (do thoát chủ động hoặc mất kết nối), trận đấu lập tức bị chấm dứt và hủy kết quả.
Chuyển quyền chủ phòng: Khi chủ phòng rời đi hoặc mất kết nối, Server tự động chuyển giao quyền chủ phòng cho người tiếp theo trong danh sách.
Tự động nộp bài: Nếu hết 60 giây vẽ mà người chơi chưa chọn Gửi, Server sẽ tự động lấy dữ liệu canvas hiện tại làm bản nộp chính thức.
Giới hạn kiểm duyệt nét vẽ: Quy định không viết chữ ghi đáp án là quy tắc fair-play, hệ thống không tự động nhận diện chữ viết tay trên canvas.
Lưu trữ kết quả: Kết quả và điểm số sau mỗi trận đấu hợp lệ được lưu lại CSDL để tính điểm xếp hạng toàn hệ thống.


