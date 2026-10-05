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
    public boolean wasOriginallyClosed = false;
    public boolean isSpeaking = false;
    public float soundLevel = 0f;

    public SeatModel() {
    }

    public SeatModel(int index) {
        this.index = index;
    }

    public SeatModel(SeatModel other) {
        if (other != null) {
            this.index = other.index;
            this.userID = other.userID != null ? other.userID : "";
            this.userName = other.userName != null ? other.userName : "";
            this.userAvatar = other.userAvatar != null ? other.userAvatar : "";
            this.equippedFrame = other.equippedFrame != null ? other.equippedFrame : "";
            this.isMicOn = other.isMicOn;
            this.isMuted = other.isMuted;
            this.isClosed = other.isClosed;
            this.wasOriginallyClosed = other.wasOriginallyClosed;
            this.isSpeaking = other.isSpeaking;
            this.soundLevel = other.soundLevel;
        }
    }

    public boolean isEmpty() {
        return userID == null || userID.trim().isEmpty();
    }

    public boolean isHost() {
        if (userID == null || userID.trim().isEmpty()) return false;
        String hostUid = SeatManager.getInstance().getHostUserID();
        return hostUid != null && !hostUid.isEmpty() && hostUid.equals(userID);
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
        if (this.wasOriginallyClosed) {
            this.isClosed = true;
            this.wasOriginallyClosed = false;
        }
    }
}
