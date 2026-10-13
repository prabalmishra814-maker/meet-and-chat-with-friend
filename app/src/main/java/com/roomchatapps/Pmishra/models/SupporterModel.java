package com.roomchatapps.Pmishra.models;

public class SupporterModel {

    private String userId;
    private String userName;
    private String avatarUrl;
    private int rank;
    private long guardScore;

    public SupporterModel() {
    }

    public SupporterModel(String userId, String userName, String avatarUrl, int rank, long guardScore) {
        this.userId = userId;
        this.userName = userName;
        this.avatarUrl = avatarUrl;
        this.rank = rank;
        this.guardScore = guardScore;
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getGuardScore() {
        return guardScore;
    }

    public void setGuardScore(long guardScore) {
        this.guardScore = guardScore;
    }
}
