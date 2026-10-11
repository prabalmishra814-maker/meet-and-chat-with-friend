package com.roomchatapps.Pmishra.models;

public class CpRecentGiftModel {

    private String id;
    private String giftName;
    private long giftCost;
    private String senderUid;
    private String senderName;
    private int iconRes;
    private long timestamp;

    public CpRecentGiftModel() {
        // Default constructor required for Firebase
    }

    public CpRecentGiftModel(String id, String giftName, long giftCost, String senderUid, String senderName, int iconRes, long timestamp) {
        this.id = id;
        this.giftName = giftName;
        this.giftCost = giftCost;
        this.senderUid = senderUid;
        this.senderName = senderName;
        this.iconRes = iconRes;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public long getGiftCost() {
        return giftCost;
    }

    public void setGiftCost(long giftCost) {
        this.giftCost = giftCost;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
