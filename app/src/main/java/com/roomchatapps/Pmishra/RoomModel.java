package com.roomchatapps.Pmishra;

import androidx.annotation.Keep;

/**
 * Model class for Room data stored in Firebase Realtime Database.
 */
@Keep
public class RoomModel {
    private String roomId;
    private String room_name;
    private String img;
    private String uid;

    // Required empty constructor for Firebase
    public RoomModel() {
    }

    public RoomModel(String roomId, String room_name, String img, String uid) {
        this.roomId = roomId;
        this.room_name = room_name;
        this.img = img;
        this.uid = uid;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
