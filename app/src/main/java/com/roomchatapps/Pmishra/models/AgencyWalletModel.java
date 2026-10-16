package com.roomchatapps.Pmishra.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class AgencyWalletModel {
    private String agencyId;
    private String userId;
    private double totalEarningsUsd;
    private double availableBalanceUsd;
    private double withdrawnBalanceUsd;
    private double pendingWithdrawalUsd;
    private long totalCoins;
    private long updatedAt;

    public AgencyWalletModel() {
        // Required empty constructor
    }

    public AgencyWalletModel(String agencyId, String userId, double totalEarningsUsd,
                             double availableBalanceUsd, double withdrawnBalanceUsd,
                             double pendingWithdrawalUsd, long totalCoins, long updatedAt) {
        this.agencyId = agencyId;
        this.userId = userId;
        this.totalEarningsUsd = totalEarningsUsd;
        this.availableBalanceUsd = availableBalanceUsd;
        this.withdrawnBalanceUsd = withdrawnBalanceUsd;
        this.pendingWithdrawalUsd = pendingWithdrawalUsd;
        this.totalCoins = totalCoins;
        this.updatedAt = updatedAt;
    }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getTotalEarningsUsd() { return totalEarningsUsd; }
    public void setTotalEarningsUsd(double totalEarningsUsd) { this.totalEarningsUsd = totalEarningsUsd; }

    public double getAvailableBalanceUsd() { return availableBalanceUsd; }
    public void setAvailableBalanceUsd(double availableBalanceUsd) { this.availableBalanceUsd = availableBalanceUsd; }

    public double getWithdrawnBalanceUsd() { return withdrawnBalanceUsd; }
    public void setWithdrawnBalanceUsd(double withdrawnBalanceUsd) { this.withdrawnBalanceUsd = withdrawnBalanceUsd; }

    public double getPendingWithdrawalUsd() { return pendingWithdrawalUsd; }
    public void setPendingWithdrawalUsd(double pendingWithdrawalUsd) { this.pendingWithdrawalUsd = pendingWithdrawalUsd; }

    public long getTotalCoins() { return totalCoins; }
    public void setTotalCoins(long totalCoins) { this.totalCoins = totalCoins; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
