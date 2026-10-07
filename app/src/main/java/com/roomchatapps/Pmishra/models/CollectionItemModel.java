package com.roomchatapps.Pmishra.models;

import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;

// COLLECTION ACTIVITY
public class CollectionItemModel {

    public enum ItemType {
        ENTRY_EFFECT,
        FRAME,
        GIFT_RECEIVED,
        PHOTO
    }

    private ItemType itemType;
    private StoreItemModel storeItem;                  // For ENTRY_EFFECT & FRAME
    private GiftStoreAdapter.GiftStoreItem giftStoreItem; // For GIFT_RECEIVED
    private String customGiftName;                     // Fallback if not in catalog
    private int giftIconRes;                            // Fallback icon
    private long giftCost;                              // Fallback price
    private int receivedCount;                          // RECEIVED GIFT COUNT

    // PHOTO FIELDS
    private String photoUrl;
    private String photoKey;
    private boolean isAddButton;
    private boolean isCurrentAvatar;

    // Constructor for Entry Effects & Frames
    public CollectionItemModel(ItemType itemType, StoreItemModel storeItem) {
        this.itemType = itemType;
        this.storeItem = storeItem;
    }

    // Constructor for Known Received Gifts
    public CollectionItemModel(GiftStoreAdapter.GiftStoreItem giftStoreItem, int receivedCount) {
        this.itemType = ItemType.GIFT_RECEIVED;
        this.giftStoreItem = giftStoreItem;
        this.receivedCount = receivedCount;
    }

    // Constructor for Fallback Received Gifts (Item not found in catalog)
    public CollectionItemModel(String giftName, int giftIconRes, long giftCost, int receivedCount) {
        this.itemType = ItemType.GIFT_RECEIVED;
        this.customGiftName = giftName;
        this.giftIconRes = giftIconRes;
        this.giftCost = giftCost;
        this.receivedCount = receivedCount;
    }

    // Constructor for Photo Item
    public CollectionItemModel(String photoKey, String photoUrl, boolean isCurrentAvatar) {
        this.itemType = ItemType.PHOTO;
        this.photoKey = photoKey;
        this.photoUrl = photoUrl;
        this.isCurrentAvatar = isCurrentAvatar;
        this.isAddButton = false;
    }

    // Constructor for Add Photo Button Tile
    public static CollectionItemModel createAddPhotoButton() {
        CollectionItemModel model = new CollectionItemModel((String) null, (String) null, false);
        model.isAddButton = true;
        return model;
    }

    public ItemType getItemType() { return itemType; }
    public StoreItemModel getStoreItem() { return storeItem; }
    public GiftStoreAdapter.GiftStoreItem getGiftStoreItem() { return giftStoreItem; }
    public int getReceivedCount() { return receivedCount; }
    public int getGiftIconRes() { return giftIconRes; }
    public String getPhotoUrl() { return photoUrl; }
    public String getPhotoKey() { return photoKey; }
    public boolean isAddButton() { return isAddButton; }
    public boolean isCurrentAvatar() { return isCurrentAvatar; }

    public String getItemName() {
        if (itemType == ItemType.PHOTO) {
            return isAddButton ? "Add Photo" : "Profile Photo";
        } else if (itemType == ItemType.GIFT_RECEIVED) {
            if (giftStoreItem != null) return giftStoreItem.name;
            return customGiftName != null ? customGiftName : "Gift";
        } else if (storeItem != null) {
            return storeItem.getName();
        }
        return "Item";
    }

    public long getItemPrice() {
        if (itemType == ItemType.GIFT_RECEIVED) {
            if (giftStoreItem != null) return giftStoreItem.cost;
            return giftCost;
        } else if (storeItem != null) {
            return storeItem.getPriceCoins();
        }
        return 0;
    }

    public boolean isEquipped() {
        if (itemType == ItemType.PHOTO) return isCurrentAvatar;
        return storeItem != null && storeItem.isEquipped();
    }
}
