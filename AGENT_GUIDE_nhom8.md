# HƯỚNG DẪN AGENT TRIỂN KHAI DỰ ÁN "VẼ HÌNH ĐOÁN Ý" (NHÓM 8)
*File này dùng để chỉ dẫn AI coding agent (Claude Code hoặc tương đương) khi viết code cho đồ án. Agent PHẢI đọc và tuân thủ toàn bộ quy định dưới đây trước khi sinh code.*

---

## 0. NGUYÊN TẮC BẮT BUỘC (ĐỌC TRƯỚC TIÊN)

1. **Chỉ dùng công nghệ có trong giáo trình** (`lap-trinh-mang-tai-lieu.md`). Không tự ý thêm framework/thư viện ngoài phạm vi môn học (ví dụ: KHÔNG dùng Spring Boot, KHÔNG dùng WebSocket/JSR 356, KHÔNG dùng RMI) trừ khi được yêu cầu rõ ràng — vì đề tài đã chốt kiến trúc **Socket TCP thuần + Swing**.
   - **Ngoại lệ đã được Huy duyệt:** tầng cơ sở dữ liệu dùng **Hibernate (ORM)** thay cho JDBC thuần như mẫu giáo trình. Đây là lựa chọn có chủ đích ngoài phạm vi giáo trình — cần ghi chú rõ trong báo cáo là "mở rộng so với giáo trình" để giảng viên không hiểu nhầm là sai lệch không chủ đích.
2. **Bám sát luật chơi đã chốt** trong `game_rules_nhom8.md`. Không tự thay đổi cơ chế điểm số, thời gian, quy mô phòng... nếu Huy chưa yêu cầu sửa.
3. **Tuân thủ mô hình MVC** giống case study mẫu trong giáo trình (Model / View / Controller tách riêng, đặt tên lớp theo mẫu: `XxxControl.java`, `XxxView.java`).
4. **Comment và tài liệu báo cáo viết bằng tiếng Việt trang trọng**, đúng văn phong giáo trình (câu đầy đủ chủ ngữ - vị ngữ, không viết tắt tùy tiện).
5. Trước khi code một module mới, agent nên tóm tắt lại thiết kế (schema bảng, giao thức message, tên lớp) và xác nhận với Huy nếu có điểm chưa rõ, thay vì tự suy đoán rồi code sai hướng.

---

## 1. NGĂN XẾP CÔNG NGHỆ (TECH STACK) — CHỈ DÙNG NHỮNG GÌ DƯỚI ĐÂY

| Thành phần | Công nghệ | Ghi chú |
|---|---|---|
| Ngôn ngữ | Java (không giới hạn phiên bản cụ thể) | Phiên bản JDK nào cũng được — điều quan trọng là **dùng đúng kỹ thuật/khái niệm** theo giáo trình (Socket, Thread, MVC...), không phải chọn đúng version |
| Giao tiếp mạng | `Socket` / `ServerSocket` (TCP) | KHÔNG dùng UDP (game cần độ tin cậy dữ liệu), KHÔNG dùng NIO/Selector trừ khi cần tối ưu nâng cao |
| Đa luồng | `Thread` / `Runnable`, có thể dùng `ExecutorService` (thread pool) | Server phải xử lý đồng thời nhiều Client |
| Đóng gói dữ liệu | Lớp Model `implements Serializable` + `ObjectInputStream`/`ObjectOutputStream` | Theo đúng mẫu `User.java` trong giáo trình |
| Giao diện Client | Java Swing (`JFrame`, `JPanel`, `Graphics2D`) | Không dùng JavaFX trừ khi được yêu cầu |
| Vẽ canvas | `Graphics2D`, `MouseListener`, `MouseMotionListener` | Lưu nét vẽ dạng vector (danh sách tọa độ), không lưu bitmap |
| Cơ sở dữ liệu | MySQL qua **Hibernate (JPA)** | ORM thay cho JDBC thuần — dùng `Session`/`EntityManager`, các thao tác kiểu `findById`, `save`, `delete` thay vì viết SQL tay |
| Kiến trúc | MVC 3 lớp (Model - View - Controller) | Theo đúng cấu trúc package mẫu: `client/`, `server/` |

**Không sử dụng** (ngoài phạm vi giáo trình, agent phải từ chối nếu bị yêu cầu thêm mà không có lý do rõ ràng): Spring, Servlet/JSP web server, WebSocket, RMI, microservices, Docker, cloud deploy — trừ khi Huy chủ động mở rộng đề tài.

---

## 2. CƠ SỞ DỮ LIỆU (DATABASE)

### 2.1 Nguyên tắc thiết kế
- Chuẩn hóa tối thiểu tới 3NF, áp dụng đúng nguyên tắc trong giáo trình Chương 3 (quan hệ 1-1 gộp bảng, 1-n thêm khóa ngoại, n-n tạo bảng trung gian).
- Tên bảng/cột: snake_case, tiếng Anh, số ít hoặc số nhiều nhất quán trong toàn bộ schema.

### 2.2 Đề xuất schema (agent xác nhận với Huy trước khi tạo bảng thật)

```sql
-- Tài khoản người chơi
CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) UNIQUE NOT NULL,
    password      VARCHAR(255) NOT NULL,
    ranking_score INT DEFAULT 0,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Lịch sử trận đấu
CREATE TABLE matches (
    match_id    INT AUTO_INCREMENT PRIMARY KEY,
    room_id     VARCHAR(50) NOT NULL,
    started_at  DATETIME,
    ended_at    DATETIME
);

-- Kết quả từng người chơi trong 1 trận (bảng trung gian n-n giữa users và matches)
CREATE TABLE match_results (
    result_id    INT AUTO_INCREMENT PRIMARY KEY,
    match_id     INT NOT NULL,
    user_id      INT NOT NULL,
    points_drawn INT DEFAULT 0,   -- điểm từ vai trò người vẽ
    points_guess INT DEFAULT 0,   -- điểm từ vai trò người đoán
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Chi tiết từng bức tranh trong trận (tùy chọn, nếu cần lưu lịch sử tranh vẽ)
CREATE TABLE paintings (
    painting_id INT AUTO_INCREMENT PRIMARY KEY,
    match_id    INT NOT NULL,
    painter_id  INT NOT NULL,
    topic       VARCHAR(100) NOT NULL,
    stroke_data LONGTEXT,   -- dữ liệu vector dạng JSON/serialized
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (painter_id) REFERENCES users(user_id)
);
```

### 2.3 Kết nối & thao tác dữ liệu bằng Hibernate

**Cấu hình `hibernate.cfg.xml`:**
```xml
<hibernate-configuration>
  <session-factory>
    <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
    <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/scribble_game</property>
    <property name="hibernate.connection.username">root</property>
    <property name="hibernate.connection.password">&lt;password&gt;</property>
    <property name="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</property>
    <property name="hibernate.hbm2ddl.auto">update</property>
    <property name="show_sql">true</property>
    <mapping class="model.User"/>
    <mapping class="model.Match"/>
    <mapping class="model.MatchResult"/>
    <mapping class="model.Painting"/>
  </session-factory>
</hibernate-configuration>
```

**Entity mẫu (thay cho `User.java implements Serializable` thuần của giáo trình — nay dùng annotation JPA):**
```java
package model;
import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    @Column(name = "ranking_score")
    private Integer rankingScore = 0;

    // getters/setters
}
```
> Lưu ý: nếu vẫn cần gửi `User` qua socket (message protocol), lớp Entity nên `implements Serializable` song song với annotation `@Entity` — không xung đột, chỉ cần thêm `implements Serializable` vào class.

**DAO pattern với `findById` (thay cho Controller viết SQL tay như mẫu giáo trình):**
```java
package dao;
import model.User;
import org.hibernate.Session;
import util.HibernateUtil;

public class UserDAO {
    public User findById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        }
    }

    public User findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User WHERE username = :uname", User.class)
                          .setParameter("uname", username)
                          .uniqueResult();
        }
    }

    public void save(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(user);
            session.getTransaction().commit();
        }
    }
}
```

**`HibernateUtil.java` (khởi tạo SessionFactory dùng chung toàn server):**
```java
package util;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        return new Configuration().configure().buildSessionFactory();
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
```

**Quy ước cho agent:**
- Mỗi Entity (`User`, `Match`, `MatchResult`, `Painting`) có 1 DAO tương ứng (`UserDAO`, `MatchDAO`...), tối thiểu gồm `findById`, `save`, `delete`.
- Controller (`ServerControl.java` trong kiến trúc MVC) gọi qua DAO, **không** tự viết `Session`/query trực tiếp trong Controller — giữ tách lớp rõ ràng.
- Dependency cần thêm vào project (Maven/Gradle): `hibernate-core`, `mysql-connector-j`.
- Cân nhắc `hibernate.hbm2ddl.auto=update` chỉ dùng khi phát triển — nếu giáo trình/giảng viên yêu cầu nộp kèm script SQL tạo schema riêng, nên tắt tùy chọn này và chạy migration bằng tay.

---

## 3. KIẾN TRÚC 4 WORKSTREAM — QUY ƯỚC PHÂN CHIA CODE

| Workstream | Vai trò | File/package gợi ý |
|---|---|---|
| Server socket core | `ServerSocket`, `accept()`, quản lý luồng client, thread pool | `server/ServerCore.java`, `server/ClientHandler.java` |
| Client socket core | `Socket`, gửi/nhận dữ liệu, reconnect | `client/ClientCore.java` |
| Message protocol | Định nghĩa các lớp message (Serializable), loại message (LOGIN, JOIN_ROOM, DRAW_SUBMIT, GUESS_SUBMIT, SCORE_UPDATE...) | `protocol/MessageType.java`, `protocol/*.java` |
| Room/state management | Quản lý phòng chơi, trạng thái người chơi, đồng bộ điểm | `server/room/Room.java`, `server/room/RoomManager.java` |

**Quy tắc đặt tên message class:** `<Action>Message.java` (ví dụ `DrawSubmitMessage`, `GuessSubmitMessage`), tất cả implements `Serializable`, có trường `type` để phân loại khi deserialize phía nhận.

---

## 4. LUẬT CHƠI ĐÃ CHỐT — AGENT KHÔNG ĐƯỢC TỰ SỬA

Tóm tắt bắt buộc tuân thủ khi code logic server (chi tiết đầy đủ xem `game_rules_nhom8.md`):

- Phòng chơi: tối thiểu **3**, tối đa **6** người.
- Giai đoạn vẽ: **60 giây/người**, mỗi người 1 chủ đề riêng không trùng.
- Giai đoạn đoán: **30 giây/bức tranh**, gợi ý hiển thị dạng gạch dưới theo số ký tự/từ.
- Công thức điểm người đoán:
  `Điểm(r) = 100 - (r-1) × [80/(m-1)]` (làm tròn xuống, tối thiểu 20 điểm)
- Điểm người vẽ: `(số người đoán đúng / tổng số người đoán) × 100`
- Chỉ có 2 kết quả: đoán đúng (có điểm) hoặc sai/timeout (0 điểm) — **không có mức "gần đúng"**.
- Rời phòng giữa chừng: nếu số người còn lại < 3 → Server giải tán phòng ngay lập tức.

Nếu bất kỳ yêu cầu code nào mâu thuẫn với danh sách trên, agent phải dừng lại và hỏi lại Huy trước khi triển khai, thay vì tự suy đoán.

---

## 5. QUY TRÌNH LÀM VIỆC ĐỀ XUẤT CHO AGENT

1. Đọc kỹ `game_rules_nhom8.md` và `lap-trinh-mang-tai-lieu.md` trước khi viết bất kỳ dòng code nào.
2. Xác định workstream đang làm (1 trong 4 mục ở phần 3).
3. Thiết kế message protocol trước, đảm bảo Client/Server dùng chung 1 bộ class message.
4. Code Model → Controller → View (đúng thứ tự MVC).
5. Với mỗi module, viết kèm phần mô tả bằng tiếng Việt trang trọng để đưa vào báo cáo (giống văn phong mẫu Rock-Paper-Scissors của giảng viên).
6. Không tự thêm tính năng ngoài phạm vi luật chơi đã chốt trừ khi Huy yêu cầu.

---

*File này nên được đặt ở thư mục gốc dự án (ví dụ `docs/AGENT_GUIDE.md`) để agent luôn đọc được khi bắt đầu phiên làm việc mới.*
