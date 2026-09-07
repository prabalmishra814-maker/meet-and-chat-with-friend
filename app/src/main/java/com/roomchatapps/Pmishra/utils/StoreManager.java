package com.roomchatapps.Pmishra.utils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.StoreItemModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoreManager {

    public interface CatalogCallback {
        void onCatalogLoaded(List<StoreItemModel> items);
        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public static void getStoreCatalog(String uid, String categoryFilter, CatalogCallback callback) {
        DatabaseReference storeRef = FirebaseDatabase.getInstance().getReference("store_items");

        storeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<StoreItemModel> rawCatalog = new ArrayList<>();
                List<StoreItemModel> defaultItems = seedDefaultItems();

                if (!snapshot.exists()) {
                    rawCatalog = defaultItems;
                    for (StoreItemModel item : rawCatalog) {
                        storeRef.child(item.getId()).setValue(item);
                    }
                } else {
                    // Sync updated items with new icons to Firebase
                    for (StoreItemModel def : defaultItems) {
                        storeRef.child(def.getId()).setValue(def);
                    }
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        StoreItemModel item = ds.getValue(StoreItemModel.class);
                        if (item != null) {
                            if (item.getId() == null) item.setId(ds.getKey());
                            rawCatalog.add(item);
                        }
                    }
                    if (rawCatalog.isEmpty()) {
                        rawCatalog = defaultItems;
                    }
                }

                // Cross reference with user inventory and equipped items if user logged in
                if (uid != null && !uid.isEmpty()) {
                    loadUserInventoryAndEquipped(uid, rawCatalog, categoryFilter, callback);
                } else {
                    filterAndReturn(rawCatalog, categoryFilter, callback);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    private static void loadUserInventoryAndEquipped(String uid, List<StoreItemModel> catalog, String categoryFilter, CatalogCallback callback) {
        DatabaseReference inventoryRef = FirebaseDatabase.getInstance().getReference("user_inventory").child(uid);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot invSnapshot) {
                Set<String> ownedIds = new HashSet<>();
                if (invSnapshot.exists()) {
                    for (DataSnapshot ds : invSnapshot.getChildren()) {
                        ownedIds.add(ds.getKey());
                    }
                }

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        Set<String> equippedIds = new HashSet<>();
                        if (userSnapshot.exists()) {
                            for (DataSnapshot ds : userSnapshot.getChildren()) {
                                if (ds.getKey() != null && ds.getKey().startsWith("equipped_")) {
                                    Object val = ds.getValue();
                                    if (val != null) equippedIds.add(String.valueOf(val));
                                }
                            }
                        }

                        for (StoreItemModel item : catalog) {
                            item.setOwned(ownedIds.contains(item.getId()));
                            item.setEquipped(equippedIds.contains(item.getId()));
                        }

                        filterAndReturn(catalog, categoryFilter, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        filterAndReturn(catalog, categoryFilter, callback);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                filterAndReturn(catalog, categoryFilter, callback);
            }
        });
    }

    private static void filterAndReturn(List<StoreItemModel> catalog, String categoryFilter, CatalogCallback callback) {
        List<StoreItemModel> filtered = new ArrayList<>();
        for (StoreItemModel item : catalog) {
            if (categoryFilter == null || categoryFilter.equalsIgnoreCase("ALL") || categoryFilter.equalsIgnoreCase(item.getCategory())) {
                filtered.add(item);
            }
        }
        if (callback != null) callback.onCatalogLoaded(filtered);
    }

    public static void buyItem(String uid, StoreItemModel item, ActionCallback callback) {
        if (uid == null || item == null) {
            if (callback != null) callback.onError("Invalid parameters");
            return;
        }

        // Deduct coins using WalletManager
        WalletManager.spendCoinsForGift(uid, null, item.getPriceCoins(), item.getName() + " (Store)", new WalletManager.WalletCallback() {
            @Override
            public void onSuccess(String message, long newCoinBalance) {
                // Mark item as owned in user inventory
                DatabaseReference invRef = FirebaseDatabase.getInstance().getReference("user_inventory")
                        .child(uid).child(item.getId());

                invRef.setValue(System.currentTimeMillis()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        item.setOwned(true);
                        // Auto equip upon buying
                        equipItem(uid, item, new ActionCallback() {
                            @Override
                            public void onSuccess(String msg) {
                                if (callback != null) callback.onSuccess("🎉 Purchased & Equipped " + item.getName() + "!");
                            }

                            @Override
                            public void onError(String err) {
                                if (callback != null) callback.onSuccess("🎉 Purchased " + item.getName() + "!");
                            }
                        });
                    } else {
                        if (callback != null) callback.onError("Failed to record inventory item.");
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (callback != null) callback.onError(error);
            }
        });
    }

    public static void equipItem(String uid, StoreItemModel item, ActionCallback callback) {
        if (uid == null || item == null || item.getCategory() == null) {
            if (callback != null) callback.onError("Invalid parameters");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        String categoryKey = "equipped_" + item.getCategory().toLowerCase();

        userRef.child(categoryKey).setValue(item.getId()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                item.setEquipped(true);
                if (callback != null) callback.onSuccess("Equipped " + item.getName() + "!");
            } else {
                if (callback != null) callback.onError("Failed to equip item.");
            }
        });
    }

    private static List<StoreItemModel> seedDefaultItems() {
        List<StoreItemModel> items = new ArrayList<>();
        
        // Profile Banner / Frames
        items.add(new StoreItemModel("frame_default_neon", "Default Neon Frame", "FRAME", 0, "Basic glowing profile frame", "_1000092519_removebg_preview", "FREE 🎁"));
        items.add(new StoreItemModel("frame_royal_gold_banner", "Royal Gold Banner", "FRAME", 120, "Luxurious Royal Gold Profile Banner", "_1000092517_removebg_preview", "ROYAL 👑"));
        items.add(new StoreItemModel("frame_mystic_aura_banner", "Mystic Aura Banner", "FRAME", 180, "Enchanted Mystic Aura Profile Banner", "_1000092518_removebg_preview", "NEW 🔥"));
        items.add(new StoreItemModel("frame_vibrant_banner", "Vibrant Glowing Banner", "FRAME", 220, "Vibrant Glowing Premium Profile Banner", "_1000092519_removebg_preview", "EPIC 💎"));
        items.add(new StoreItemModel("frame_cyber_yellow", "Cyber Yellow Frame", "FRAME", 100, "Electric yellow profile border", "_1000092462_removebg_preview", "NEW 🔥"));
        items.add(new StoreItemModel("frame_neon_green", "Neon Green Border", "FRAME", 150, "Bright neon green outline", "_1000092463_removebg_preview", "COOL"));
        items.add(new StoreItemModel("frame_mystic_purple", "Mystic Purple Aura", "FRAME", 200, "Deep purple mysterious frame", "_1000092464_removebg_preview", "TRENDING"));
        items.add(new StoreItemModel("frame_hot_pink", "Hot Pink Glow", "FRAME", 250, "Vibrant hot pink profile ring", "_1000092465_removebg_preview", "HOT 🔥"));
        items.add(new StoreItemModel("frame_aqua_blue", "Aqua Blue Ring", "FRAME", 300, "Refreshing aqua blue energy frame", "_1000092466_removebg_preview", "POPULAR"));
        items.add(new StoreItemModel("frame_golden_royal", "Golden Royal Border", "FRAME", 400, "Luxurious golden border", "_1000092467_removebg_preview", "VIP 💎"));
        items.add(new StoreItemModel("frame_diamond_glint", "Diamond Glint Frame", "FRAME", 500, "Sparkling diamond profile frame", "_1000092469_removebg_preview", "LUXURY"));
        items.add(new StoreItemModel("frame_ultimate_fire", "Ultimate Fire Ring", "FRAME", 800, "Blazing ultimate fire ring", "_1000092470_removebg_preview", "EPIC 🦅"));

        // Entrances (Kept new image for Gold Sports Car)
        items.add(new StoreItemModel("entrance_sports_car", "Gold Sports Car Entrance", "ENTRANCE", 600, "Ride into rooms in a Golden Sports Car", "_1000092344_removebg_preview", "POPULAR"));
        items.add(new StoreItemModel("entrance_phoenix", "Phoenix Flight Effect", "ENTRANCE", 1000, "Fly into rooms with fiery phoenix wings", "_1000092363_removebg_preview", "EPIC 🦅"));

        // Bubbles (Kept new images for Neon Cyan & VIP Gold Sparkle)
        items.add(new StoreItemModel("bubble_cyan", "Neon Cyan Glow Bubble", "BUBBLE", 200, "Glowing cyan background for room chat", "_1000092342_removebg_preview", "COOL"));
        items.add(new StoreItemModel("bubble_vip_gold", "VIP Gold Sparkle Bubble", "BUBBLE", 450, "Shiny gold chat message bubble", "_1000092343_removebg_preview", "VIP 🌟"));

        // VIP
        items.add(new StoreItemModel("vip_baron", "Baron Nobility Badge", "VIP", 1200, "Unlock Baron Nobility Title & Privileges", "profile_entrance_nobel_img", "NOBILITY"));
        items.add(new StoreItemModel("vip_king", "Royal King SVIP", "VIP", 2500, "Ultimate King SVIP Title Badge", "profile_entrance_svip_img", "SVIP 👑"));

        return items;
    }
}
