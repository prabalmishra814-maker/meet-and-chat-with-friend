package com.roomchatapps.Pmishra.models;

public class StoreItemModel {
    private String id;
    private String name;
    private String category; // "FRAME", "ENTRANCE", "BUBBLE", "VIP"
    private long priceCoins;
    private String description;
    private String iconResName;
    private String badgeText;
    private String svgaPath;
    private int validityDays = 7; // Default validity e.g. 7 days or 3 days
    private long expiryTimestamp = 0L; // Timestamp when user's ownership expires
    private boolean isGifted = false; // True if item was received as a gift from a friend
    private boolean isOwned;
    private boolean isEquipped;

    public StoreItemModel() {}

    public StoreItemModel(String id, String name, String category, long priceCoins, String description, String iconResName, String badgeText) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.priceCoins = priceCoins;
        this.description = description;
        this.iconResName = iconResName;
        this.badgeText = badgeText;
        this.validityDays = 7;
        this.isOwned = false;
        this.isEquipped = false;
    }

    public StoreItemModel(String id, String name, String category, long priceCoins, String description, String iconResName, String badgeText, String svgaPath) {
        this(id, name, category, priceCoins, description, iconResName, badgeText);
        this.svgaPath = svgaPath;
        this.validityDays = 7;
    }

    public StoreItemModel(String id, String name, String category, long priceCoins, String description, String iconResName, String badgeText, String svgaPath, int validityDays) {
        this(id, name, category, priceCoins, description, iconResName, badgeText, svgaPath);
        this.validityDays = validityDays > 0 ? validityDays : 7;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getPriceCoins() { return priceCoins; }
    public void setPriceCoins(long priceCoins) { this.priceCoins = priceCoins; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconResName() { return iconResName; }
    public void setIconResName(String iconResName) { this.iconResName = iconResName; }

    public String getBadgeText() { return badgeText; }
    public void setBadgeText(String badgeText) { this.badgeText = badgeText; }

    public String getSvgaPath() { return svgaPath; }
    public void setSvgaPath(String svgaPath) { this.svgaPath = svgaPath; }

    public int getValidityDays() { return validityDays > 0 ? validityDays : 7; }
    public void setValidityDays(int validityDays) { this.validityDays = validityDays; }

    public long getExpiryTimestamp() { return expiryTimestamp; }
    public void setExpiryTimestamp(long expiryTimestamp) { this.expiryTimestamp = expiryTimestamp; }

    public boolean isGifted() { return isGifted; }
    public void setGifted(boolean gifted) { isGifted = gifted; }

    public boolean isOwned() { return isOwned; }
    public void setOwned(boolean owned) { isOwned = owned; }

    public boolean isEquipped() { return isEquipped; }
    public void setEquipped(boolean equipped) { isEquipped = equipped; }
}
