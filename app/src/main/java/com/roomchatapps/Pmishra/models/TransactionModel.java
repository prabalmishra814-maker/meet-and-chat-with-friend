package com.roomchatapps.Pmishra.models;

public class TransactionModel {
    private String id;
    private String type; // "TOPUP", "GIFT_SENT", "GIFT_RECEIVED", "REWARD", "COIN_EXCHANGE", "GAME_SPIN"
    private long coinAmount;
    private String title;
    private String description;
    private long timestamp;

    // Detailed Gift -> Energy Transaction Fields
    private String roomId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
    private String giftId;
    private String giftName;
    private long giftValue;
    private int quantity = 1;
    private long totalCoinCost;
    private long systemCut;
    private long remainingValue;
    private int energyPercentage = 30;
    private long energyAwarded;

    public TransactionModel() {}

    public TransactionModel(String id, String type, long coinAmount, String title, String description, long timestamp) {
        this.id = id;
        this.type = type;
        this.coinAmount = coinAmount;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
    }

    public TransactionModel(String id, String type, long coinAmount, long diamondAmount, String title, String description, long timestamp) {
        this(id, type, coinAmount, title, description, timestamp);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getCoinAmount() { return coinAmount; }
    public void setCoinAmount(long coinAmount) { this.coinAmount = coinAmount; }

    // Legacy getter for backwards compatibility
    public long getDiamondAmount() { return 0; }
    public void setDiamondAmount(long diamondAmount) {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getGiftId() { return giftId; }
    public void setGiftId(String giftId) { this.giftId = giftId; }

    public String getGiftName() { return giftName; }
    public void setGiftName(String giftName) { this.giftName = giftName; }

    public long getGiftValue() { return giftValue; }
    public void setGiftValue(long giftValue) { this.giftValue = giftValue; }

    // Legacy alias
    public long getGiftPrice() { return giftValue; }
    public void setGiftPrice(long giftPrice) { this.giftValue = giftPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public long getTotalCoinCost() { return totalCoinCost; }
    public void setTotalCoinCost(long totalCoinCost) { this.totalCoinCost = totalCoinCost; }

    public long getTotalGiftCoins() { return totalCoinCost; }
    public void setTotalGiftCoins(long totalGiftCoins) { this.totalCoinCost = totalGiftCoins; }

    public long getSystemCut() { return systemCut; }
    public void setSystemCut(long systemCut) { this.systemCut = systemCut; }

    public long getRemainingValue() { return remainingValue; }
    public void setRemainingValue(long remainingValue) { this.remainingValue = remainingValue; }

    public int getEnergyPercentage() { return energyPercentage; }
    public void setEnergyPercentage(int energyPercentage) { this.energyPercentage = energyPercentage; }

    public long getEnergyAwarded() { return energyAwarded; }
    public void setEnergyAwarded(long energyAwarded) { this.energyAwarded = energyAwarded; }
}
