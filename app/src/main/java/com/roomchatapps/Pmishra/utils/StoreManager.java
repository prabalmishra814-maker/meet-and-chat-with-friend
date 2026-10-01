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
                    // Sync updated items with new prices to Firebase
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
            // Keep ONLY FRAME and ENTRANCE categories
            if (!"FRAME".equalsIgnoreCase(item.getCategory()) && !"ENTRANCE".equalsIgnoreCase(item.getCategory())) {
                continue;
            }
            // Keep ONLY SVGA Frames for FRAME category
            if ("FRAME".equalsIgnoreCase(item.getCategory()) && (item.getSvgaPath() == null || item.getSvgaPath().trim().isEmpty())) {
                continue;
            }
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

    public static void unequipItem(String uid, StoreItemModel item, ActionCallback callback) {
        if (uid == null || item == null || item.getCategory() == null) {
            if (callback != null) callback.onError("Invalid parameters");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        String categoryKey = "equipped_" + item.getCategory().toLowerCase();

        userRef.child(categoryKey).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                item.setEquipped(false);
                if (callback != null) callback.onSuccess("Unequipped " + item.getName() + "!");
            } else {
                if (callback != null) callback.onError("Failed to unequip item.");
            }
        });
    }

    // DAILY CHECK-IN STORE REWARD
    public static void grantStoreItemOwnership(String uid, String itemId, ActionCallback callback) {
        if (uid == null || itemId == null || itemId.trim().isEmpty()) {
            if (callback != null) callback.onError("Invalid parameters");
            return;
        }

        DatabaseReference invRef = FirebaseDatabase.getInstance().getReference("user_inventory")
                .child(uid).child(itemId.trim());

        invRef.setValue(System.currentTimeMillis()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (callback != null) callback.onSuccess("Item unlocked successfully!");
            } else {
                if (callback != null) callback.onError("Failed to record inventory item.");
            }
        });
    }

    private static List<StoreItemModel> seedDefaultItems() {
        List<StoreItemModel> items = new ArrayList<>();
        
        // Profile Banner / Frames - Exact Prices from Screenshots
        items.add(new StoreItemModel("frame_champion", "Champion Frame 🏆", "FRAME", 200000L, "Glorious Champion Frame", "ic_crown_gold_frame", "CHAMPION 👑", "frame/champion_frame.svga"));
        items.add(new StoreItemModel("frame_crown_circle", "Crown Circle 👑", "FRAME", 400000L, "Royal Crown Ring Frame", "ic_crown_gold_frame", "ROYAL ✨", "frame/crown_circle.svga"));
        items.add(new StoreItemModel("frame_diamond_glow", "Diamond Glow ✨", "FRAME", 700000L, "Glowing Diamond Energy Frame", "_1000092462_removebg_preview", "HOT 🔥", "frame/diamond_glow.svga"));
        items.add(new StoreItemModel("frame_diamond_ring", "Diamond Ring 💎", "FRAME", 800000L, "Dazzling Diamond Ring Frame", "_1000092469_removebg_preview", "LUXURY", "frame/diamond_ring.svga"));
        items.add(new StoreItemModel("frame_star_ring", "Star Ring ✨", "FRAME", 1000000L, "Shining Star Ring Frame", "_1000092462_removebg_preview", "POPULAR", "frame/star_ring.svga"));
        items.add(new StoreItemModel("frame_crystal", "Crystal Frame 💎", "FRAME", 1500000L, "Sparkling Crystal Border", "_1000092469_removebg_preview", "CRYSTAL 💎", "frame/crystal_frame.svga"));
        items.add(new StoreItemModel("frame_crystal_ring", "Crystal Ring 💍", "FRAME", 1500000L, "Radiant Crystal Ring Frame", "_1000092466_removebg_preview", "NEW 🔥", "frame/crystal_ring.svga"));
        items.add(new StoreItemModel("frame_fire_ring", "Fire Ring 💥", "FRAME", 1500000L, "Blazing Fire Ring Frame", "_1000092470_removebg_preview", "FIRE 🔥", "frame/fire_ring_frame.svga"));
        items.add(new StoreItemModel("frame_lion_glory", "Lion Glory 🦁", "FRAME", 1500000L, "Glorious Lion Spirit Frame", "_1000092467_removebg_preview", "LION 🦁", "frame/lion_glory.svga"));

        items.add(new StoreItemModel("frame_music_ring", "Music Ring 🎵", "FRAME", 2000000L, "Rhythmic Music Ring Frame", "_1000092463_removebg_preview", "MUSIC 🎵", "frame/music_ring.svga"));
        items.add(new StoreItemModel("frame_purple_star", "Purple Star 🌟", "FRAME", 2000000L, "Purple Starburst Frame", "_1000092464_removebg_preview", "STAR 🌟", "frame/purple_star.svga"));
        items.add(new StoreItemModel("frame_golden_beast", "Golden Beast 🦁", "FRAME", 2000000L, "Golden Beast Aura Frame", "ic_crown_gold_frame", "VIP 👑", "frame/golden_beast.svga"));
        items.add(new StoreItemModel("frame_flame_lion", "Flame Lion 🦁", "FRAME", 2000000L, "Mighty Flame Lion Frame", "_1000092470_removebg_preview", "LION 🦁", "frame/flame_lion.svga"));
        items.add(new StoreItemModel("frame_imperial_glory", "Imperial Glory 👑", "FRAME", 2000000L, "Imperial Royal Glory Frame", "ic_crown_gold_frame", "IMPERIAL 🌟", "frame/imperial_glory.svga"));
        items.add(new StoreItemModel("frame_golden_emperor", "Golden Emperor 👑", "FRAME", 2000000L, "Majestic Golden Emperor Frame", "ic_crown_gold_frame", "EMPEROR 👑", "frame/golden_emperor.svga"));
        items.add(new StoreItemModel("frame_golden_wings", "Golden Wings 🪽", "FRAME", 2000000L, "Shining Golden Wings Frame", "ic_crown_gold_frame", "WINGS 🪽", "frame/golden_wings.svga"));
        items.add(new StoreItemModel("frame_majestic_aura", "Majestic Aura 🌟", "FRAME", 2000000L, "Majestic Glowing Aura Frame", "_1000092464_removebg_preview", "FEATURED", "frame/majestic_aura.svga"));

        items.add(new StoreItemModel("frame_inferno_crown", "Inferno Crown 🔥", "FRAME", 2500000L, "Inferno Flame Crown Frame", "_1000092470_removebg_preview", "HOT 🔥", "frame/inferno_crown.svga"));
        items.add(new StoreItemModel("frame_nature_ring", "Nature Ring 🌿", "FRAME", 3000000L, "Fresh Nature Ring Frame", "_1000092463_removebg_preview", "NATURE 🌿", "frame/nature_ring.svga"));
        items.add(new StoreItemModel("frame_vip_1", "VIP 1 Frame 👑", "FRAME", 3000000L, "VIP Level 1 Avatar Frame", "ic_crown_bronze_frame", "VIP 1", "frame/vip_1.svga"));
        items.add(new StoreItemModel("frame_purple_thunder", "Purple Thunder ⚡", "FRAME", 3000000L, "Electric Purple Thunder Frame", "_1000092464_removebg_preview", "THUNDER ⚡", "frame/purple_thunder.svga"));

        items.add(new StoreItemModel("frame_ice_crystal", "Ice Crystal ❄️", "FRAME", 3500000L, "Cool Ice Crystal Frame", "_1000092466_removebg_preview", "COOL ❄️", "frame/ice_crystal.svga"));
        items.add(new StoreItemModel("frame_dragon", "Dragon Flame 🐉", "FRAME", 3500000L, "Fiery Dragon Frame", "_1000092470_removebg_preview", "EPIC 🐲", "frame/dragon_frame.svga"));
        items.add(new StoreItemModel("frame_vip_2", "VIP 2 Frame 👑", "FRAME", 3500000L, "VIP Level 2 Avatar Frame", "ic_crown_silver_frame", "VIP 2", "frame/vip_2.svga"));

        items.add(new StoreItemModel("frame_vip_3", "VIP 3 Frame 👑", "FRAME", 4000000L, "VIP Level 3 Avatar Frame", "ic_crown_gold_frame", "VIP 3", "frame/vip_3.svga"));
        items.add(new StoreItemModel("frame_vip_4", "VIP 4 Frame 👑", "FRAME", 4500000L, "VIP Level 4 Avatar Frame", "ic_crown_gold_frame", "VIP 4", "frame/vip_4.svga"));

        items.add(new StoreItemModel("frame_purple_mask", "Purple Mask 🎭", "FRAME", 5000000L, "Mysterious Purple Mask Frame", "_1000092464_removebg_preview", "MYSTIC 🎭", "frame/purple_mask.svga"));
        items.add(new StoreItemModel("frame_vip_5", "VIP 5 Frame 👑", "FRAME", 5000000L, "VIP Level 5 Avatar Frame", "ic_crown_gold_frame", "VIP 5", "frame/vip_5.svga"));

        items.add(new StoreItemModel("frame_star_crown", "Star Crown 👑", "FRAME", 6000000L, "Star Crown Frame", "ic_crown_gold_frame", "CROWN 👑", "frame/star_crown.svga"));
        items.add(new StoreItemModel("frame_vip_6", "VIP 6 Frame 👑", "FRAME", 6000000L, "VIP Level 6 Avatar Frame", "ic_crown_gold_frame", "VIP 6", "frame/vip_6.svga"));

        items.add(new StoreItemModel("frame_vip_7", "VIP 7 Frame 👑", "FRAME", 7000000L, "Ultimate VIP Level 7 Frame", "ic_crown_gold_frame", "VIP 7 🔥", "frame/vip_7.svga"));
        items.add(new StoreItemModel("frame_rank_3", "Rank 3 Bronze Frame 🥉", "FRAME", 7500000L, "Top 3 Leaderboard Rank Frame", "ic_crown_bronze_frame", "RANK 3 🥉", "frame/frame_rank_3.svga"));
        items.add(new StoreItemModel("frame_rank_2", "Rank 2 Silver Frame 🥈", "FRAME", 8000000L, "Top 2 Leaderboard Rank Frame", "ic_crown_silver_frame", "RANK 2 🥈", "frame/frame_rank_2.svga"));
        items.add(new StoreItemModel("frame_rank_1", "Rank 1 Gold Frame 🥇", "FRAME", 9000000L, "Top 1 Leaderboard Rank Frame", "ic_crown_gold_frame", "RANK 1 🥇", "frame/frame_rank_1.svga"));

        // Entrances / Rides - Exact Prices from Screenshots
        items.add(new StoreItemModel("entrance_magic_deer", "Magic Deer 🦌", "ENTRANCE", 5400000L, "Arrive gracefully on Magic Deer", "ic_entrance_toyota_car", "MAGIC ✨", "Entry/magic_deer.svga"));
        items.add(new StoreItemModel("entrance_toyota_car", "Toyota Car Entrance 🚗", "ENTRANCE", 8000000L, "Cruising into rooms in Toyota Car", "ic_entrance_toyota_car", "POPULAR", "Entry/toyota_car_entry.svga"));
        items.add(new StoreItemModel("entrance_red_car", "Red Super Car 🏎️", "ENTRANCE", 8000000L, "Arrive in style with Red Super Car", "ic_entrance_red_car", "HOT 🔥", "Entry/red_super_car.svga"));
        items.add(new StoreItemModel("entrance_anime_man", "Anime Legend Arrival ⚡", "ENTRANCE", 10000000L, "Enter rooms with Anime Hero Arrival", "ic_entrance_anime_man", "EPIC ⚡", "Entry/anime_man_entry.svga"));
        // DAILY CHECK-IN STORE REWARD
        items.add(new StoreItemModel("entrance_golden_super_car", "Golden Super Car 🏎️", "ENTRANCE", 12000000L, "Arrive like royalty in Golden Super Car", "ic_entrance_golden_car", "LEGENDARY 👑", "Entry/golden_super_car.svga"));

        return items;
    }
}
