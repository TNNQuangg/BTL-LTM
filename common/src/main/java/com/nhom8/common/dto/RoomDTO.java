package com.nhom8.common.dto;

import java.util.List;

public class RoomDTO {
    private String roomId;
    private String name;
    private int currentPlayers;
    private int maxPlayers;
    private String status; // "WAITING", "PLAYING"
    private String hostUsername;
    private List<PlayerDTO> players;

    public RoomDTO() {}

    public RoomDTO(String roomId, String name, int currentPlayers, int maxPlayers, String status, String hostUsername, List<PlayerDTO> players) {
        this.roomId = roomId;
        this.name = name;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
        this.status = status;
        this.hostUsername = hostUsername;
        this.players = players;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCurrentPlayers() { return currentPlayers; }
    public void setCurrentPlayers(int currentPlayers) { this.currentPlayers = currentPlayers; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getHostUsername() { return hostUsername; }
    public void setHostUsername(String hostUsername) { this.hostUsername = hostUsername; }

    public List<PlayerDTO> getPlayers() { return players; }
    public void setPlayers(List<PlayerDTO> players) { this.players = players; }
}
