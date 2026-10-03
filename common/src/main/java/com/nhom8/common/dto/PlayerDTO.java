package com.nhom8.common.dto;

public class PlayerDTO {
    private String username;
    private String displayName;
    private String avatar;
    private int rankingScore;
    private String status; // "ONLINE", "IN_ROOM", "PLAYING"
    private boolean isReady;
    
    public PlayerDTO() {}

    public PlayerDTO(String username, String displayName, String avatar, int rankingScore, String status, boolean isReady) {
        this.username = username;
        this.displayName = displayName;
        this.avatar = avatar;
        this.rankingScore = rankingScore;
        this.status = status;
        this.isReady = isReady;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public int getRankingScore() { return rankingScore; }
    public void setRankingScore(int rankingScore) { this.rankingScore = rankingScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isReady() { return isReady; }
    public void setReady(boolean isReady) { this.isReady = isReady; }
}
