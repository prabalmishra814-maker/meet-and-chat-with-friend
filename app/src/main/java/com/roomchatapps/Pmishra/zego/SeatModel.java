package com.roomchatapps.Pmishra.zego;

public class SeatModel {
    public int index;
    public String userID = "";
    public String userName = "";
    public String userAvatar = "";
    public String equippedFrame = "";
    public boolean isMicOn = true;
    public boolean isMuted = false;
    public boolean isClosed = false;
    public boolean isSpeaking = false;
    public float soundLevel = 0f;

    public SeatModel() {
    }

    public SeatModel(int index) {
        this.index = index;
    }

    public boolean isEmpty() {
        return userID == null || userID.trim().isEmpty();
    }

    public boolean isHost() {
        return index == 0;
    }

    public void clear() {
        this.userID = "";
        this.userName = "";
        this.userAvatar = "";
        this.equippedFrame = "";
        this.isMicOn = true;
        this.isMuted = false;
        this.isSpeaking = false;
        this.soundLevel = 0f;
    }
}
