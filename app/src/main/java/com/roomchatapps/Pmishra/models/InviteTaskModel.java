package com.roomchatapps.Pmishra.models;

public class InviteTaskModel {

    private String id;
    private String title;
    private String description;
    private int iconRes;
    private String rewardType; // "CHIP" or "COIN"
    private long currentCount;
    private boolean hasHelpInfo;

    public InviteTaskModel(String id, String title, String description, int iconRes, String rewardType, long currentCount, boolean hasHelpInfo) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.rewardType = rewardType;
        this.currentCount = currentCount;
        this.hasHelpInfo = hasHelpInfo;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconRes() { return iconRes; }
    public String getRewardType() { return rewardType; }
    public long getCurrentCount() { return currentCount; }
    public boolean isHasHelpInfo() { return hasHelpInfo; }

    public void setCurrentCount(long currentCount) { this.currentCount = currentCount; }
}
