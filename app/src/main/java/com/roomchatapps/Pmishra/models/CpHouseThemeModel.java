package com.roomchatapps.Pmishra.models;

public class CpHouseThemeModel {

    private String id;
    private String name;
    private int drawableRes;
    private int unlockLevel;
    private boolean isCustomAdd;
    private boolean isLocked;
    private boolean isEquipped;

    public CpHouseThemeModel(String id, String name, int drawableRes, int unlockLevel, boolean isCustomAdd, boolean isLocked, boolean isEquipped) {
        this.id = id;
        this.name = name;
        this.drawableRes = drawableRes;
        this.unlockLevel = unlockLevel;
        this.isCustomAdd = isCustomAdd;
        this.isLocked = isLocked;
        this.isEquipped = isEquipped;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDrawableRes() {
        return drawableRes;
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }

    public boolean isCustomAdd() {
        return isCustomAdd;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isEquipped() {
        return isEquipped;
    }

    public void setEquipped(boolean equipped) {
        isEquipped = equipped;
    }
}
