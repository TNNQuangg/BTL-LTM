package com.nhom8.server.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Thực thể quan hệ bạn bè giữa hai người chơi trong hệ thống.
 * Mỗi bản ghi biểu diễn một lời mời kết bạn hoặc quan hệ bạn bè đã xác nhận.
 *
 * Trạng thái quan hệ:
 * - PENDING: Lời mời đã gửi, đang chờ người nhận xác nhận.
 * - ACCEPTED: Cả hai bên đã là bạn bè.
 * - REJECTED: Người nhận đã từ chối lời mời.
 */
@Entity
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
})
public class Friendship implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friendship_id")
    private Integer friendshipId;

    /** Người gửi lời mời kết bạn */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /** Người nhận lời mời kết bạn */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    /** Trạng thái quan hệ bạn bè */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    /** Thời điểm tạo lời mời */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum FriendshipStatus {
        PENDING, ACCEPTED, REJECTED
    }

    public Friendship() {
    }

    public Friendship(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = FriendshipStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // === Getters và Setters ===

    public Integer getFriendshipId() {
        return friendshipId;
    }

    public void setFriendshipId(Integer friendshipId) {
        this.friendshipId = friendshipId;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Friendship{" +
                "friendshipId=" + friendshipId +
                ", sender=" + (sender != null ? sender.getUsername() : "null") +
                ", receiver=" + (receiver != null ? receiver.getUsername() : "null") +
                ", status=" + status +
                '}';
    }
}
