package com.nhom8.vehinhdoany.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nhom8.vehinhdoany.model.User;

import java.util.List;

/**
 * Repository quản lý thực thể User (thay thế UserDAO).
 * Spring Data JPA tự động tạo implementation — không cần viết code Hibernate Session.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /** Tìm người chơi theo tên đăng nhập. */
    User findByUsername(String username);

    /** Lấy top N người chơi có điểm hạng cao nhất. */
    List<User> findTop50ByOrderByRankingScoreDesc();

    /** Lấy danh sách toàn bộ user sắp xếp theo điểm giảm dần (dùng cho admin). */
    List<User> findAllByOrderByRankingScoreDesc();

    /** Tìm kiếm người chơi theo tên đăng nhập (chứa từ khóa, không phân biệt hoa thường). */
    List<User> findByUsernameContainingIgnoreCase(String keyword);

    /** Tìm người chơi theo tên hiển thị (chính xác). */
    User findByDisplayName(String displayName);

    /** Tìm kiếm người chơi theo tên hiển thị (chứa từ khóa, không phân biệt hoa thường). */
    List<User> findByDisplayNameContainingIgnoreCase(String keyword);

    /** Cập nhật điểm hạng tích lũy cho người chơi. */
    @Modifying
    @Query("UPDATE User u SET u.rankingScore = u.rankingScore + :points WHERE u.userId = :userId")
    void addRankingScore(@Param("userId") Integer userId, @Param("points") int points);
}
