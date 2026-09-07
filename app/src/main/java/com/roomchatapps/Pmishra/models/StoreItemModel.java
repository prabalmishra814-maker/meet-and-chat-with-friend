package com.roomchatapps.Pmishra.models;

public class StoreItemModel {
    private String id;
    private String name;
    private String category; // "FRAME", "ENTRANCE", "BUBBLE", "VIP"
    private long priceCoins;
    private String description;
    private String iconResName;
    private String badgeText;
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
        this.isOwned = false;
        this.isEquipped = false;
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

    public boolean isOwned() { return isOwned; }
    public void setOwned(boolean owned) { isOwned = owned; }

    public boolean isEquipped() { return isEquipped; }
    public void setEquipped(boolean equipped) { isEquipped = equipped; }
}
