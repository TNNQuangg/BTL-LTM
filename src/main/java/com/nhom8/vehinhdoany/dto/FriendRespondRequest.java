package com.nhom8.vehinhdoany.dto;

public class FriendRespondRequest {
    private String myUsername;
    private String senderUsername;
    private boolean accept;

    public String getMyUsername() { return myUsername; }
    public void setMyUsername(String myUsername) { this.myUsername = myUsername; }
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public boolean isAccept() { return accept; }
    public void setAccept(boolean accept) { this.accept = accept; }
}
