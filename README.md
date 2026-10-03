# Dự án Vẽ Hình Đoán Ý (Nhóm 8)

Hệ thống trò chơi "Vẽ Hình Đoán Ý" nhiều người chơi thời gian thực (Pictionary Online) sử dụng kiến trúc WebSocket.

- **Backend**: Java Core, WebSocket (`Java-WebSocket`), Hibernate ORM, MySQL DB, Gson, BCrypt.
- **Frontend**: React 19, Vite, Framer Motion, Howler.js, Lucide Icons.

---

## 🚀 HƯỚNG DẪN KHỞI CHẠY (QUICK START)

Chi tiết đầy đủ xem tại: 📄 [HUONG_DAN_CHAY.md](file:///c:/Users/TUAN/Downloads/BTL%20LTM/HUONG_DAN_CHAY.md)

### 1. Bật Backend Server (Java Maven)
Mở Terminal tại thư mục gốc dự án:
```bash
mvn clean compile exec:java "-Dexec.mainClass=server.ServerCore"
```
*(Server sẽ chạy tại `ws://localhost:9999`)*

---

### 2. Bật Frontend Client (React Vite)
Mở Terminal mới:
```bash
cd client
npm install   # (chỉ chạy ở lần đầu)
npm run dev
```
*(Client sẽ chạy tại `http://localhost:5173`)*

---

## ⚙️ CẤU HÌNH DATABASE
Cấu hình kết nối MySQL nằm trong: `src/main/resources/hibernate.cfg.xml`
- **Database**: `scribble_game`
- **Port**: `3306`
- **User**: `root`
- **Password**: `123456`
