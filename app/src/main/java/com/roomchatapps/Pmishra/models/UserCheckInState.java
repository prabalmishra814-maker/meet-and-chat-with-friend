package com.roomchatapps.Pmishra.models;

import java.util.HashMap;
import java.util.Map;

// DAILY CHECK-IN
public class UserCheckInState {
    private int currentDay = 1;
    private String lastClaimDate = "";
    private Map<String, Boolean> claimedDays = new HashMap<>();
    private boolean isTodayClaimed = false;

    public UserCheckInState() {}

    public int getCurrentDay() { return currentDay; }
    public void setCurrentDay(int currentDay) { this.currentDay = currentDay; }

    public String getLastClaimDate() { return lastClaimDate; }
    public void setLastClaimDate(String lastClaimDate) { this.lastClaimDate = lastClaimDate; }

    public Map<String, Boolean> getClaimedDays() {
        if (claimedDays == null) claimedDays = new HashMap<>();
        return claimedDays;
    }
    public void setClaimedDays(Map<String, Boolean> claimedDays) { this.claimedDays = claimedDays; }

    public boolean isTodayClaimed() { return isTodayClaimed; }
    public void setTodayClaimed(boolean todayClaimed) { isTodayClaimed = todayClaimed; }

    public boolean isDayClaimed(int dayNumber) {
        if (claimedDays == null) return false;
        Boolean val = claimedDays.get("day_" + dayNumber);
        return val != null && val;
    }
}
