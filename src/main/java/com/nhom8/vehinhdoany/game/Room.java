package com.nhom8.vehinhdoany.game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.nhom8.vehinhdoany.protocol.Message;
import com.nhom8.vehinhdoany.service.GameService;
import com.nhom8.vehinhdoany.websocket.ClientSession;

/**
 * Lớp đại diện cho một phòng chơi trong trò chơi "Vẽ Hình Đoán Ý".
 * Quản lý danh sách người chơi, trạng thái sẵn sàng, và phiên chơi (GameSession).
 *
 * Thay đổi so với phiên bản cũ:
 * - ClientHandler → ClientSession
 * - ServerControl → GameService
 */
public class Room {

    public enum RoomStatus {
        WAITING, PLAYING, FINISHED
    }

    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 6;

    private String roomId;
    private String roomName;
    private String hostUsername;
    private RoomStatus status;
    private ConcurrentHashMap<String, ClientSession> players;
    private ConcurrentHashMap<String, Boolean> readyStatus;
    private GameSession gameSession;

    // Room Settings
    private int maxRounds = 3;
    private int drawTime = 60;
    private String language = "vi";

    public Room(String roomId, String roomName, ClientSession host, GameService gameService) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.hostUsername = host.getUsername();
        this.status = RoomStatus.WAITING;
        this.players = new ConcurrentHashMap<>();
        this.readyStatus = new ConcurrentHashMap<>();
        this.gameSession = new GameSession(this);
        this.gameSession.setGameService(gameService);

        addPlayer(host);
    }

    public synchronized void addPlayer(ClientSession handler) {
        if (players.size() < MAX_PLAYERS) {
            players.put(handler.getUsername(), handler);
            readyStatus.put(handler.getUsername(), false);
        }
    }

    public synchronized void removePlayer(ClientSession handler) {
        players.remove(handler.getUsername());
        readyStatus.remove(handler.getUsername());
        if (handler.getUsername().equals(hostUsername) && !players.isEmpty()) {
            hostUsername = players.keys().nextElement();
        }
    }

    public void setPlayerReady(String username, boolean ready) {
        readyStatus.put(username, ready);
    }

    public boolean allPlayersReady() {
        if (players.size() < MIN_PLAYERS) return false;
        for (Boolean ready : readyStatus.values()) {
            if (!ready) return false;
        }
        return true;
    }

    public void broadcast(Message message) {
        for (ClientSession handler : players.values()) {
            handler.sendMessage(message);
        }
    }

    public void broadcastExcept(Message message, String exceptUsername) {
        for (Map.Entry<String, ClientSession> entry : players.entrySet()) {
            if (!entry.getKey().equals(exceptUsername)) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    public ArrayList<HashMap<String, Object>> getPlayerListData() {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, ClientSession> entry : players.entrySet()) {
            HashMap<String, Object> info = new HashMap<>();
            info.put("username", entry.getKey());
            info.put("displayName", entry.getValue().getDisplayName());
            info.put("isReady", readyStatus.getOrDefault(entry.getKey(), false));
            info.put("isHost", entry.getKey().equals(hostUsername));
            list.add(info);
        }
        return list;
    }

    public String getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public String getHostUsername() { return hostUsername; }
    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public int getPlayerCount() { return players.size(); }
    public boolean isFull() { return players.size() >= MAX_PLAYERS; }
    public boolean isPlaying() { return status == RoomStatus.PLAYING; }
    public Collection<ClientSession> getPlayers() { return players.values(); }
    public ClientSession getPlayer(String username) { return players.get(username); }
    public Set<String> getPlayerUsernames() { return players.keySet(); }
    public GameSession getGameSession() { return gameSession; }

    public int getMaxRounds() { return maxRounds; }
    public void setMaxRounds(int maxRounds) { this.maxRounds = maxRounds; }
    public int getDrawTime() { return drawTime; }
    public void setDrawTime(int drawTime) { this.drawTime = drawTime; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public void resetReadyStatus() {
        for (String username : readyStatus.keySet()) {
            readyStatus.put(username, false);
        }
    }
}
