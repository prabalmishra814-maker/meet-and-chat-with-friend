package com.roomchatapps.Pmishra.models;

public class GuardRankModel {
    private int rank;
    private String userName;
    private String profileId;
    private String avatarUrl;
    private String guardScore;

    public GuardRankModel() {}

    public GuardRankModel(int rank, String userName, String profileId, String avatarUrl, String guardScore) {
        this.rank = rank;
        this.userName = userName;
        this.profileId = profileId;
        this.avatarUrl = avatarUrl;
        this.guardScore = guardScore;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getGuardScore() { return guardScore; }
    public void setGuardScore(String guardScore) { this.guardScore = guardScore; }
}
