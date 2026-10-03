package com.nhom8.common.message.payload;

import com.nhom8.common.dto.PlayerDTO;

public class LoginResultPayload {
    private PlayerDTO player;
    private String token; // Optional if we still want to keep token in some form, or can remove

    public LoginResultPayload() {}

    public LoginResultPayload(PlayerDTO player, String token) {
        this.player = player;
        this.token = token;
    }

    public PlayerDTO getPlayer() { return player; }
    public void setPlayer(PlayerDTO player) { this.player = player; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
