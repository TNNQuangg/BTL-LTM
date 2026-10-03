package com.nhom8.vehinhdoany.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.nhom8.vehinhdoany.dto.AuthRequest;
import com.nhom8.vehinhdoany.dto.AuthResponse;
import com.nhom8.vehinhdoany.dto.ChangePasswordRequest;
import com.nhom8.vehinhdoany.dto.UpdateProfileRequest;
import com.nhom8.vehinhdoany.model.User;
import com.nhom8.vehinhdoany.repository.UserRepository;

import com.nhom8.vehinhdoany.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.nhom8.vehinhdoany.websocket.ClientSession;

@Service
public class AuthService {
    
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Autowired
    private GameService gameService;

    public AuthService(UserRepository userRepository, JwtTokenProvider tokenProvider ) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider; 
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("Tên đăng nhập không tồn tại.");
        }

        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu không chính xác.");
        }
        String token = tokenProvider.generateToken(user.getUsername());
        

        AuthResponse response = new AuthResponse("Đăng nhập thành công!");
        response.setToken(token);
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setRankingScore(user.getRankingScore());
        response.setRole(user.getRole());
        response.setAvatar(user.getAvatar());
        return response;
    }

    public AuthResponse register(AuthRequest request) {
        if (userRepository.findByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng. Vui lòng chọn tên khác.");
        }

        if (request.getUsername() == null || request.getUsername().trim().length() < 3) {
            throw new IllegalArgumentException("Tên đăng nhập phải có ít nhất 3 ký tự.");
        }

        if (request.getPassword() == null || request.getPassword().length() < 4) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 4 ký tự.");
        }

        String displayName = request.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = request.getUsername();
        }

        if (!displayName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Tên hiển thị phải viết liền, không dấu và không chứa ký tự đặc biệt.");
        }

        // Kiểm tra trùng displayName
        User existingDisplayName = userRepository.findByDisplayName(displayName);
        if (existingDisplayName != null) {
            throw new IllegalArgumentException("Tên hiển thị \"" + displayName + "\" đã được sử dụng. Vui lòng chọn tên khác.");
        }

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
        User newUser = new User(request.getUsername(), hashedPassword, displayName);
        userRepository.save(newUser);

        return new AuthResponse("Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.");
    }

    public AuthResponse updateProfile(UpdateProfileRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống.");
        }
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("Người dùng không tồn tại.");
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getDisplayName() != null && !request.getDisplayName().trim().isEmpty()) {
            if (!request.getDisplayName().matches("^[a-zA-Z0-9_]+$")) {
                throw new IllegalArgumentException("Tên hiển thị phải viết liền, không dấu và không chứa ký tự đặc biệt.");
            }
            // Kiểm tra trùng displayName (bỏ qua chính user hiện tại)
            if (!request.getDisplayName().equals(user.getDisplayName())) {
                User existingDisplayName = userRepository.findByDisplayName(request.getDisplayName());
                if (existingDisplayName != null) {
                    throw new IllegalArgumentException("Tên hiển thị \"" + request.getDisplayName() + "\" đã được sử dụng. Vui lòng chọn tên khác.");
                }
            }
            user.setDisplayName(request.getDisplayName());
        }
        userRepository.save(user);

        if (gameService != null) {
            ClientSession session = gameService.getClientSession(user.getUsername());
            if (session != null) {
                session.setDisplayName(user.getDisplayName());
                gameService.broadcastPlayerList();
                
                if (session.getCurrentRoomId() != null) {
                    com.nhom8.vehinhdoany.game.Room room = gameService.getRoomManager().getRoom(session.getCurrentRoomId());
                    if (room != null) {
                        gameService.broadcastRoomUpdate(room);
                    }
                }
            }
        }

        AuthResponse response = new AuthResponse("Cập nhật thông tin thành công!");
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setAvatar(user.getAvatar());
        response.setRankingScore(user.getRankingScore());
        response.setRole(user.getRole());
        return response;
    }

    public void changePassword(ChangePasswordRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống.");
        }
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("Người dùng không tồn tại.");
        }

        if (request.getOldPassword() == null || !BCrypt.checkpw(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác.");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 4) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 4 ký tự.");
        }
        if(request.getConfirmPassword() == null || !request.getConfirmPassword().equals(request.getNewPassword()) ){
            throw new IllegalArgumentException("Xác nhận mật khẩu không khớp.");
        }

        String newHashedPassword = BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt());
        user.setPassword(newHashedPassword);
        userRepository.save(user);
    }

    public void updateProfile(String username, String avatar, String displayName) {
        updateProfile(new UpdateProfileRequest(username, displayName, avatar));
    }
}
