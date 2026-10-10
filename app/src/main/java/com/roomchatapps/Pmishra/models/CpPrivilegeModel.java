package com.roomchatapps.Pmishra.models;

public class CpPrivilegeModel {

    private String id;
    private String title;
    private int iconRes;
    private int unlockLevel;
    private String description;
    private boolean isUnlocked;

    public CpPrivilegeModel(String id, String title, int iconRes, int unlockLevel, String description, boolean isUnlocked) {
        this.id = id;
        this.title = title;
        this.iconRes = iconRes;
        this.unlockLevel = unlockLevel;
        this.description = description;
        this.isUnlocked = isUnlocked;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }
}
