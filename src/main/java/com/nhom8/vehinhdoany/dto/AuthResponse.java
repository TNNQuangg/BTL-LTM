package com.nhom8.vehinhdoany.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class AuthResponse {
    private String message;
    private Integer userId;
    private String username;
    private String displayName;
    private Integer rankingScore;
    private String role;
    private String avatar;
    private String token ;
    private String tokenType = "Bearer";

    public AuthResponse() {}

    public AuthResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Integer getRankingScore() { return rankingScore; }
    public void setRankingScore(Integer rankingScore) { this.rankingScore = rankingScore; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
