package com.nhom8.server.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nhom8.common.message.Envelope;
import com.nhom8.server.service.GameService;
import com.nhom8.server.util.JsonUtil;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring WebSocket Handler — điểm tiếp nhận kết nối WebSocket từ JavaFX Client.
 */
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    /** Bảng ánh xạ sessionId → ClientSession */
    private final ConcurrentHashMap<String, ClientSession> sessions = new ConcurrentHashMap<>();

    /** Service xử lý logic nghiệp vụ */
    private final GameService gameService;

    public GameWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String address = session.getRemoteAddress() != null
                ? session.getRemoteAddress().toString() : session.getId();
        System.out.println("[SERVER] Kết nối mới từ: " + address);

        ClientSession clientSession = new ClientSession(session);
        sessions.put(session.getId(), clientSession);
        gameService.addClient(clientSession);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        ClientSession clientSession = sessions.get(session.getId());
        if (clientSession == null) return;

        try {
            Envelope envelope = JsonUtil.fromJson(textMessage.getPayload(), Envelope.class);
            if (envelope != null && envelope.getType() != null) {
                System.out.println("[SERVER] Nhận từ " + clientSession.getDisplayName() + ": " + envelope.getType());
                dispatchMessage(clientSession, envelope);
            }
        } catch (Exception e) {
            System.err.println("[SERVER] JSON không hợp lệ từ "
                    + clientSession.getDisplayName() + ": " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        ClientSession clientSession = sessions.remove(session.getId());
        if (clientSession != null) {
            System.out.println("[SERVER] Ngắt kết nối: " + clientSession.getDisplayName()
                    + " (code=" + status.getCode() + ", reason=" + status.getReason() + ")");
            gameService.handleDisconnect(clientSession);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("[SERVER] Lỗi WebSocket: " + exception.getMessage());
        ClientSession clientSession = sessions.get(session.getId());
        if (clientSession != null) {
            System.err.println("[SERVER] Lỗi từ client: " + clientSession.getDisplayName());
        }
    }

    private void dispatchMessage(ClientSession handler, Envelope envelope) {
        switch (envelope.getType()) {
            case LOGIN:
                gameService.handleAuth(handler, envelope);
                break;
            case REGISTER:
                gameService.handleRegister(handler, envelope);
                break;
            case ROOM_CREATE:
                gameService.handleCreateRoom(handler, envelope);
                break;
            case ROOM_JOIN:
                gameService.handleJoinRoom(handler, envelope);
                break;
            case ROOM_LEAVE:
                gameService.handleLeaveRoom(handler, envelope);
                break;
            case READY:
                gameService.handleReady(handler, envelope);
                break;
            case ROOM_SETTINGS:
                gameService.handleRoomUpdateSettings(handler, envelope);
                break;
            case ROOM_KICK:
                gameService.handleRoomKickPlayer(handler, envelope);
                break;
            case ROOM_CHAT:
                gameService.handleRoomChatSend(handler, envelope);
                break;
            case TOPIC_SELECT:
                gameService.handleTopicSelect(handler, envelope);
                break;
            case STROKE_BATCH:
                gameService.handleDrawData(handler, envelope);
                break;
            case DRAW_SUBMIT:
                gameService.handleDrawSubmit(handler, envelope);
                break;
            case GUESS_SUBMIT:
                gameService.handleGuessSubmit(handler, envelope);
                break;
            case ROOM_INVITE_SEND:
                gameService.handleRoomInviteSend(handler, envelope);
                break;
            case ROOM_INVITE_RESPOND:
                gameService.handleRoomInviteRespond(handler, envelope);
                break;
            case CHAT_PRIVATE:
                gameService.handleDirectMessageSend(handler, envelope);
                break;
            case CHAT_HISTORY:
                gameService.handleChatHistoryRequest(handler, envelope);
                break;
            default:
                System.out.println("[SERVER] Chưa hỗ trợ loại thông điệp: " + envelope.getType());
                break;
        }
    }
}
