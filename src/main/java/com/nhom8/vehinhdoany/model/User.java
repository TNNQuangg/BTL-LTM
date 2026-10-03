package com.nhom8.vehinhdoany.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Thực thể người chơi trong hệ thống trò chơi "Vẽ Hình Đoán Ý".
 * Lưu trữ thông tin tài khoản, điểm hạng tích lũy và thời điểm tạo tài khoản.
 * Lớp này đóng vai trò Entity (JPA/Hibernate) để ánh xạ với bảng "users"
 * trong cơ sở dữ liệu, được Spring Data JPA quản lý.
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "ranking_score")
    private Integer rankingScore = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 20)
    private String role = "USER";

    @Column(nullable = false, length = 50)
    private String avatar = "1";

    @Column(name = "display_name", unique = true, nullable = false, length = 50)
    private String displayName;

    /**
     * Hàm khởi tạo mặc định (bắt buộc cho Hibernate).
     */
    public User() {
    }

    /**
     * Hàm khởi tạo với tên đăng nhập, mật khẩu và tên hiển thị.
     * Thời điểm tạo tài khoản được gán tự động.
     */
    public User(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.rankingScore = 0;
        this.role = "admin".equalsIgnoreCase(username) ? "ADMIN" : "USER";
    }

    /**
     * Gán thời điểm tạo tài khoản tự động trước khi lưu vào cơ sở dữ liệu.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // === Getters và Setters ===

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRankingScore() {
        return rankingScore;
    }

    public void setRankingScore(Integer rankingScore) {
        this.rankingScore = rankingScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Cộng thêm điểm vào điểm hạng tích lũy của người chơi.
     */
    public void addScore(int points) {
        this.rankingScore += points;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", rankingScore=" + rankingScore +
                '}';
    }
}
