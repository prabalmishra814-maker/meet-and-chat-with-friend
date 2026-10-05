package com.roomchatapps.Pmishra.utils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.StoreItemModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
                List<StoreItemModel> defaultItems = seedDefaultItems();
                Set<String> validSeedIds = new HashSet<>();
                for (StoreItemModel def : defaultItems) {
                    if (def.getId() != null) validSeedIds.add(def.getId());
                }

                if (!snapshot.exists()) {
                    for (StoreItemModel item : defaultItems) {
                        storeRef.child(item.getId()).setValue(item);
                    }
                } else {
                    // Sync verified items to Firebase and delete obsolete/duplicate nodes
                    for (StoreItemModel def : defaultItems) {
                        storeRef.child(def.getId()).setValue(def);
                    }
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String key = ds.getKey();
                        if (key != null && !validSeedIds.contains(key)) {
                            storeRef.child(key).removeValue();
                        }
                    }
                }

                List<StoreItemModel> rawCatalog = new ArrayList<>(defaultItems);

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
                Set<String> validOwnedIds = new HashSet<>();
                Set<String> giftedItemIds = new HashSet<>();
                Map<String, Long> itemExpiryMap = new HashMap<>();
                long now = System.currentTimeMillis();

                if (invSnapshot.exists()) {
                    for (DataSnapshot ds : invSnapshot.getChildren()) {
                        String itemId = ds.getKey();
                        if (itemId == null) continue;

                        if (ds.hasChild("acquiredFrom")) {
                            String from = ds.child("acquiredFrom").getValue(String.class);
                            if (from != null && !from.equals("self_purchase") && !from.equals("daily_checkin")) {
                                giftedItemIds.add(itemId);
                            }
                        }

                        long expiry = 0L;
                        if (ds.hasChild("expiryTimestamp")) {
                            Long expObj = ds.child("expiryTimestamp").getValue(Long.class);
                            if (expObj != null) expiry = expObj;
                        } else if (ds.getValue() instanceof Long) {
                            Long purchasedAt = (Long) ds.getValue();
                            if (purchasedAt != null && purchasedAt > 0) {
                                // Default 7 days validity fallback
                                expiry = purchasedAt + (7 * 86400000L);
                            }
                        }

                        // Check if item has expired
                        if (expiry > 0 && now > expiry) {
                            // Item expired: Remove from user_inventory and auto-unequip
                            inventoryRef.child(itemId).removeValue();
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot uSnap) {
                                    if (uSnap.exists()) {
                                        for (DataSnapshot uChild : uSnap.getChildren()) {
                                            if (uChild.getKey() != null && uChild.getKey().startsWith("equipped_")) {
                                                if (itemId.equals(String.valueOf(uChild.getValue()))) {
                                                    userRef.child(uChild.getKey()).removeValue();
                                                }
                                            }
                                        }
                                    }
                                }
                                @Override public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        } else {
                            // Item is valid and active
                            validOwnedIds.add(itemId);
                            itemExpiryMap.put(itemId, expiry);
                        }
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
                            boolean isOwned = validOwnedIds.contains(item.getId());
                            item.setOwned(isOwned);
                            item.setGifted(giftedItemIds.contains(item.getId()));
                            item.setEquipped(isOwned && equippedIds.contains(item.getId()));
                            Long exp = itemExpiryMap.get(item.getId());
                            if (exp != null) {
                                item.setExpiryTimestamp(exp);
                            }
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
        Set<String> seenIds = new HashSet<>();
        Set<String> seenSvgaPaths = new HashSet<>();

        for (StoreItemModel item : catalog) {
            if (item == null || item.getId() == null) continue;

            // Keep ONLY FRAME and ENTRANCE categories
            if (!"FRAME".equalsIgnoreCase(item.getCategory()) && !"ENTRANCE".equalsIgnoreCase(item.getCategory())) {
                continue;
            }

            // Must have a valid non-empty SVGA path
            String svgaPath = item.getSvgaPath();
            if (svgaPath == null || svgaPath.trim().isEmpty()) {
                continue;
            }

            // Strict Deduplication by ID and SVGA path
            String normalizedId = item.getId().toLowerCase().trim();
            String normalizedPath = svgaPath.toLowerCase().trim();

            if (seenIds.contains(normalizedId) || seenSvgaPaths.contains(normalizedPath)) {
                continue;
            }

            if (categoryFilter == null || categoryFilter.equalsIgnoreCase("ALL") || categoryFilter.equalsIgnoreCase(item.getCategory())) {
                seenIds.add(normalizedId);
                seenSvgaPaths.add(normalizedPath);
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

        int validityDays = item.getValidityDays() > 0 ? item.getValidityDays() : 7;
        long now = System.currentTimeMillis();
        long expiryTimestamp = now + (validityDays * 86400000L);

        // Deduct coins using WalletManager
        WalletManager.spendCoinsForGift(uid, null, item.getPriceCoins(), item.getName() + " (" + validityDays + " days)", new WalletManager.WalletCallback() {
            @Override
            public void onSuccess(String message, long newCoinBalance) {
                // Save inventory record with expiryTimestamp
                DatabaseReference invRef = FirebaseDatabase.getInstance().getReference("user_inventory")
                        .child(uid).child(item.getId());

                Map<String, Object> invData = new HashMap<>();
                invData.put("purchasedAt", now);
                invData.put("expiryTimestamp", expiryTimestamp);
                invData.put("validityDays", validityDays);
                invData.put("acquiredFrom", "self_purchase");

                invRef.setValue(invData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        item.setOwned(true);
                        item.setExpiryTimestamp(expiryTimestamp);
                        // Auto equip upon buying
                        equipItem(uid, item, new ActionCallback() {
                            @Override
                            public void onSuccess(String msg) {
                                if (callback != null) callback.onSuccess("🎉 Purchased & Equipped " + item.getName() + " (" + validityDays + " Days)!");
                            }

                            @Override
                            public void onError(String err) {
                                if (callback != null) callback.onSuccess("🎉 Purchased " + item.getName() + " (" + validityDays + " Days)!");
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

    public static void sendItemAsGift(String senderUid, String senderName, String recipientUid, String recipientName, StoreItemModel item, ActionCallback callback) {
        if (senderUid == null || recipientUid == null || item == null) {
            if (callback != null) callback.onError("Invalid parameters");
            return;
        }

        if (senderUid.equals(recipientUid)) {
            if (callback != null) callback.onError("You cannot send a gift to yourself. Click Buy instead.");
            return;
        }

        int validityDays = item.getValidityDays() > 0 ? item.getValidityDays() : 7;
        long now = System.currentTimeMillis();
        long expiryTimestamp = now + (validityDays * 86400000L);

        // Deduct coins from Sender using WalletManager
        WalletManager.spendCoinsForGift(senderUid, recipientUid, item.getPriceCoins(), "Sent Gift: " + item.getName() + " (" + validityDays + " days) to " + (recipientName != null ? recipientName : recipientUid), new WalletManager.WalletCallback() {
            @Override
            public void onSuccess(String message, long newCoinBalance) {
                // Grant inventory item with expiryTimestamp to Recipient
                DatabaseReference recipientInvRef = FirebaseDatabase.getInstance().getReference("user_inventory")
                        .child(recipientUid).child(item.getId());

                Map<String, Object> invData = new HashMap<>();
                invData.put("purchasedAt", now);
                invData.put("expiryTimestamp", expiryTimestamp);
                invData.put("validityDays", validityDays);
                invData.put("acquiredFrom", senderUid);

                recipientInvRef.setValue(invData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Log transaction under recipient's wallet_transactions
                        DatabaseReference recipientTxRef = FirebaseDatabase.getInstance().getReference("wallet_transactions")
                                .child(recipientUid).push();

                        Map<String, Object> txData = new HashMap<>();
                        txData.put("type", "GIFT_RECEIVED");
                        txData.put("title", "Received Gift: " + item.getName());
                        txData.put("description", "Received " + item.getName() + " (" + validityDays + " days) from " + (senderName != null ? senderName : "a friend"));
                        txData.put("amount", 0);
                        txData.put("quantity", 1);
                        txData.put("timestamp", now);
                        txData.put("giftName", item.getName());

                        recipientTxRef.setValue(txData);

                        // Save to recipient's received_gifts node so it displays under Friend Gifts in CollectionActivity
                        String cleanName = item.getName().replaceAll("\\s*\\(.*?\\)", "").trim();
                        DatabaseReference recipientGiftsRef = FirebaseDatabase.getInstance().getReference("users")
                                .child(recipientUid).child("received_gifts").child(cleanName);

                        recipientGiftsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot gSnap) {
                                long currentCount = 0;
                                if (gSnap.exists()) {
                                    Long countObj = gSnap.getValue(Long.class);
                                    if (countObj != null) {
                                        currentCount = countObj;
                                    }
                                }
                                recipientGiftsRef.setValue(currentCount + 1);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });

                        // 1. Send Notification to Recipient
                        String sName = (senderName != null && !senderName.trim().isEmpty()) ? senderName.trim() : "A Friend";
                        String rName = (recipientName != null && !recipientName.trim().isEmpty()) ? recipientName.trim() : recipientUid;
                        String recipientNotifMsg = "🎁 " + sName + " sent you " + item.getName() + " (" + validityDays + " days valid) to " + rName + " (ID: " + recipientUid + ")!";
                        NotificationHelper.sendNotification(recipientUid, "Gift Received 🎁", recipientNotifMsg, "GIFT", senderUid);

                        // 2. Send Confirmation Notification to Sender
                        String senderNotifMsg = "🎁 You sent " + item.getName() + " (" + validityDays + " days valid) to " + rName + " (ID: " + recipientUid + ")!";
                        NotificationHelper.sendNotification(senderUid, "Gift Delivered 🎁", senderNotifMsg, "GIFT", recipientUid);

                        // 3. Broadcast Global Realtime SVGA Banner Broadcast across entire app
                        UserProfileCache.getUserProfile(senderUid, senderProfile -> {
                            String sAvatar = (senderProfile != null && senderProfile.avatarUrl != null) ? senderProfile.avatarUrl : "";
                            GlobalBroadcastHelper.broadcastGift(
                                    senderUid,
                                    sName,
                                    sAvatar,
                                    recipientUid,
                                    rName,
                                    item.getName() + " (" + validityDays + " days)",
                                    1,
                                    "Virtual Store"
                            );
                        });

                        if (callback != null) callback.onSuccess("🎁 Sent " + item.getName() + " (" + validityDays + " Days) to " + rName + "!");
                    } else {
                        if (callback != null) callback.onError("Failed to deliver gift to recipient.");
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

        long now = System.currentTimeMillis();
        long expiryTimestamp = now + (7 * 86400000L); // 7 days reward validity

        Map<String, Object> invData = new HashMap<>();
        invData.put("purchasedAt", now);
        invData.put("expiryTimestamp", expiryTimestamp);
        invData.put("validityDays", 7);
        invData.put("acquiredFrom", "daily_checkin");

        invRef.setValue(invData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (callback != null) callback.onSuccess("Item unlocked successfully!");
            } else {
                if (callback != null) callback.onError("Failed to record inventory item.");
            }
        });
    }

    private static List<StoreItemModel> seedDefaultItems() {
        List<StoreItemModel> items = new ArrayList<>();
        
        // Profile Banner / Frames - Exact Prices from Screenshots & Matched Drawables
        items.add(new StoreItemModel("frame_champion", "Champion Frame 🏆", "FRAME", 200000L, "Glorious Champion Frame", "test_frame", "CHAMPION 👑", "frame/champion_frame.svga"));
        items.add(new StoreItemModel("frame_crown_circle", "Crown Circle 👑", "FRAME", 400000L, "Royal Crown Ring Frame", "crown_circle", "ROYAL ✨", "frame/crown_circle.svga"));
        items.add(new StoreItemModel("frame_diamond_glow", "Diamond Glow ✨", "FRAME", 700000L, "Glowing Diamond Energy Frame", "dimond_glow_", "HOT 🔥", "frame/diamond_glow.svga"));
        items.add(new StoreItemModel("frame_star_ring", "Star Ring ✨", "FRAME", 1000000L, "Shining Star Ring Frame", "star_ring", "POPULAR", "frame/star_ring.svga"));
        items.add(new StoreItemModel("frame_crystal", "Crystal Frame 💎", "FRAME", 1500000L, "Sparkling Crystal Border", "cristal_frame", "CRYSTAL 💎", "frame/crystal_frame.svga"));
        items.add(new StoreItemModel("frame_crystal_ring", "Crystal Ring 💍", "FRAME", 1500000L, "Radiant Crystal Ring Frame", "cristal_ring", "NEW 🔥", "frame/crystal_ring.svga"));
        items.add(new StoreItemModel("frame_fire_ring", "Fire Ring 💥", "FRAME", 1500000L, "Blazing Fire Ring Frame", "fire_ring_frame", "FIRE 🔥", "frame/fire_ring_frame.svga"));
        items.add(new StoreItemModel("frame_lion_glory", "Lion Glory 🦁", "FRAME", 1500000L, "Glorious Lion Spirit Frame", "lion_glory", "LION 🦁", "frame/lion_glory.svga"));

        items.add(new StoreItemModel("frame_music_ring", "Music Ring 🎵", "FRAME", 2000000L, "Rhythmic Music Ring Frame", "music_ring", "MUSIC 🎵", "frame/music_ring.svga"));
        items.add(new StoreItemModel("frame_purple_star", "Purple Star 🌟", "FRAME", 2000000L, "Purple Starburst Frame", "purple_thunder_", "STAR 🌟", "frame/purple_star.svga"));
        items.add(new StoreItemModel("frame_golden_beast", "Golden Beast 🦁", "FRAME", 2000000L, "Golden Beast Aura Frame", "golden_beast", "VIP 👑", "frame/golden_beast.svga"));
        items.add(new StoreItemModel("frame_flame_lion", "Flame Lion 🦁", "FRAME", 2000000L, "Mighty Flame Lion Frame", "flame_lion", "LION 🦁", "frame/flame_lion.svga"));
        items.add(new StoreItemModel("frame_imperial_glory", "Imperial Glory 👑", "FRAME", 2000000L, "Imperial Royal Glory Frame", "imperial_glory", "IMPERIAL 🌟", "frame/imperial_glory.svga"));
        items.add(new StoreItemModel("frame_golden_emperor", "Golden Emperor 👑", "FRAME", 2000000L, "Majestic Golden Emperor Frame", "golden_emperor", "EMPEROR 👑", "frame/golden_emperor.svga"));
        items.add(new StoreItemModel("frame_golden_wings", "Golden Wings 🪽", "FRAME", 2000000L, "Shining Golden Wings Frame", "golden_wings", "WINGS 🪽", "frame/golden_wings.svga"));
        items.add(new StoreItemModel("frame_majestic_aura", "Majestic Aura 🌟", "FRAME", 2000000L, "Majestic Glowing Aura Frame", "majestic_aura_frame", "FEATURED", "frame/majestic_aura.svga"));

        items.add(new StoreItemModel("frame_inferno_crown", "Inferno Crown 🔥", "FRAME", 2500000L, "Inferno Flame Crown Frame", "inferno_crown", "HOT 🔥", "frame/inferno_crown.svga"));
        items.add(new StoreItemModel("frame_nature_ring", "Nature Ring 🌿", "FRAME", 3000000L, "Fresh Nature Ring Frame", "nature_ring", "NATURE 🌿", "frame/nature_ring.svga"));
        items.add(new StoreItemModel("frame_vip_1", "VIP 1 Frame 👑", "FRAME", 3000000L, "VIP Level 1 Avatar Frame", "vip_1_", "VIP 1", "frame/vip_1.svga"));
        items.add(new StoreItemModel("frame_purple_thunder", "Purple Thunder ⚡", "FRAME", 3000000L, "Electric Purple Thunder Frame", "purple_thunder", "THUNDER ⚡", "frame/purple_thunder.svga"));

        items.add(new StoreItemModel("frame_ice_crystal", "Ice Crystal ❄️", "FRAME", 3500000L, "Cool Ice Crystal Frame", "ice_cristal", "COOL ❄️", "frame/ice_crystal.svga"));
        items.add(new StoreItemModel("frame_dragon", "Dragon Flame 🐉", "FRAME", 3500000L, "Fiery Dragon Frame", "dragon_frame", "EPIC 🐲", "frame/dragon_frame.svga"));
        items.add(new StoreItemModel("frame_vip_2", "VIP 2 Frame 👑", "FRAME", 3500000L, "VIP Level 2 Avatar Frame", "vip_2", "VIP 2", "frame/vip_2.svga"));

        items.add(new StoreItemModel("frame_vip_3", "VIP 3 Frame 👑", "FRAME", 4000000L, "VIP Level 3 Avatar Frame", "vip3", "VIP 3", "frame/vip_3.svga"));
        items.add(new StoreItemModel("frame_vip_4", "VIP 4 Frame 👑", "FRAME", 4500000L, "VIP Level 4 Avatar Frame", "vip_4", "VIP 4", "frame/vip_4.svga"));

        items.add(new StoreItemModel("frame_purple_mask", "Purple Mask 🎭", "FRAME", 5000000L, "Mysterious Purple Mask Frame", "purple_mask_frame", "MYSTIC 🎭", "frame/purple_mask.svga"));
        items.add(new StoreItemModel("frame_vip_5", "VIP 5 Frame 👑", "FRAME", 5000000L, "VIP Level 5 Avatar Frame", "vip_4", "VIP 5", "frame/vip_5.svga"));

        items.add(new StoreItemModel("frame_star_crown", "Star Crown 👑", "FRAME", 6000000L, "Star Crown Frame", "star_crown", "CROWN 👑", "frame/star_crown.svga"));
        items.add(new StoreItemModel("frame_vip_6", "VIP 6 Frame 👑", "FRAME", 6000000L, "VIP Level 6 Avatar Frame", "vip_6", "VIP 6", "frame/vip_6.svga"));

        items.add(new StoreItemModel("frame_vip_7", "VIP 7 Frame 👑", "FRAME", 7000000L, "Ultimate VIP Level 7 Frame", "vip_7", "VIP 7 🔥", "frame/vip_7.svga"));
        items.add(new StoreItemModel("frame_rank_3", "Rank 3 Bronze Frame 🥉", "FRAME", 7500000L, "Top 3 Leaderboard Rank Frame", "frame_rank_3", "RANK 3 🥉", "frame/frame_rank_3.svga"));
        items.add(new StoreItemModel("frame_rank_2", "Rank 2 Silver Frame 🥈", "FRAME", 8000000L, "Top 2 Leaderboard Rank Frame", "fram_rank_2", "RANK 2 🥈", "frame/frame_rank_2.svga"));
        items.add(new StoreItemModel("frame_rank_1", "Rank 1 Gold Frame 🥇", "FRAME", 9000000L, "Top 1 Leaderboard Rank Frame", "frame_rank_1", "RANK 1 🥇", "frame/frame_rank_1.svga"));

        // Entrances / Rides - Exact Prices from Screenshots & Valid SVGA Assets
        items.add(new StoreItemModel("entrance_toyota_car", "Toyota Car Entrance 🚗", "ENTRANCE", 8000000L, "Cruising into rooms in Toyota Car", "ic_entrance_toyota_car", "POPULAR", "Entry/toyota_car_entry.svga"));
        items.add(new StoreItemModel("entrance_red_car", "Red Super Car 🏎️", "ENTRANCE", 8000000L, "Arrive in style with Red Super Car", "ic_entrance_red_car", "HOT 🔥", "Entry/red_super_car.svga"));
        items.add(new StoreItemModel("entrance_anime_man", "Anime Legend Arrival ⚡", "ENTRANCE", 10000000L, "Enter rooms with Anime Hero Arrival", "ic_entrance_anime_man", "EPIC ⚡", "Entry/anime_man_entry.svga"));
        // DAILY CHECK-IN STORE REWARD
        items.add(new StoreItemModel("entrance_golden_super_car", "Golden Super Car 🏎️", "ENTRANCE", 12000000L, "Arrive like royalty in Golden Super Car", "ic_entrance_golden_car", "LEGENDARY 👑", "Entry/golden_super_car.svga"));

        return items;
    }
}
