package com.roomchatapps.Pmishra.models;

// DAILY CHECK-IN
public class DayRewardConfig {
    private int dayNumber;
    private String dayTitle;
    private String rewardName;
    private String rewardType; // "COIN", "ENERGY", "FRAME", "ENTRANCE"
    private long rewardAmount;
    private String itemId;
    private String iconResName;
    private String svgaPath;

    public DayRewardConfig() {}

    public DayRewardConfig(int dayNumber, String dayTitle, String rewardName, String rewardType, long rewardAmount, String itemId, String iconResName, String svgaPath) {
        this.dayNumber = dayNumber;
        this.dayTitle = dayTitle;
        this.rewardName = rewardName;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount;
        this.itemId = itemId;
        this.iconResName = iconResName;
        this.svgaPath = svgaPath;
    }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public String getDayTitle() { return dayTitle; }
    public void setDayTitle(String dayTitle) { this.dayTitle = dayTitle; }

    public String getRewardName() { return rewardName; }
    public void setRewardName(String rewardName) { this.rewardName = rewardName; }

    public String getRewardType() { return rewardType; }
    public void setRewardType(String rewardType) { this.rewardType = rewardType; }

    public long getRewardAmount() { return rewardAmount; }
    public void setRewardAmount(long rewardAmount) { this.rewardAmount = rewardAmount; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getIconResName() { return iconResName; }
    public void setIconResName(String iconResName) { this.iconResName = iconResName; }

    public String getSvgaPath() { return svgaPath; }
    public void setSvgaPath(String svgaPath) { this.svgaPath = svgaPath; }
}
