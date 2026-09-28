package com.roomchatapps.Pmishra.models;

import com.google.firebase.database.PropertyName;

public class Room {
    private String roomId;
    private String room_name;
    private String uid;
    private String hostId;
    private String img;
    private int onlineCount;
    private int memberCount;
    private boolean chatEnabled = true;
    private boolean giftEnabled = true;
    private boolean spinEnabled = true;
    private boolean luckySpinEnabled = true;
    private long createdAt;
    private long updatedAt;

    public Room() {}

    public Room(String roomId, String room_name, String uid, String img) {
        this.roomId = roomId;
        this.room_name = room_name;
        this.uid = uid;
        this.hostId = uid;
        this.img = img;
        this.onlineCount = 0;
        this.memberCount = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @PropertyName("roomId")
    public String getRoomId() { return roomId; }
    @PropertyName("roomId")
    public void setRoomId(String roomId) { this.roomId = roomId; }

    @PropertyName("room_name")
    public String getRoomName() { return room_name; }
    @PropertyName("room_name")
    public void setRoomName(String room_name) { this.room_name = room_name; }

    @PropertyName("uid")
    public String getUid() { return uid; }
    @PropertyName("uid")
    public void setUid(String uid) { this.uid = uid; }

    @PropertyName("hostId")
    public String getHostId() { return hostId != null ? hostId : uid; }
    @PropertyName("hostId")
    public void setHostId(String hostId) { this.hostId = hostId; }

    @PropertyName("img")
    public String getImg() { return img; }
    @PropertyName("img")
    public void setImg(String img) { this.img = img; }

    public int getOnlineCount() { return onlineCount; }
    public void setOnlineCount(int onlineCount) { this.onlineCount = onlineCount; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public boolean isChatEnabled() { return chatEnabled; }
    public void setChatEnabled(boolean chatEnabled) { this.chatEnabled = chatEnabled; }

    public boolean isGiftEnabled() { return giftEnabled; }
    public void setGiftEnabled(boolean giftEnabled) { this.giftEnabled = giftEnabled; }

    public boolean isSpinEnabled() { return spinEnabled; }
    public void setSpinEnabled(boolean spinEnabled) { this.spinEnabled = spinEnabled; }

    public boolean isLuckySpinEnabled() { return luckySpinEnabled; }
    public void setLuckySpinEnabled(boolean luckySpinEnabled) { this.luckySpinEnabled = luckySpinEnabled; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
