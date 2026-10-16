package com.roomchatapps.Pmishra.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class AgencyHostModel {
    private String hostUid;
    private String hostName;
    private String hostAvatar;
    private String profileId;
    private long dailyCoins;
    private long monthlyCoins;
    private long totalCoins;
    private double activeHours;
    private long joinedAt;
    private String status; // "LIVE", "ACTIVE", "IDLE"

    public AgencyHostModel() {
        // Required empty constructor for Firebase
    }

    public AgencyHostModel(String hostUid, String hostName, String hostAvatar, String profileId,
                           long dailyCoins, long monthlyCoins, long totalCoins, double activeHours,
                           long joinedAt, String status) {
        this.hostUid = hostUid;
        this.hostName = hostName;
        this.hostAvatar = hostAvatar;
        this.profileId = profileId;
        this.dailyCoins = dailyCoins;
        this.monthlyCoins = monthlyCoins;
        this.totalCoins = totalCoins;
        this.activeHours = activeHours;
        this.joinedAt = joinedAt;
        this.status = status;
    }

    public String getHostUid() { return hostUid; }
    public void setHostUid(String hostUid) { this.hostUid = hostUid; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getHostAvatar() { return hostAvatar; }
    public void setHostAvatar(String hostAvatar) { this.hostAvatar = hostAvatar; }

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }

    public long getDailyCoins() { return dailyCoins; }
    public void setDailyCoins(long dailyCoins) { this.dailyCoins = dailyCoins; }

    public long getMonthlyCoins() { return monthlyCoins; }
    public void setMonthlyCoins(long monthlyCoins) { this.monthlyCoins = monthlyCoins; }

    public long getTotalCoins() { return totalCoins; }
    public void setTotalCoins(long totalCoins) { this.totalCoins = totalCoins; }

    public double getActiveHours() { return activeHours; }
    public void setActiveHours(double activeHours) { this.activeHours = activeHours; }

    public long getJoinedAt() { return joinedAt; }
    public void setJoinedAt(long joinedAt) { this.joinedAt = joinedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
