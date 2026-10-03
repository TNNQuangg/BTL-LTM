package com.nhom8.server.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.nhom8.server.model.User;
import com.nhom8.server.repository.UserRepository;
import com.nhom8.common.dto.PlayerDTO;
import com.nhom8.common.message.payload.LoginResultPayload;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    private GameService gameService;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public LoginResultPayload login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Tên đăng nhập không tồn tại.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu không chính xác.");
        }
        
        PlayerDTO playerDTO = new PlayerDTO(user.getUsername(), user.getDisplayName(), user.getAvatar(), user.getRankingScore(), "ONLINE", false);
        return new LoginResultPayload(playerDTO, null);
    }

    public void register(String username, String password, String displayName) {
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng.");
        }
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Tên đăng nhập phải có ít nhất 3 ký tự.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 4 ký tự.");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = username;
        }
        if (!displayName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Tên hiển thị phải viết liền, không dấu.");
        }
        User existingDisplayName = userRepository.findByDisplayName(displayName);
        if (existingDisplayName != null) {
            throw new IllegalArgumentException("Tên hiển thị đã được sử dụng.");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(username, hashedPassword, displayName);
        userRepository.save(newUser);
    }
}
