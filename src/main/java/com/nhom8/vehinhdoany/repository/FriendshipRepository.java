package com.nhom8.vehinhdoany.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nhom8.vehinhdoany.model.Friendship;
import com.nhom8.vehinhdoany.model.User;

import java.util.List;

/**
 * Repository quản lý thực thể Friendship (quan hệ bạn bè).
 * Cung cấp các truy vấn tìm kiếm danh sách bạn bè, lời mời đang chờ,
 * và kiểm tra quan hệ giữa hai người chơi.
 */
@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

    /**
     * Tìm quan hệ bạn bè giữa sender và receiver (một chiều).
     */
    Friendship findBySenderAndReceiver(User sender, User receiver);

    /**
     * Tìm quan hệ bạn bè giữa hai user (bất kể ai là sender/receiver).
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.sender = :user1 AND f.receiver = :user2) OR " +
            "(f.sender = :user2 AND f.receiver = :user1)")
    Friendship findByUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Lấy danh sách bạn bè đã xác nhận (status = ACCEPTED) của một user.
     * Trả về tất cả Friendship mà user là sender hoặc receiver.
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.sender.userId = :userId OR f.receiver.userId = :userId) " +
            "AND f.status = 'ACCEPTED'")
    List<Friendship> findAllAcceptedFriends(@Param("userId") Integer userId);

    /**
     * Lấy danh sách lời mời kết bạn đang chờ xử lý mà user là người nhận.
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "f.receiver.userId = :userId AND f.status = 'PENDING'")
    List<Friendship> findPendingRequestsForUser(@Param("userId") Integer userId);

    /**
     * Lấy danh sách lời mời kết bạn đã gửi đi mà user là người gửi.
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "f.sender.userId = :userId AND f.status = 'PENDING'")
    List<Friendship> findPendingSentByUser(@Param("userId") Integer userId);
}
