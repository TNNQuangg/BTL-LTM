package com.nhom8.server.config;

import com.nhom8.server.model.User;
import com.nhom8.server.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDateTime;

/**
 * Tài khoản mẫu chỉ được tạo khi game.demo-seed.enabled=true.
 */
@Component
@ConditionalOnProperty(name = "game.demo-seed.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final com.nhom8.server.repository.FriendshipRepository friendshipRepository;
    private final com.nhom8.server.repository.ChatMessageRepository chatMessageRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(UserRepository userRepository,
                           com.nhom8.server.repository.FriendshipRepository friendshipRepository,
                           com.nhom8.server.repository.ChatMessageRepository chatMessageRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public void run(String... args) {
        String defaultPasswordHash = passwordEncoder.encode("123456");

        Object[][] defaultUsers = new Object[][] {
            {"admin", "Quản Trị Viên", "ADMIN", "1", 100},
            {"123456", "vuhuy", "USER", "2", 100},
            {"vuhuy", "Vũ Huy", "USER", "2", 100},
            {"playerone", "PlayerOne", "USER", "1", 100},
            {"testuser", "Test User", "USER", "1", 100},
            {"player1", "Người Chơi 1", "USER", "1", 50},
            {"player2", "Người Chơi 2", "USER", "2", 50},
            {"player3", "Người Chơi 3", "USER", "3", 50}
        };

        for (Object[] uData : defaultUsers) {
            String username = (String) uData[0];
            String displayName = (String) uData[1];
            String role = (String) uData[2];
            String avatar = (String) uData[3];
            int score = (Integer) uData[4];

            User user = userRepository.findByUsername(username);
            if (user == null) {
                user = new User(username, defaultPasswordHash, displayName);
                user.setRole(role);
                user.setAvatar(avatar);
                user.setRankingScore(score);
                user.setCreatedAt(LocalDateTime.now());
                try {
                    userRepository.save(user);
                } catch (Exception e) {
                    System.err.println("[DataInitializer] Không thể tạo tài khoản " + username + ": " + e.getMessage());
                }
            }
        }

        // Khởi tạo quan hệ bạn bè mặc định nếu chưa có
        try {
            seedFriendship("admin", "testuser", com.nhom8.server.model.Friendship.FriendshipStatus.ACCEPTED);
            seedFriendship("admin", "123456", com.nhom8.server.model.Friendship.FriendshipStatus.ACCEPTED);
            seedFriendship("admin", "playerone", com.nhom8.server.model.Friendship.FriendshipStatus.ACCEPTED);
            seedFriendship("testuser", "123456", com.nhom8.server.model.Friendship.FriendshipStatus.ACCEPTED);
            seedFriendship("player1", "player2", com.nhom8.server.model.Friendship.FriendshipStatus.ACCEPTED);
            seedFriendship("player3", "admin", com.nhom8.server.model.Friendship.FriendshipStatus.PENDING); // Lời mời kết bạn đang chờ admin duyệt

            // Khởi tạo một số tin nhắn mẫu giữa admin và testuser
            if (chatMessageRepository.count() == 0) {
                User uAdmin = userRepository.findByUsername("admin");
                User uTest = userRepository.findByUsername("testuser");
                if (uAdmin != null && uTest != null) {
                    com.nhom8.server.model.ChatMessage m1 = new com.nhom8.server.model.ChatMessage(uAdmin, uTest, "Chào bạn testuser! Sẵn sàng vào phòng chơi vẽ tranh chưa?");
                    com.nhom8.server.model.ChatMessage m2 = new com.nhom8.server.model.ChatMessage(uTest, uAdmin, "Sẵn sàng rồi admin ơi! Tạo phòng ngay nhé.");
                    chatMessageRepository.save(m1);
                    chatMessageRepository.save(m2);
                }
            }
        } catch (Exception e) {
            System.err.println("[DataInitializer] Lưu ý khởi tạo bạn bè/chat: " + e.getMessage());
        }

        System.out.println("[SERVER] [DataInitializer] Đồng bộ tài khoản, bạn bè & tin nhắn hoàn tất (Mật khẩu: 123456).");
    }

    private void seedFriendship(String u1Name, String u2Name, com.nhom8.server.model.Friendship.FriendshipStatus status) {
        User u1 = userRepository.findByUsername(u1Name);
        User u2 = userRepository.findByUsername(u2Name);
        if (u1 != null && u2 != null) {
            com.nhom8.server.model.Friendship existing = friendshipRepository.findByUsers(u1, u2);
            if (existing == null) {
                com.nhom8.server.model.Friendship f = new com.nhom8.server.model.Friendship(u1, u2);
                f.setStatus(status);
                f.setCreatedAt(LocalDateTime.now());
                friendshipRepository.save(f);
            }
        }
    }
}
