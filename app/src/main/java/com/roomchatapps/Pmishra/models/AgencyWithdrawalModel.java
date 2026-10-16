package com.roomchatapps.Pmishra.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class AgencyWithdrawalModel {
    private String withdrawalId;
    private String agencyId;
    private String userId;
    private String agencyName;
    private String ownerName;
    private double amountUsd;
    private String paymentMethod; // "UPI", "Bank Transfer", "PayPal", "USDT"
    private String accountDetails; // e.g. UPI ID or Account Number
    private String status; // "PENDING", "APPROVED", "COMPLETED", "REJECTED"
    private String adminNote;
    private long timestamp;

    public AgencyWithdrawalModel() {
        // Required empty constructor
    }

    public AgencyWithdrawalModel(String withdrawalId, String agencyId, String userId,
                                 String agencyName, String ownerName, double amountUsd,
                                 String paymentMethod, String accountDetails,
                                 String status, String adminNote, long timestamp) {
        this.withdrawalId = withdrawalId;
        this.agencyId = agencyId;
        this.userId = userId;
        this.agencyName = agencyName;
        this.ownerName = ownerName;
        this.amountUsd = amountUsd;
        this.paymentMethod = paymentMethod;
        this.accountDetails = accountDetails;
        this.status = status;
        this.adminNote = adminNote;
        this.timestamp = timestamp;
    }

    public String getWithdrawalId() { return withdrawalId; }
    public void setWithdrawalId(String withdrawalId) { this.withdrawalId = withdrawalId; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public double getAmountUsd() { return amountUsd; }
    public void setAmountUsd(double amountUsd) { this.amountUsd = amountUsd; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getAccountDetails() { return accountDetails; }
    public void setAccountDetails(String accountDetails) { this.accountDetails = accountDetails; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
