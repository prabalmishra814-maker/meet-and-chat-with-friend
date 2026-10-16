package com.roomchatapps.Pmishra.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class AgencyMonthlyReportModel {
    private String monthName; // e.g., "April 2025"
    private int totalHosts;
    private long totalCoins;
    private double incomeUsd;
    private double commissionBonus;
    private String status; // "PAID", "PROCESSING", "CALCULATING"

    public AgencyMonthlyReportModel() {
        // Required empty constructor
    }

    public AgencyMonthlyReportModel(String monthName, int totalHosts, long totalCoins, double incomeUsd, double commissionBonus, String status) {
        this.monthName = monthName;
        this.totalHosts = totalHosts;
        this.totalCoins = totalCoins;
        this.incomeUsd = incomeUsd;
        this.commissionBonus = commissionBonus;
        this.status = status;
    }

    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }

    public int getTotalHosts() { return totalHosts; }
    public void setTotalHosts(int totalHosts) { this.totalHosts = totalHosts; }

    public long getTotalCoins() { return totalCoins; }
    public void setTotalCoins(long totalCoins) { this.totalCoins = totalCoins; }

    public double getIncomeUsd() { return incomeUsd; }
    public void setIncomeUsd(double incomeUsd) { this.incomeUsd = incomeUsd; }

    public double getCommissionBonus() { return commissionBonus; }
    public void setCommissionBonus(double commissionBonus) { this.commissionBonus = commissionBonus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
