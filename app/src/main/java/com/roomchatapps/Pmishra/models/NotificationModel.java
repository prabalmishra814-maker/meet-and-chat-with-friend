package com.roomchatapps.Pmishra.models;

public class NotificationModel {
    private String id;
    private String title;
    private String message;
    private String type; // "SYSTEM", "GIFT", "FOLLOW", "ROOM_INVITE", "COMMENT"
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private long timestamp;
    private boolean isRead;
    private String targetId;

    public NotificationModel() {}

    public NotificationModel(String id, String title, String message, String type, String senderId, String senderName, String senderAvatar, long timestamp, boolean isRead, String targetId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.targetId = targetId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
}
