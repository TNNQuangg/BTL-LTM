package com.nhom8.vehinhdoany.websocket;

import com.google.gson.JsonSyntaxException;
import com.nhom8.vehinhdoany.protocol.Message;
import com.nhom8.vehinhdoany.protocol.MessageType;
import com.nhom8.vehinhdoany.service.GameService;
import com.nhom8.vehinhdoany.util.JsonUtil;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring WebSocket Handler — điểm tiếp nhận kết nối WebSocket từ React Client.
 * Thay thế ServerCore (Java-WebSocket) + phần dispatch trong ClientHandler.
 *
 * Kiến trúc MVC:
 * - GameWebSocketHandler = View (tầng giao tiếp mạng, nhận/gửi JSON)
 * - GameService = Controller (xử lý logic nghiệp vụ)
 * - Entity + Repository = Model (dữ liệu)
 *
 * Mỗi kết nối WebSocket được gán một ClientSession,
 * thông điệp JSON được parse và dispatch tới GameService xử lý.
 */
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    /** Bảng ánh xạ sessionId → ClientSession */
    private final ConcurrentHashMap<String, ClientSession> sessions = new ConcurrentHashMap<>();

    /** Service xử lý logic nghiệp vụ (được Spring inject) */
    private final GameService gameService;

    public GameWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Xử lý khi có kết nối WebSocket mới từ Client.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String address = session.getRemoteAddress() != null
                ? session.getRemoteAddress().toString() : session.getId();
        System.out.println("[SERVER] Kết nối mới từ: " + address);

        ClientSession clientSession = new ClientSession(session);
        sessions.put(session.getId(), clientSession);
        gameService.addClient(clientSession);
    }

    /**
     * Xử lý khi nhận thông điệp JSON từ Client.
     * Parse JSON → Message → dispatch theo MessageType tới GameService.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        ClientSession clientSession = sessions.get(session.getId());
        if (clientSession == null) return;

        try {
            Message message = JsonUtil.fromJson(textMessage.getPayload(), Message.class);
            if (message != null && message.getType() != null) {
                System.out.println("[SERVER] Nhận từ " + clientSession.getDisplayName() + ": " + message.getType());
                dispatchMessage(clientSession, message);
            }
        } catch (JsonSyntaxException e) {
            System.err.println("[SERVER] JSON không hợp lệ từ "
                    + clientSession.getDisplayName() + ": " + e.getMessage());
        }
    }

    /**
     * Xử lý khi Client ngắt kết nối.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        ClientSession clientSession = sessions.remove(session.getId());
        if (clientSession != null) {
            System.out.println("[SERVER] Ngắt kết nối: " + clientSession.getDisplayName()
                    + " (code=" + status.getCode() + ", reason=" + status.getReason() + ")");
            gameService.handleDisconnect(clientSession);
        }
    }

    /**
     * Xử lý lỗi WebSocket.
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("[SERVER] Lỗi WebSocket: " + exception.getMessage());
        ClientSession clientSession = sessions.get(session.getId());
        if (clientSession != null) {
            System.err.println("[SERVER] Lỗi từ client: " + clientSession.getDisplayName());
        }
    }

    /**
     * Phân loại và chuyển tiếp thông điệp cho GameService xử lý.
     * Tương đương switch-case trong ClientHandler.handleMessage() phiên bản cũ.
     */
    private void dispatchMessage(ClientSession handler, Message message) {
        switch (message.getType()) {
            // === Xác thực ===
            case AUTH:
                gameService.handleAuth(handler, message);
                break;

            // === Phòng chơi ===
            case CREATE_ROOM:
                gameService.handleCreateRoom(handler, message);
                break;
            case JOIN_ROOM:
                gameService.handleJoinRoom(handler, message);
                break;
            case LEAVE_ROOM:
                gameService.handleLeaveRoom(handler, message);
                break;
            case READY:
                gameService.handleReady(handler, message);
                break;
            case ROOM_UPDATE_SETTINGS:
                gameService.handleRoomUpdateSettings(handler, message);
                break;
            case ROOM_KICK_PLAYER:
                gameService.handleRoomKickPlayer(handler, message);
                break;
            case ROOM_CHAT_SEND:
                gameService.handleRoomChatSend(handler, message);
                break;

            // === Luồng trò chơi ===
            case TOPIC_SELECT:
                gameService.handleTopicSelect(handler, message);
                break;
            case DRAW_DATA:
                gameService.handleDrawData(handler, message);
                break;
            case DRAW_SUBMIT:
                gameService.handleDrawSubmit(handler, message);
                break;
            case GUESS_SUBMIT:
                gameService.handleGuessSubmit(handler, message);
                break;



            // === Admin ===
            case ADMIN_GET_USERS:
                gameService.handleAdminGetUsers(handler, message);
                break;
            case ADMIN_UPDATE_SCORE:
                gameService.handleAdminUpdateScore(handler, message);
                break;
            case ADMIN_DISSOLVE_ROOM:
                gameService.handleAdminDissolveRoom(handler, message);
                break;
            case ADMIN_GET_ROOMS:
                gameService.sendRoomList(handler);
                break;



            // === Mời vào phòng ===
            case ROOM_INVITE_SEND:
                gameService.handleRoomInviteSend(handler, message);
                break;
            case ROOM_INVITE_RESPOND:
                gameService.handleRoomInviteRespond(handler, message);
                break;

            // === Nhắn tin riêng ===
            case DIRECT_MESSAGE_SEND:
                gameService.handleDirectMessageSend(handler, message);
                break;
            case CHAT_HISTORY_REQUEST:
                gameService.handleChatHistoryRequest(handler, message);
                break;

            default:
                System.out.println("[SERVER] Loại thông điệp không xác định: " + message.getType());
                break;
        }
    }
}
