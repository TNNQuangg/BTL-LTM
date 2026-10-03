package com.nhom8.server.game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.nhom8.server.service.GameService;
import com.nhom8.server.ws.ClientSession;

/**
 * Lớp quản lý toàn bộ phòng chơi trong hệ thống trò chơi "Vẽ Hình Đoán Ý".
 *
 * Thay đổi so với phiên bản cũ:
 * - ServerControl → GameService
 * - ClientHandler → ClientSession
 */
public class RoomManager {

    private ConcurrentHashMap<String, Room> rooms;
    private int roomCounter;
    private GameService gameService;

    public RoomManager(GameService gameService) {
        this.rooms = new ConcurrentHashMap<>();
        this.roomCounter = 0;
        this.gameService = gameService;
    }

    public synchronized Room createRoom(ClientSession host, String roomName) {
        String roomId;
        Random random = new Random();
        do {
            roomId = String.format("%06d", random.nextInt(1000000));
        } while (rooms.containsKey(roomId));

        Room room = new Room(roomId, roomName, host, gameService);
        rooms.put(roomId, room);
        return room;
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public void removeRoom(String roomId) {
        rooms.remove(roomId);
    }

    public ArrayList<HashMap<String, Object>> getRoomListData() {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        for (Room room : rooms.values()) {
            HashMap<String, Object> info = new HashMap<>();
            info.put("roomId", room.getRoomId());
            info.put("roomName", room.getRoomName());
            info.put("host", room.getHostUsername());
            
            ClientSession hostSession = room.getPlayer(room.getHostUsername());
            if (hostSession != null) {
                info.put("hostDisplayName", hostSession.getDisplayName());
            } else {
                info.put("hostDisplayName", room.getHostUsername());
            }
            
            info.put("playerCount", room.getPlayerCount());
            info.put("maxPlayers", Room.MAX_PLAYERS);
            info.put("status", room.getStatus().name());
            list.add(info);
        }
        return list;
    }

    public int getRoomCount() { return rooms.size(); }
    public GameService getGameService() { return gameService; }
}
