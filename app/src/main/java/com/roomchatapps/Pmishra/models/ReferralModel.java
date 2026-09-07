package com.roomchatapps.Pmishra.models;

public class ReferralModel {
    private String id;
    private String referredUid;
    private String referredName;
    private String referredAvatar;
    private long rewardCoins;
    private long timestamp;

    public ReferralModel() {}

    public ReferralModel(String id, String referredUid, String referredName, String referredAvatar, long rewardCoins, long timestamp) {
        this.id = id;
        this.referredUid = referredUid;
        this.referredName = referredName;
        this.referredAvatar = referredAvatar;
        this.rewardCoins = rewardCoins;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReferredUid() { return referredUid; }
    public void setReferredUid(String referredUid) { this.referredUid = referredUid; }

    public String getReferredName() { return referredName; }
    public void setReferredName(String referredName) { this.referredName = referredName; }

    public String getReferredAvatar() { return referredAvatar; }
    public void setReferredAvatar(String referredAvatar) { this.referredAvatar = referredAvatar; }

    public long getRewardCoins() { return rewardCoins; }
    public void setRewardCoins(long rewardCoins) { this.rewardCoins = rewardCoins; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
