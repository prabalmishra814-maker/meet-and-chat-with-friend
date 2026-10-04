package com.roomchatapps.Pmishra.models;

import androidx.annotation.Keep;

@Keep
public class RoomContributionModel {
    private String userId;
    private String userName;
    private String userAvatar;
    private long amount;

    public RoomContributionModel() {}

    public RoomContributionModel(String userId, String userName, String userAvatar, long amount) {
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.amount = amount;
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

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
