package com.nhom8.server.ws;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.nhom8.common.message.Envelope;
import com.nhom8.server.util.JsonUtil;

/**
 * Lớp đóng gói phiên kết nối WebSocket với một Client cụ thể.
 * Thay thế ClientHandler trong phiên bản Java-WebSocket cũ.
 *
 * Trong mô hình Spring WebSocket, mỗi ClientSession tương ứng
 * với một WebSocketSession của Spring, chịu trách nhiệm:
 * - Gửi thông điệp JSON tới Client
 * - Quản lý trạng thái người chơi (username, phòng hiện tại)
 */
public class ClientSession {

    /** Phiên WebSocket của Spring */
    private final WebSocketSession session;

    /** Tên đăng nhập của người chơi (null nếu chưa đăng nhập) */
    private String username;
    
    /** Tên hiển thị trong game */
    private String displayName;

    /** Mã phòng chơi mà người chơi đang tham gia (null nếu chưa vào phòng) */
    private String currentRoomId;

    public ClientSession(WebSocketSession session) {
        this.session = session;
    }

    /**
     * Gửi thông điệp JSON tới Client qua WebSocket.
     * Synchronized để tránh xung đột khi nhiều luồng cùng gửi.
     */
    public synchronized void sendMessage(Envelope envelope) {
        try {
            if (session != null && session.isOpen()) {
                String json = JsonUtil.toJson(envelope);
                session.sendMessage(new TextMessage(json));
            }
        } catch (Exception e) {
            System.err.println("[SERVER] Lỗi gửi thông điệp tới "
                    + getDisplayName() + ": " + e.getMessage());
        }
    }

    /**
     * Ngắt kết nối WebSocket.
     */
    public void disconnect() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            // Bỏ qua lỗi khi đóng kết nối
        }
    }

    // === Getters và Setters ===

    public WebSocketSession getSession() {
        return session;
    }

    public String getSessionId() {
        return session.getId();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(String currentRoomId) {
        this.currentRoomId = currentRoomId;
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    /**
     * Trả về tên hiển thị: displayName nếu đã đăng nhập,
     * ngược lại trả về session ID.
     */
    public String getDisplayName() {
        if (displayName != null) return displayName;
        if (username != null) return username;
        try {
            return session.getRemoteAddress() != null
                    ? session.getRemoteAddress().toString()
                    : session.getId();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
