package com.roomchatapps.Pmishra.models;

public class LeaderboardModel {
    private String uid;
    private String name;
    private String profileId;
    private String avatar;
    private long spentCoins;
    private int rank;

    public LeaderboardModel() {
    }

    public LeaderboardModel(String uid, String name, String profileId, String avatar, long spentCoins, int rank) {
        this.uid = uid;
        this.name = name;
        this.profileId = profileId;
        this.avatar = avatar;
        this.spentCoins = spentCoins;
        this.rank = rank;
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

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public long getSpentCoins() {
        return spentCoins;
    }

    public void setSpentCoins(long spentCoins) {
        this.spentCoins = spentCoins;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
