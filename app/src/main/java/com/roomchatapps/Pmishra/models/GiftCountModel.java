package com.roomchatapps.Pmishra.models;

public class GiftCountModel {
    private String giftName;
    private int iconRes;
    private long costCoins;
    private int receivedCount;
    private int sentCount;

    public GiftCountModel() {}

    public GiftCountModel(String giftName, int iconRes, long costCoins, int receivedCount, int sentCount) {
        this.giftName = giftName;
        this.iconRes = iconRes;
        this.costCoins = costCoins;
        this.receivedCount = receivedCount;
        this.sentCount = sentCount;
    }

    public String getGiftName() { return giftName; }
    public void setGiftName(String giftName) { this.giftName = giftName; }

    public int getIconRes() { return iconRes; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }

    public long getCostCoins() { return costCoins; }
    public void setCostCoins(long costCoins) { this.costCoins = costCoins; }

    public int getReceivedCount() { return receivedCount; }
    public void setReceivedCount(int receivedCount) { this.receivedCount = receivedCount; }

    public int getSentCount() { return sentCount; }
    public void setSentCount(int sentCount) { this.sentCount = sentCount; }
}
