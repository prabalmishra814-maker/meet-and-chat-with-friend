package com.roomchatapps.Pmishra.models;

public class IntimacyFriendModel {

    private String userId;
    private String userName;
    private String avatarUrl;
    private long intimacyScore;
    private int level;
    private long startDateTimestamp;
    private long durationDays;
    private boolean isBestFriend;

    public IntimacyFriendModel() {
    }

    public IntimacyFriendModel(String userId, String userName, String avatarUrl, long intimacyScore, int level, long startDateTimestamp, long durationDays, boolean isBestFriend) {
        this.userId = userId;
        this.userName = userName;
        this.avatarUrl = avatarUrl;
        this.intimacyScore = intimacyScore;
        this.level = level;
        this.startDateTimestamp = startDateTimestamp;
        this.durationDays = durationDays;
        this.isBestFriend = isBestFriend;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public long getIntimacyScore() {
        return intimacyScore;
    }

    public void setIntimacyScore(long intimacyScore) {
        this.intimacyScore = intimacyScore;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getStartDateTimestamp() {
        return startDateTimestamp;
    }

    public void setStartDateTimestamp(long startDateTimestamp) {
        this.startDateTimestamp = startDateTimestamp;
    }

    public long getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(long durationDays) {
        this.durationDays = durationDays;
    }

    public boolean isBestFriend() {
        return isBestFriend;
    }

    public void setBestFriend(boolean bestFriend) {
        isBestFriend = bestFriend;
    }
}
