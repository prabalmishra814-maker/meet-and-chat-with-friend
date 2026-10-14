package com.roomchatapps.Pmishra.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class BdModel {
    private String bdId;
    private String bdUid;
    private String bdName;
    private String bdPhone;
    private String bdEmail;
    private String status; // "APPROVED", "PENDING", "REJECTED"
    private int totalAgencies;
    private int totalHosts;
    private long createdAt;

    public BdModel() {
        // Required empty constructor
    }

    public BdModel(String bdId, String bdUid, String bdName, String bdPhone, String bdEmail, String status, int totalAgencies, int totalHosts, long createdAt) {
        this.bdId = bdId;
        this.bdUid = bdUid;
        this.bdName = bdName;
        this.bdPhone = bdPhone;
        this.bdEmail = bdEmail;
        this.status = status;
        this.totalAgencies = totalAgencies;
        this.totalHosts = totalHosts;
        this.createdAt = createdAt;
    }

    public String getBdId() { return bdId; }
    public void setBdId(String bdId) { this.bdId = bdId; }

    public String getBdUid() { return bdUid; }
    public void setBdUid(String bdUid) { this.bdUid = bdUid; }

    public String getBdName() { return bdName; }
    public void setBdName(String bdName) { this.bdName = bdName; }

    public String getBdPhone() { return bdPhone; }
    public void setBdPhone(String bdPhone) { this.bdPhone = bdPhone; }

    public String getBdEmail() { return bdEmail; }
    public void setBdEmail(String bdEmail) { this.bdEmail = bdEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalAgencies() { return totalAgencies; }
    public void setTotalAgencies(int totalAgencies) { this.totalAgencies = totalAgencies; }

    public int getTotalHosts() { return totalHosts; }
    public void setTotalHosts(int totalHosts) { this.totalHosts = totalHosts; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
