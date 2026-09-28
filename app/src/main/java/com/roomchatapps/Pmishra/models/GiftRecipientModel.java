package com.roomchatapps.Pmishra.models;

public class GiftRecipientModel {
    private String uid;
    private String name;
    private String avatar;
    private String frame;
    private String seatBadge;
    private boolean isAll;
    private boolean isSelected;

    public GiftRecipientModel(String uid, String name, String avatar, String seatBadge, boolean isAll, boolean isSelected) {
        this(uid, name, avatar, "", seatBadge, isAll, isSelected);
    }

    public GiftRecipientModel(String uid, String name, String avatar, String frame, String seatBadge, boolean isAll, boolean isSelected) {
        this.uid = uid;
        this.name = name;
        this.avatar = avatar;
        this.frame = frame != null ? frame : "";
        this.seatBadge = seatBadge;
        this.isAll = isAll;
        this.isSelected = isSelected;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

    public String getSeatBadge() {
        return seatBadge;
    }

    public void setSeatBadge(String seatBadge) {
        this.seatBadge = seatBadge;
    }

    public boolean isAll() {
        return isAll;
    }

    public void setAll(boolean all) {
        isAll = all;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
