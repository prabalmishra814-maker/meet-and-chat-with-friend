package com.roomchatapps.Pmishra.models;

import com.google.firebase.database.PropertyName;

public class ChatMessage {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String message;
    private String senderAvatar;
    private int giftIconRes;
    private long timestamp;
    private boolean read;

    public ChatMessage() {
        // Required for Firebase
    }

    public ChatMessage(String senderId, String receiverId, String message, long timestamp) {
        this(senderId, receiverId, message, timestamp, false);
    }

    public ChatMessage(String senderId, String receiverId, String message, long timestamp, boolean read) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("read")
    public boolean isRead() {
        return read;
    }

    @PropertyName("read")
    public void setRead(boolean read) {
        this.read = read;
    }

    public int getGiftIconRes() {
        return giftIconRes;
    }

    public void setGiftIconRes(int giftIconRes) {
        this.giftIconRes = giftIconRes;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
