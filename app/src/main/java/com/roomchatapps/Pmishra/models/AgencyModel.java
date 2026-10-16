package com.roomchatapps.Pmishra.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class AgencyModel {
    private String agencyId;
    private String userId;
    private String agencyName;
    private String agencyLogo;
    private String agencyDescription;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private String ownerNationalId;
    private String status; // "PENDING", "APPROVED", "REJECTED"
    private String adminNote;
    private long appliedAt;
    private long updatedAt;
    private int memberCount;
    private String bdUid; // Optional parent Business Developer UID
    private String bdName; // Optional parent Business Developer Name

    public AgencyModel() {
        // Default constructor required for Firebase Realtime Database
    }

    public AgencyModel(String agencyId, String userId, String agencyName, String agencyLogo,
                       String agencyDescription, String ownerName, String ownerPhone,
                       String ownerEmail, String ownerNationalId, String status,
                       String adminNote, long appliedAt, long updatedAt) {
        this.agencyId = agencyId;
        this.userId = userId;
        this.agencyName = agencyName;
        this.agencyLogo = agencyLogo;
        this.agencyDescription = agencyDescription;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.ownerEmail = ownerEmail;
        this.ownerNationalId = ownerNationalId;
        this.status = status;
        this.adminNote = adminNote;
        this.appliedAt = appliedAt;
        this.updatedAt = updatedAt;
        this.memberCount = 1;
    }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getAgencyLogo() { return agencyLogo; }
    public void setAgencyLogo(String agencyLogo) { this.agencyLogo = agencyLogo; }

    public String getAgencyDescription() { return agencyDescription; }
    public void setAgencyDescription(String agencyDescription) { this.agencyDescription = agencyDescription; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getOwnerNationalId() { return ownerNationalId; }
    public void setOwnerNationalId(String ownerNationalId) { this.ownerNationalId = ownerNationalId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public long getAppliedAt() { return appliedAt; }
    public void setAppliedAt(long appliedAt) { this.appliedAt = appliedAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public String getBdUid() { return bdUid; }
    public void setBdUid(String bdUid) { this.bdUid = bdUid; }

    public String getBdName() { return bdName; }
    public void setBdName(String bdName) { this.bdName = bdName; }
}
