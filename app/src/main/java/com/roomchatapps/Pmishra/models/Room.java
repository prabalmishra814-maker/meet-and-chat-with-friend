package com.roomchatapps.Pmishra.models;

import com.google.firebase.database.PropertyName;

public class Room {
    private String roomId;
    private String room_name;
    private String uid;
    private String img;
    private int onlineCount;

    public Room() {}

    public Room(String roomId, String room_name, String uid, String img) {
        this.roomId = roomId;
        this.room_name = room_name;
        this.uid = uid;
        this.img = img;
        this.onlineCount = 0;
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

    @PropertyName("img")
    public String getImg() { return img; }
    @PropertyName("img")
    public void setImg(String img) { this.img = img; }

    public int getOnlineCount() { return onlineCount; }
    public void setOnlineCount(int onlineCount) { this.onlineCount = onlineCount; }
}
