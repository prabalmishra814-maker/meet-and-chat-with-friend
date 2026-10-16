package com.roomchatapps.Pmishra.models;

public class CpLevelRewardModel {

    // Temporary testing flag to unlock all CP level rewards by default
    public static boolean TESTING_UNLOCK_ALL = true;

    private String id;
    private String title;
    private String description;
    private String svgaPath;
    private int iconRes;
    private int unlockLevel;
    private boolean isUnlocked;
    private boolean isEquipped;
    private String category;

    public CpLevelRewardModel(String id, String title, String description, String svgaPath, int iconRes, int unlockLevel, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.svgaPath = svgaPath;
        this.iconRes = iconRes;
        this.unlockLevel = unlockLevel;
        this.category = category;
        this.isUnlocked = TESTING_UNLOCK_ALL;
        this.isEquipped = false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSvgaPath() {
        return svgaPath;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }

    public boolean isUnlocked() {
        return isUnlocked || TESTING_UNLOCK_ALL;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public boolean isEquipped() {
        return isEquipped;
    }

    public void setEquipped(boolean equipped) {
        isEquipped = equipped;
    }

    public String getCategory() {
        return category;
    }
}
