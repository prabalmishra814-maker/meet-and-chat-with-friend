package com.roomchatapps.Pmishra.models;

public class TransactionModel {
    private String id;
    private String type; // "TOPUP", "GIFT_SENT", "GIFT_RECEIVED", "REWARD"
    private long coinAmount;
    private long diamondAmount;
    private String title;
    private String description;
    private long timestamp;

    public TransactionModel() {}

    public TransactionModel(String id, String type, long coinAmount, long diamondAmount, String title, String description, long timestamp) {
        this.id = id;
        this.type = type;
        this.coinAmount = coinAmount;
        this.diamondAmount = diamondAmount;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getCoinAmount() { return coinAmount; }
    public void setCoinAmount(long coinAmount) { this.coinAmount = coinAmount; }

    public long getDiamondAmount() { return diamondAmount; }
    public void setDiamondAmount(long diamondAmount) { this.diamondAmount = diamondAmount; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
