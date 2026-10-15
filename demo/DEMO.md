# Demo Vẽ Hình Đoán Ý trên Windows

## Gói nào gửi cho ai?

- `ve-hinh-demo-windows.zip`: gửi cho người muốn **chơi/demo**. Có server JAR, client JAR, thư viện JavaFX và lệnh chạy. Cần Java 21 trở lên, không cần Maven.
- `ve-hinh-source.zip`: gửi cho giảng viên/nhóm muốn **xem hoặc sửa mã nguồn**. Cần Java 21 và Maven để build. Gói này không có cơ sở dữ liệu cá nhân trong `server/data`.

## Máy của bạn làm server trong cùng Wi-Fi/LAN

1. Giải nén gói demo. Chạy `start-server.cmd` trên máy chủ và giữ cửa sổ đó mở.
2. Mở PowerShell **Run as administrator** tại thư mục giải nén, chạy `powershell -ExecutionPolicy Bypass -File .\allow-lan-firewall.ps1` một lần. Quy tắc chỉ mở TCP 8080 cho các máy trong mạng nội bộ, kể cả khi Windows đặt Wi-Fi là Public.
3. Xem IP Wi-Fi của máy chủ: `ipconfig` hoặc `Get-NetIPAddress -AddressFamily IPv4`. Chọn IP của Wi-Fi/Ethernet, không dùng `127.0.0.1`, `172.31.*` của máy ảo, hay IP VPN. IP của máy khi chuẩn bị gói lần này là `192.168.1.180`; IP này có thể đổi.
4. Người chơi cùng Wi-Fi giải nén gói demo và chạy `start-client.cmd 192.168.1.180:8080` (thay bằng IP hiện tại của máy chủ). Có thể nhập IP ngay trên màn đăng nhập.
5. Kiểm tra từ máy người chơi: `Test-NetConnection 192.168.1.180 -Port 8080`. `TcpTestSucceeded : True` nghĩa là đã tới server. Nếu sai, kiểm tra cùng mạng, firewall, IP và cửa sổ server.

Máy chủ cũng có thể chạy `start-client.cmd localhost:8080` để tham gia chơi. Mỗi người đăng nhập một tài khoản khác nhau. Các tài khoản demo: `player1`, `player2`, `player3`, `admin`; mật khẩu ban đầu `123456`.

## Người chơi ở mạng khác

Cách dễ cho buổi demo là cho các máy tham gia một mạng riêng như Tailscale, rồi nhập IP Tailscale `100.x.y.z:8080` của máy chủ vào client. Kiểm tra cổng bằng `Test-NetConnection` như trên. Khi dùng Tailscale, có thể cần một quy tắc Windows Firewall cho mạng Tailscale thay cho quy tắc LocalSubnet ở trên.

Không đưa cổng 8080 trực tiếp ra Internet khi còn tài khoản demo/mật khẩu mặc định và WebSocket `ws://` chưa mã hóa. Nếu buộc phải dùng port forwarding, cần đổi thông tin đăng nhập, dùng TLS (`wss://`) qua reverse proxy và kiểm tra nhà mạng có cấp IP công khai hay dùng CGNAT.

## Ghi chú

- Dữ liệu H2 được tạo và lưu trong `server/data` của thư mục demo. Đừng xóa thư mục này nếu muốn giữ tài khoản/trận cũ.
- Dừng server bằng `Ctrl+C` trong cửa sổ server.
- Client trong gói này dành cho Windows x64 vì thư viện JavaFX đi kèm là bản Windows.
