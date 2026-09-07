package com.roomchatapps.Pmishra.models;

public class FriendRequestModel {
    private String senderUid;
    private String senderName;
    private String senderAvatar;
    private long timestamp;
    private String status; // PENDING, ACCEPTED, REJECTED

    public FriendRequestModel() {}

    public FriendRequestModel(String senderUid, String senderName, String senderAvatar, long timestamp, String status) {
        this.senderUid = senderUid;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getSenderUid() { return senderUid; }
    public void setSenderUid(String senderUid) { this.senderUid = senderUid; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
