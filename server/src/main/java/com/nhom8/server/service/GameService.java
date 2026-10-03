package com.nhom8.server.service;


import org.springframework.stereotype.Service;

import com.nhom8.server.game.Room;
import com.nhom8.server.game.RoomManager;
import com.nhom8.server.model.Friendship;
import com.nhom8.server.model.User;
import com.nhom8.common.message.Envelope;
import com.nhom8.common.message.MessageType;
import com.nhom8.server.repository.FriendshipRepository;
import com.nhom8.server.repository.MatchRepository;
import com.nhom8.server.repository.MatchResultRepository;
import com.nhom8.server.repository.PaintingRepository;
import com.nhom8.server.repository.UserRepository;
import com.nhom8.server.ws.ClientSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lớp điều khiển trung tâm (Controller/Service) phía Server của trò chơi "Vẽ Hình Đoán Ý".
 * Thay thế ServerControl trong phiên bản cũ, nay được Spring quản lý (@Service).
 *
 * Thay đổi so với phiên bản cũ:
 * - ClientHandler → ClientSession
 * - UserDAO, MatchDAO... → UserRepository, MatchRepository... (Spring Data JPA, @Autowired)
 * - new RoomManager(this) → vẫn giữ pattern cũ vì RoomManager cần tham chiếu ngược
 *
 * Logic nghiệp vụ giữ nguyên 100%.
 */
@Service
public class GameService {

    /** Bảng ánh xạ username → ClientSession cho tất cả người chơi đang trực tuyến */
    private final ConcurrentHashMap<String, ClientSession> onlineClients = new ConcurrentHashMap<>();

    /** Danh sách tất cả ClientSession đang kết nối (kể cả chưa đăng nhập) */
    private final List<ClientSession> allClients = Collections.synchronizedList(new ArrayList<>());

    /** Quản lý phòng chơi */
    private final RoomManager roomManager;

    // === Spring Data JPA Repositories (thay DAO) ===
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final MatchResultRepository matchResultRepository;
    private final PaintingRepository paintingRepository;
    private final FriendshipRepository friendshipRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.nhom8.server.repository.ChatMessageRepository chatMessageRepository;

    /**
     * Constructor injection — Spring tự động inject các Repository.
     */
    public GameService(UserRepository userRepository,
                       MatchRepository matchRepository,
                       MatchResultRepository matchResultRepository,
                       PaintingRepository paintingRepository,
                       FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.matchResultRepository = matchResultRepository;
        this.paintingRepository = paintingRepository;
        this.friendshipRepository = friendshipRepository;
        this.roomManager = new RoomManager(this);
    }

    /**
     * Thêm ClientSession mới vào danh sách kết nối.
     */
    public void addClient(ClientSession handler) {
        allClients.add(handler);
    }

    public int getOnlineCount() {
        return onlineClients.size();
    }

    public int getRoomCount() {
        return roomManager.getRoomCount();
    }

    // ========================================================================
    // XỬ LÝ XÁC THỰC TÀI KHOẢN
    // ========================================================================

    @org.springframework.beans.factory.annotation.Autowired
    private AuthService authService;

    public void handleAuth(ClientSession handler, Envelope request) {
        String username = request.get("username");
        String password = request.get("password");
        if (username == null || password == null) return;

        try {
            com.nhom8.common.message.payload.LoginResultPayload payload = authService.login(username, password);
            if (onlineClients.containsKey(username)) {
                // Đóng connection cũ
                ClientSession oldSession = onlineClients.get(username);
                oldSession.sendMessage(Envelope.error(MessageType.LOGIN_RESULT, "Đăng nhập ở nơi khác."));
                try {
                    oldSession.getSession().close(org.springframework.web.socket.CloseStatus.NORMAL);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }

            handler.setUsername(username);
            handler.setDisplayName(payload.getPlayer().getDisplayName());
            onlineClients.put(username, handler);
            System.out.println("[SERVER] User " + username + " đã kết nối WebSocket.");

            Envelope response = new Envelope(MessageType.LOGIN_RESULT);
            response.setSuccess(true);
            response.setContent("Đăng nhập thành công");
            response.put("player", payload.getPlayer());
            handler.sendMessage(response);

            broadcastPlayerList();
            sendRoomList(handler);
        } catch (Exception e) {
            handler.sendMessage(Envelope.error(MessageType.LOGIN_RESULT, e.getMessage()));
        }
    }

    public void handleRegister(ClientSession handler, Envelope request) {
        String username = request.get("username");
        String password = request.get("password");
        String displayName = request.get("displayName");
        try {
            authService.register(username, password, displayName);
            handler.sendMessage(Envelope.success(MessageType.REGISTER, "Đăng ký thành công!"));
        } catch (Exception e) {
            handler.sendMessage(Envelope.error(MessageType.REGISTER, e.getMessage()));
        }
    }

    // ========================================================================
    // XỬ LÝ PHÒNG CHƠI
    // ========================================================================

    public void handleCreateRoom(ClientSession handler, Envelope request) {
        String roomName = request.getOrDefault("roomName", "Phòng " + handler.getUsername());
        Room room = roomManager.createRoom(handler, roomName);

        if (room != null) {
            handler.setCurrentRoomId(room.getRoomId());
            Envelope response = Envelope.success(MessageType.ROOM_RESPONSE, "Tạo phòng thành công!");
            response.put("roomId", room.getRoomId());
            response.put("roomName", room.getRoomName());
            handler.sendMessage(response);

            broadcastRoomUpdate(room);
            broadcastPlayerList();
            broadcastRoomList();
        } else {
            handler.sendMessage(Envelope.error(MessageType.ROOM_RESPONSE,
                    "Không thể tạo phòng. Vui lòng thử lại."));
        }
    }

    public void handleJoinRoom(ClientSession handler, Envelope request) {
        String roomId = request.get("roomId");
        Room room = roomManager.getRoom(roomId);

        if (room == null) {
            handler.sendMessage(Envelope.error(MessageType.ROOM_RESPONSE, "Phòng không tồn tại."));
            return;
        }

        if (room.isFull()) {
            handler.sendMessage(Envelope.error(MessageType.ROOM_RESPONSE,
                    "Phòng đã đầy (tối đa 6 người)."));
            return;
        }

        if (room.isPlaying()) {
            handler.sendMessage(Envelope.error(MessageType.ROOM_RESPONSE,
                    "Phòng đang trong trận đấu, không thể tham gia."));
            return;
        }

        room.addPlayer(handler);
        handler.setCurrentRoomId(roomId);

        Envelope response = Envelope.success(MessageType.ROOM_RESPONSE, "Tham gia phòng thành công!");
        response.put("roomId", room.getRoomId());
        response.put("roomName", room.getRoomName());
        handler.sendMessage(response);

        broadcastRoomUpdate(room);
        broadcastPlayerList();
        broadcastRoomList();
    }

    public void handleLeaveRoom(ClientSession handler, Envelope request) {
        String roomId = handler.getCurrentRoomId();
        if (roomId == null) return;

        Room room = roomManager.getRoom(roomId);
        if (room == null) return;

        room.removePlayer(handler);
        handler.setCurrentRoomId(null);

        if (room.isPlaying() && room.getPlayerCount() < Room.MIN_PLAYERS) {
            dissolveRoom(room);
        } else if (room.getPlayerCount() == 0) {
            roomManager.removeRoom(roomId);
        } else {
            broadcastRoomUpdate(room);
        }

        broadcastPlayerList();
        broadcastRoomList();
    }

    public void handleReady(ClientSession handler, Envelope request) {
        String roomId = handler.getCurrentRoomId();
        if (roomId == null) return;

        Room room = roomManager.getRoom(roomId);
        if (room == null) return;

        boolean isReady = true;
        Object readyVal = request.getData() != null ? request.getData().get("isReady") : null;
        if (readyVal instanceof Boolean) {
            isReady = (Boolean) readyVal;
        }

        room.setPlayerReady(handler.getUsername(), isReady);
        broadcastRoomUpdate(room);

        if (room.getPlayerCount() >= Room.MIN_PLAYERS && room.allPlayersReady()) {
            room.getGameSession().startGame();
        }
    }

    public void handleRoomUpdateSettings(ClientSession handler, Envelope request) {
        String roomId = handler.getCurrentRoomId();
        if (roomId == null) return;
        Room room = roomManager.getRoom(roomId);
        if (room == null || !room.getHostUsername().equals(handler.getUsername())) return;

        if (request.getData() != null && request.getData().has("maxRounds")) {
            room.setMaxRounds(request.get("maxRounds"));
        }
        if (request.getData() != null && request.getData().has("drawTime")) {
            room.setDrawTime(request.get("drawTime"));
        }
        if (request.getData() != null && request.getData().has("language")) {
            room.setLanguage(request.get("language"));
        }
        
        Envelope updateSettings = new Envelope(MessageType.ROOM_UPDATE_SETTINGS);
        updateSettings.put("maxRounds", room.getMaxRounds());
        updateSettings.put("drawTime", room.getDrawTime());
        updateSettings.put("language", room.getLanguage());
        room.broadcast(updateSettings);
    }

    public void handleRoomKickPlayer(ClientSession handler, Envelope request) {
        String roomId = handler.getCurrentRoomId();
        if (roomId == null) return;
        Room room = roomManager.getRoom(roomId);
        if (room == null || !room.getHostUsername().equals(handler.getUsername())) return;

        String targetUsername = request.get("targetUsername");
        ClientSession targetSession = onlineClients.get(targetUsername);
        if (targetSession != null && room.getPlayerUsernames().contains(targetUsername)) {
            // Remove player from room
            room.removePlayer(targetSession);
            targetSession.setCurrentRoomId(null);

            // Send notification to the kicked player
            Envelope kickedMsg = new Envelope(MessageType.ROOM_DISSOLVED);
            kickedMsg.setContent("Bạn đã bị chủ phòng mời ra khỏi phòng.");
            targetSession.sendMessage(kickedMsg);

            // Update room
            broadcastRoomUpdate(room);
            broadcastPlayerList();
            broadcastRoomList();
        }
    }

    public void handleRoomChatSend(ClientSession handler, Envelope request) {
        String roomId = handler.getCurrentRoomId();
        if (roomId == null) return;
        Room room = roomManager.getRoom(roomId);
        if (room == null) return;

        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) return;

        Envelope chatMsg = new Envelope(MessageType.ROOM_CHAT_RECEIVE);
        chatMsg.put("senderUsername", handler.getUsername());
        chatMsg.put("senderDisplayName", handler.getDisplayName());
        chatMsg.put("content", content);
        chatMsg.put("timestamp", new java.util.Date().toString());
        
        room.broadcast(chatMsg);
    }

    // ========================================================================
    // XỬ LÝ LUỒNG TRÒ CHƠI
    // ========================================================================

    public void handleTopicSelect(ClientSession handler, Envelope request) {
        String roomId = handler.getCurrentRoomId();
        if (roomId == null) return;
        Room room = roomManager.getRoom(roomId);
        if (room == null) return;
        String selectedTopic = request.get("topic");
        room.getGameSession().onTopicSelected(handler.getUsername(), selectedTopic);
    }

    public void handleDrawData(ClientSession handler, Envelope request) {
        String roomId = handler.getCurrentRoomId();
        if (roomId == null) return;
        Room room = roomManager.getRoom(roomId);
        if (room == null || !room.isPlaying()) return;
        
        room.getGameSession().onDrawingDataReceived(handler.getUsername(), request);
    }

    public void handleDrawSubmit(ClientSession handler, Envelope request) {
        String roomId = handler.getCurrentRoomId();
        if (roomId == null) return;
        Room room = roomManager.getRoom(roomId);
        if (room == null) return;
        room.getGameSession().onDrawingSubmitted(handler.getUsername(), request);
    }

    public void handleGuessSubmit(ClientSession handler, Envelope request) {
        String roomId = handler.getCurrentRoomId();
        if (roomId == null) return;
        Room room = roomManager.getRoom(roomId);
        if (room == null) return;
        String guess = request.getContent();
        room.getGameSession().onGuessSubmitted(handler.getUsername(), guess);
    }


    // ========================================================================
    // XỬ LÝ NHẮN TIN RIÊNG
    // ========================================================================

    public void handleDirectMessageSend(ClientSession handler, Envelope request) {
        String targetUsername = request.get("targetUsername");
        String content = request.get("content");
        String myUsername = handler.getUsername();

        if (myUsername == null || targetUsername == null || content == null || content.trim().isEmpty()) {
            return;
        }

        User me = userRepository.findByUsername(myUsername);
        User target = userRepository.findByUsername(targetUsername);
        if (me == null || target == null) return;

        // Lưu tin nhắn vào DB
        com.nhom8.server.model.ChatMessage chat = new com.nhom8.server.model.ChatMessage(me, target, content);
        chatMessageRepository.save(chat);

        // Chuẩn bị payload gửi đi
        Envelope msg = new Envelope(MessageType.DIRECT_MESSAGE_RECEIVE);
        msg.put("senderUsername", me.getUsername());
        msg.put("senderDisplayName", me.getDisplayName());
        msg.put("senderAvatar", me.getAvatar());
        msg.put("content", content);
        msg.put("timestamp", chat.getCreatedAt().toString());

        // Nếu người nhận đang online, gửi luôn
        ClientSession targetSession = onlineClients.get(targetUsername);
        if (targetSession != null) {
            targetSession.sendMessage(msg);
        }

        // Gửi thông báo thành công về cho người gửi (để họ add vào giao diện)
        Envelope ack = new Envelope(MessageType.DIRECT_MESSAGE_RECEIVE);
        ack.put("senderUsername", me.getUsername());
        ack.put("senderDisplayName", me.getDisplayName());
        ack.put("senderAvatar", me.getAvatar());
        ack.put("content", content);
        ack.put("timestamp", chat.getCreatedAt().toString());
        ack.put("isSelf", true);
        ack.put("targetUsername", targetUsername);
        handler.sendMessage(ack);
    }

    public void handleChatHistoryRequest(ClientSession handler, Envelope request) {
        String targetUsername = request.get("targetUsername");
        String myUsername = handler.getUsername();

        if (myUsername == null || targetUsername == null) return;

        User me = userRepository.findByUsername(myUsername);
        User target = userRepository.findByUsername(targetUsername);
        if (me == null || target == null) return;

        java.util.List<com.nhom8.server.model.ChatMessage> history = chatMessageRepository.findChatHistory(me, target);
        java.util.List<java.util.Map<String, Object>> responseList = new java.util.ArrayList<>();

        for (com.nhom8.server.model.ChatMessage c : history) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("senderUsername", c.getSender().getUsername());
            map.put("senderDisplayName", c.getSender().getDisplayName());
            map.put("content", c.getContent());
            map.put("timestamp", c.getCreatedAt().toString());
            map.put("isSelf", c.getSender().getUsername().equals(myUsername));
            responseList.add(map);
        }

        Envelope response = new Envelope(MessageType.CHAT_HISTORY_RESPONSE);
        response.put("targetUsername", targetUsername);
        response.put("history", responseList);
        handler.sendMessage(response);
    }

    // ========================================================================
    // XỬ LÝ ADMIN
    // ========================================================================

    private boolean isAdmin(ClientSession handler) {
        String username = handler.getUsername();
        if (username == null) return false;
        User user = userRepository.findByUsername(username);
        return user != null && "ADMIN".equals(user.getRole());
    }

    public void handleAdminGetUsers(ClientSession handler, Envelope request) {
        if (!isAdmin(handler)) return;
        List<User> allUsers = userRepository.findAllByOrderByRankingScoreDesc();
        List<Map<String, Object>> usersData = new ArrayList<>();
        for (User u : allUsers) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getUserId());
            map.put("username", u.getUsername());
            map.put("rankingScore", u.getRankingScore());
            map.put("role", u.getRole());
            usersData.add(map);
        }
        Envelope response = Envelope.success(MessageType.ADMIN_USERS_RESPONSE, "Success");
        response.put("users", usersData);
        handler.sendMessage(response);
    }

    public void handleAdminUpdateScore(ClientSession handler, Envelope request) {
        if (!isAdmin(handler)) return;
        String targetUser = request.get("targetUser");
        int newScore = request.get("newScore");

        User user = userRepository.findByUsername(targetUser);
        if (user != null) {
            user.setRankingScore(newScore);
            userRepository.save(user);
            handler.sendMessage(Envelope.success(MessageType.ADMIN_UPDATE_SCORE,
                    "Cập nhật điểm thành công cho " + targetUser));
            broadcastPlayerList();
        }
    }

    public void handleAdminDissolveRoom(ClientSession handler, Envelope request) {
        if (!isAdmin(handler)) return;
        String roomId = request.get("roomId");
        Room room = roomManager.getRoom(roomId);
        if (room != null) {
            dissolveRoom(room);
            handler.sendMessage(Envelope.success(MessageType.ADMIN_DISSOLVE_ROOM,
                    "Đã giải tán phòng " + roomId));
        }
    }

    // ========================================================================
    // XỬ LÝ HỆ THỐNG BẠN BÈ
    // ========================================================================

    public boolean isUserOnline(String username) {
        return onlineClients.containsKey(username);
    }

    public void notifyFriendRequestViaWS(User receiver, User sender) {
        ClientSession receiverSession = onlineClients.get(receiver.getUsername());
        if (receiverSession != null) {
            Envelope notify = new Envelope(MessageType.FRIEND_REQUEST_NOTIFY);
            notify.setSuccess(true);
            notify.put("senderUsername", sender.getUsername());
            notify.put("senderDisplayName", sender.getDisplayName());
            notify.put("senderAvatar", sender.getAvatar());
            notify.put("senderScore", sender.getRankingScore());
            notify.setContent(sender.getDisplayName() + " muốn kết bạn với bạn!");
            receiverSession.sendMessage(notify);
        }
    }

    public void notifyFriendAcceptViaWS(User receiver, User accepter) {
        ClientSession receiverSession = onlineClients.get(receiver.getUsername());
        if (receiverSession != null) {
            Envelope notify = new Envelope(MessageType.FRIEND_UPDATE);
            notify.setSuccess(true);
            notify.put("action", "ACCEPTED");
            notify.put("username", accepter.getUsername());
            notify.put("displayName", accepter.getDisplayName());
            notify.put("avatar", accepter.getAvatar());
            notify.setContent(accepter.getDisplayName() + " đã chấp nhận lời mời kết bạn!");
            receiverSession.sendMessage(notify);
        }
    }

    public void notifyFriendRemoveViaWS(User receiver, User remover) {
        ClientSession receiverSession = onlineClients.get(receiver.getUsername());
        if (receiverSession != null) {
            Envelope notify = new Envelope(MessageType.FRIEND_UPDATE);
            notify.setSuccess(true);
            notify.put("action", "REMOVED");
            notify.put("username", remover.getUsername());
            notify.setContent("Danh sách bạn bè đã thay đổi.");
            receiverSession.sendMessage(notify);
        }
    }

    // ========================================================================
    // XỬ LÝ MỜI VÀO PHÒNG
    // ========================================================================

    /**
     * Xử lý yêu cầu mời bạn bè vào phòng chơi.
     * Chỉ có thể mời người là bạn bè và đang online.
     */
    public void handleRoomInviteSend(ClientSession handler, Envelope request) {
        String targetUsername = request.get("targetUsername");
        String myUsername = handler.getUsername();
        String roomId = handler.getCurrentRoomId();

        if (myUsername == null || targetUsername == null || roomId == null) {
            handler.sendMessage(Envelope.error(MessageType.FRIEND_UPDATE,
                    "Bạn chưa ở trong phòng chơi nào."));
            return;
        }

        User me = userRepository.findByUsername(myUsername);
        User target = userRepository.findByUsername(targetUsername);
        if (me == null || target == null) return;

        // Kiểm tra là bạn bè
        Friendship friendship = friendshipRepository.findByUsers(me, target);
        if (friendship == null || friendship.getStatus() != Friendship.FriendshipStatus.ACCEPTED) {
            handler.sendMessage(Envelope.error(MessageType.FRIEND_UPDATE,
                    "Bạn chỉ có thể mời bạn bè vào phòng."));
            return;
        }

        ClientSession targetSession = onlineClients.get(targetUsername);
        if (targetSession == null) {
            handler.sendMessage(Envelope.error(MessageType.FRIEND_UPDATE,
                    targetUsername + " đang offline."));
            return;
        }

        if (targetSession.getCurrentRoomId() != null) {
            handler.sendMessage(Envelope.error(MessageType.FRIEND_UPDATE,
                    targetUsername + " đang ở trong phòng khác."));
            return;
        }

        Room room = roomManager.getRoom(roomId);
        if (room == null) return;

        // Gửi lời mời tới bạn bè
        Envelope invite = new Envelope(MessageType.ROOM_INVITE_NOTIFY);
        invite.put("roomId", roomId);
        invite.put("roomName", room.getRoomName());
        invite.put("inviterUsername", myUsername);
        invite.put("inviterAvatar", me.getAvatar());
        invite.setContent(myUsername + " mời bạn vào phòng " + room.getRoomName() + "!");
        targetSession.sendMessage(invite);

        handler.sendMessage(Envelope.success(MessageType.FRIEND_UPDATE,
                "Đã gửi lời mời vào phòng cho " + targetUsername + "."));
    }

    /**
     * Xử lý phản hồi lời mời vào phòng (chấp nhận hoặc từ chối).
     */
    public void handleRoomInviteRespond(ClientSession handler, Envelope request) {
        boolean accept = Boolean.TRUE.equals(request.get("accept"));
        String roomId = request.get("roomId");

        if (!accept || roomId == null) return;

        // Chấp nhận → tự động join room
        Envelope joinRequest = new Envelope(MessageType.JOIN_ROOM);
        joinRequest.put("roomId", roomId);
        handleJoinRoom(handler, joinRequest);
    }

    // ========================================================================
    // XỬ LÝ NGẮT KẾT NỐI
    // ========================================================================

    public void handleDisconnect(ClientSession handler) {
        String username = handler.getUsername();
        if (username != null) {
            onlineClients.remove(username);

            String roomId = handler.getCurrentRoomId();
            if (roomId != null) {
                Room room = roomManager.getRoom(roomId);
                if (room != null) {
                    room.removePlayer(handler);

                    Envelope disconnectMsg = new Envelope(MessageType.PLAYER_DISCONNECTED);
                    disconnectMsg.put("username", username);
                    room.broadcast(disconnectMsg);

                    if (room.isPlaying() && room.getPlayerCount() < Room.MIN_PLAYERS) {
                        dissolveRoom(room);
                    } else if (room.getPlayerCount() == 0) {
                        roomManager.removeRoom(roomId);
                    } else {
                        broadcastRoomUpdate(room);
                    }
                }
            }
        }

        allClients.remove(handler);
        broadcastPlayerList();
        broadcastRoomList();
    }

    // ========================================================================
    // BROADCAST
    // ========================================================================

    public void broadcastPlayerList() {
        List<Map<String, Object>> playerList = new ArrayList<>();
        for (Map.Entry<String, ClientSession> entry : onlineClients.entrySet()) {
            Map<String, Object> playerInfo = new HashMap<>();
            playerInfo.put("username", entry.getKey());
            playerInfo.put("status", entry.getValue().getCurrentRoomId() != null ? "Đang bận" : "Đang rỗi");

            User user = userRepository.findByUsername(entry.getKey());
            playerInfo.put("rankingScore", user != null ? user.getRankingScore() : 0);
            playerInfo.put("avatar", user != null ? user.getAvatar() : "1");

            playerList.add(playerInfo);
        }

        Envelope msg = new Envelope(MessageType.PLAYER_LIST);
        msg.put("players", playerList);

        for (ClientSession handler : onlineClients.values()) {
            handler.sendMessage(msg);
        }
    }

    public void sendRoomList(ClientSession handler) {
        Envelope msg = new Envelope(MessageType.ROOM_LIST);
        msg.put("rooms", roomManager.getRoomListData());
        handler.sendMessage(msg);
    }

    public void broadcastRoomList() {
        Envelope msg = new Envelope(MessageType.ROOM_LIST);
        msg.put("rooms", roomManager.getRoomListData());

        for (ClientSession handler : onlineClients.values()) {
            handler.sendMessage(msg);
        }
    }

    public void broadcastRoomUpdate(Room room) {
        Envelope msg = new Envelope(MessageType.ROOM_UPDATE);
        msg.put("roomId", room.getRoomId());
        msg.put("roomName", room.getRoomName());
        msg.put("players", room.getPlayerListData());
        msg.put("playerCount", room.getPlayerCount());
        msg.put("status", room.getStatus().name());

        room.broadcast(msg);
    }

    private void dissolveRoom(Room room) {
        Envelope dissolveMsg = new Envelope(MessageType.ROOM_DISSOLVED);
        dissolveMsg.setContent("Phòng đã bị giải tán do không đủ người chơi (tối thiểu "
                + Room.MIN_PLAYERS + " người).");
        room.broadcast(dissolveMsg);

        for (ClientSession handler : room.getPlayers()) {
            handler.setCurrentRoomId(null);
        }

        room.getGameSession().endGame();
        roomManager.removeRoom(room.getRoomId());

        broadcastPlayerList();
        broadcastRoomList();
    }

    // === Getters ===

    public ConcurrentHashMap<String, ClientSession> getOnlineClients() { return onlineClients; }
    public ClientSession getClientSession(String username) { return onlineClients.get(username); }
    public RoomManager getRoomManager() { return roomManager; }
    public UserRepository getUserRepository() { return userRepository; }
    public MatchRepository getMatchRepository() { return matchRepository; }
    public MatchResultRepository getMatchResultRepository() { return matchResultRepository; }
    public PaintingRepository getPaintingRepository() { return paintingRepository; }
    public FriendshipRepository getFriendshipRepository() { return friendshipRepository; }
}
