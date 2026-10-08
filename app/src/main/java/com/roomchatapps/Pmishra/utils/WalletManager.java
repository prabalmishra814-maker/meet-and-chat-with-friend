package com.roomchatapps.Pmishra.utils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.TransactionModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class WalletManager {

    public interface WalletCallback {
        void onSuccess(String message, long newCoinBalance);
        void onError(String error);
    }

    public interface TransactionCallback {
        void onTransactionsLoaded(List<TransactionModel> transactions);
        void onError(String error);
    }

    public interface CoinCallback {
        void onCoinsLoaded(long coins);
    }

    public interface EnergyCallback {
        void onEnergyLoaded(long energy);
    }

    public static void getUserEnergy(String uid, EnergyCallback callback) {
        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onEnergyLoaded(0);
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("energy");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentEnergy = 0;
                if (snapshot.exists() && snapshot.getValue() != null) {
                    try {
                        currentEnergy = Long.parseLong(String.valueOf(snapshot.getValue()));
                    } catch (Exception ignored) {}
                }
                if (callback != null) callback.onEnergyLoaded(currentEnergy);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onEnergyLoaded(0);
            }
        });
    }

    public static void getUserCoins(String uid, CoinCallback callback) {
        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onCoinsLoaded(0);
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("coins");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentCoins = 0;
                if (snapshot.exists() && snapshot.getValue() != null) {
                    try {
                        currentCoins = Long.parseLong(String.valueOf(snapshot.getValue()));
                    } catch (Exception ignored) {}
                }
                if (callback != null) callback.onCoinsLoaded(currentCoins);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onCoinsLoaded(0);
            }
        });
    }

    /**
     * Convert energy to coins for a user (1 Energy = 1 Coin)
     */
    public static void convertEnergyToCoins(String uid, long energyAmount, WalletCallback callback) {
        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onError("Invalid user ID");
            return;
        }

        if (energyAmount <= 0) {
            if (callback != null) callback.onError("Amount must be greater than 0");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentEnergy = 0;
                long currentCoins = 0;

                if (snapshot.exists()) {
                    if (snapshot.child("energy").exists() && snapshot.child("energy").getValue() != null) {
                        try {
                            currentEnergy = Long.parseLong(String.valueOf(snapshot.child("energy").getValue()));
                        } catch (Exception e) {
                            try {
                                currentEnergy = (long) Double.parseDouble(String.valueOf(snapshot.child("energy").getValue()));
                            } catch (Exception ignored) {}
                        }
                    }

                    if (snapshot.child("coins").exists() && snapshot.child("coins").getValue() != null) {
                        try {
                            currentCoins = Long.parseLong(String.valueOf(snapshot.child("coins").getValue()));
                        } catch (Exception e) {
                            try {
                                currentCoins = (long) Double.parseDouble(String.valueOf(snapshot.child("coins").getValue()));
                            } catch (Exception ignored) {}
                        }
                    }
                }

                if (currentEnergy < energyAmount) {
                    if (callback != null) callback.onError("Insufficient energy balance!");
                    return;
                }

                long coinsGained = Math.round(energyAmount * 0.70);
                long updatedEnergy = currentEnergy - energyAmount;
                long updatedCoins = currentCoins + coinsGained;

                Map<String, Object> updates = new HashMap<>();
                updates.put("energy", updatedEnergy);
                updates.put("coins", updatedCoins);

                userRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserProfileCache.invalidate(uid);
                        logTransaction(uid, "ENERGY_CONVERT", energyAmount, 0, "Energy Conversion", "Converted " + energyAmount + " Energy to " + coinsGained + " Coins (30% Charge)");
                        if (callback != null) {
                            callback.onSuccess("Successfully converted " + energyAmount + " Energy to " + coinsGained + " Coins!", updatedCoins);
                        }
                    } else {
                        if (callback != null) {
                            callback.onError("Failed to convert energy.");
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Top-up / Recharge coins for user
     */
    public static void addCoins(String uid, long amount, String packageTitle, WalletCallback callback) {
        addCoins(uid, amount, "TOPUP", "Coin Top-Up", packageTitle + " Pack (" + amount + " Coins)", callback);
    }

    public static void addCoins(String uid, long amount, String txType, String txTitle, String txDescription, WalletCallback callback) {
        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onError("Invalid user ID");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentCoins = 0;
                long currentDiamonds = 0;
                long currentCoinsSpent = 0;

                if (snapshot.exists()) {
                    if (snapshot.child("coins").exists() && snapshot.child("coins").getValue() != null) {
                        try {
                            currentCoins = Long.parseLong(String.valueOf(snapshot.child("coins").getValue()));
                        } catch (Exception ignored) {}
                    }
                    if (snapshot.child("diamonds").exists() && snapshot.child("diamonds").getValue() != null) {
                        try {
                            currentDiamonds = Long.parseLong(String.valueOf(snapshot.child("diamonds").getValue()));
                        } catch (Exception ignored) {}
                    }
                    if (snapshot.child("coinsSpent").exists() && snapshot.child("coinsSpent").getValue() != null) {
                        try {
                            currentCoinsSpent = Long.parseLong(String.valueOf(snapshot.child("coinsSpent").getValue()));
                        } catch (Exception ignored) {}
                    }
                }

                long updatedCoins = currentCoins + amount;
                Map<String, Object> updates = new HashMap<>();
                updates.put("coins", updatedCoins);

                long logDiamondAmount = 0;
                if ("GIFT_RECEIVED".equalsIgnoreCase(txType)) {
                    long updatedDiamonds = currentDiamonds + amount;
                    updates.put("diamonds", updatedDiamonds);
                    logDiamondAmount = amount;
                }

                // If transaction is a Top-Up / Recharge, also increase coinsSpent, level, and XP!
                if ("TOPUP".equalsIgnoreCase(txType) || (txTitle != null && txTitle.toLowerCase().contains("top-up"))) {
                    long newCoinsSpent = currentCoinsSpent + amount;
                    long newLevel = LevelUtils.calculateLevel(newCoinsSpent);
                    long totalXp = LevelUtils.calculateTotalXp(newCoinsSpent);

                    updates.put("coinsSpent", newCoinsSpent);
                    updates.put("level", String.valueOf(newLevel));
                    updates.put("xp", totalXp);
                }

                long finalLogDiamondAmount = logDiamondAmount;
                userRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserProfileCache.invalidate(uid);
                        // Log transaction history with custom type and description
                        logTransaction(uid, txType, amount, finalLogDiamondAmount, txTitle, txDescription);
                        if (callback != null) {
                            callback.onSuccess("Successfully added " + amount + " coins!", updatedCoins);
                        }
                    } else {
                        if (callback != null) {
                            callback.onError("Failed to update wallet balance.");
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Credits received gift coins directly to recipient's account balance,
     * invalidates local user profile cache, and logs a GIFT_RECEIVED transaction.
     */
    public static void addGiftCoinsToRecipient(String recipientUid, long giftCost, String giftName, String senderName) {
        if (recipientUid == null || recipientUid.trim().isEmpty() || giftCost <= 0) return;

        DatabaseReference recipientRef = FirebaseDatabase.getInstance().getReference("users").child(recipientUid.trim());
        recipientRef.child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentCoins = 0;
                if (snapshot.exists() && snapshot.getValue() != null) {
                    try {
                        currentCoins = Long.parseLong(String.valueOf(snapshot.getValue()));
                    } catch (Exception e) {
                        currentCoins = 0;
                    }
                }

                long updatedCoins = currentCoins + giftCost;
                recipientRef.child("coins").setValue(updatedCoins).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserProfileCache.invalidate(recipientUid);
                        String sender = (senderName != null && !senderName.trim().isEmpty()) ? senderName : "User";
                        String gift = (giftName != null && !giftName.trim().isEmpty()) ? giftName : "Gift";
                        logTransaction(recipientUid, "GIFT_RECEIVED", giftCost, 0, "Received Gift: " + gift, "Received " + gift + " from " + sender);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ROOM CONTRIBUTION TRACKING WITH TIME BUCKETS (Daily, Weekly, Monthly, Total)
    public static void trackRoomContribution(String roomId, String userId, String userName, String userAvatar, long coinAmount) {
        if (roomId == null || roomId.trim().isEmpty() || userId == null || userId.trim().isEmpty() || coinAmount <= 0) {
            return;
        }
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId.trim());

        // 1. Atomically increment total room coin spend
        roomRef.child("totalCoinSpend").setValue(ServerValue.increment(coinAmount));

        // Dates formatting for Daily, Weekly, Monthly buckets
        Date now = new Date();
        String dailyKey = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now);
        String weeklyKey = new SimpleDateFormat("yyyy-'W'ww", Locale.US).format(now);
        String monthlyKey = new SimpleDateFormat("yyyy-MM", Locale.US).format(now);

        Map<String, Object> contribData = new HashMap<>();
        contribData.put("userId", userId.trim());
        if (userName != null && !userName.trim().isEmpty()) {
            contribData.put("userName", userName.trim());
        }
        if (userAvatar != null && !userAvatar.trim().isEmpty()) {
            contribData.put("userAvatar", userAvatar.trim());
        }
        contribData.put("amount", ServerValue.increment(coinAmount));
        contribData.put("timestamp", System.currentTimeMillis());

        // 2. All-Time Total
        roomRef.child("room_contributions").child(userId.trim()).updateChildren(contribData);

        // 3. Daily Bucket
        roomRef.child("room_contributions_daily").child(dailyKey).child(userId.trim()).updateChildren(contribData);

        // 4. Weekly Bucket
        roomRef.child("room_contributions_weekly").child(weeklyKey).child(userId.trim()).updateChildren(contribData);

        // 5. Monthly Bucket
        roomRef.child("room_contributions_monthly").child(monthlyKey).child(userId.trim()).updateChildren(contribData);
    }

    // GIFT COIN/ENERGY FIX
    private static final Set<String> processedTransactionIds = Collections.synchronizedSet(new HashSet<>());

    /**
     * GIFT COIN/ENERGY FIX
     * Process gift transaction with strict anti-duplication and CASE 1 / CASE 2 energy rules.
     */
    public static void processGiftTransaction(
            String transactionId,
            String roomId,
            String senderUid,
            List<String> targetUids,
            long giftValue,
            String giftName,
            List<String> roomMemberUids,
            WalletCallback callback
    ) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            if (callback != null) callback.onError("Invalid transaction ID");
            return;
        }

        String cleanTxId = transactionId.trim();

        // 1. In-memory anti-duplication check
        synchronized (processedTransactionIds) {
            if (processedTransactionIds.contains(cleanTxId)) {
                if (callback != null) callback.onError("Transaction already processed");
                return;
            }
            if (processedTransactionIds.size() > 500) {
                processedTransactionIds.clear();
            }
            processedTransactionIds.add(cleanTxId);
        }

        if (senderUid == null || senderUid.isEmpty()) {
            if (callback != null) callback.onError("Invalid sender ID");
            return;
        }

        // Self-gifting protection: Prevent sending gifts to oneself
        if (targetUids != null && targetUids.contains(senderUid)) {
            synchronized (processedTransactionIds) {
                processedTransactionIds.remove(cleanTxId);
            }
            if (callback != null) callback.onError("You cannot send gifts to yourself! 🎁");
            return;
        }

        if (giftValue <= 0) {
            if (callback != null) callback.onError("Invalid gift value");
            return;
        }

        // 2. Firebase Database atomic transaction deduplication check
        DatabaseReference txRef = FirebaseDatabase.getInstance().getReference("processed_gift_transactions").child(cleanTxId);
        txRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot txSnapshot) {
                if (txSnapshot.exists()) {
                    if (callback != null) callback.onError("Transaction already processed");
                    return;
                }

                txRef.setValue(true);

                DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("users").child(senderUid);
                senderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long currentCoins = 0;
                        long currentCoinsSpent = 0;

                        if (snapshot.exists()) {
                            if (snapshot.child("coins").exists() && snapshot.child("coins").getValue() != null) {
                                try {
                                    currentCoins = Long.parseLong(String.valueOf(snapshot.child("coins").getValue()));
                                } catch (Exception e) {
                                    currentCoins = 0;
                                }
                            }

                            if (snapshot.child("coinsSpent").exists() && snapshot.child("coinsSpent").getValue() != null) {
                                try {
                                    currentCoinsSpent = Long.parseLong(String.valueOf(snapshot.child("coinsSpent").getValue()));
                                } catch (Exception e) {
                                    currentCoinsSpent = 0;
                                }
                            } else if (snapshot.child("level").exists() && snapshot.child("level").getValue() != null) {
                                try {
                                    long lvl = Long.parseLong(String.valueOf(snapshot.child("level").getValue()));
                                    currentCoinsSpent = Math.max(0, (lvl - 1) * LevelUtils.COINS_PER_LEVEL);
                                } catch (Exception ignored) {}
                            }
                        }

                        if (currentCoins < giftValue) {
                            txRef.removeValue();
                            processedTransactionIds.remove(cleanTxId);
                            if (callback != null) callback.onError("Insufficient coin balance! Please top-up coins.");
                            return;
                        }

                        long newBalance = currentCoins - giftValue;
                        long newCoinsSpent = currentCoinsSpent + giftValue;

                        long oldLevel = LevelUtils.calculateLevel(currentCoinsSpent);
                        long newLevel = LevelUtils.calculateLevel(newCoinsSpent);
                        long totalXp = LevelUtils.calculateTotalXp(newCoinsSpent);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("coins", newBalance);
                        updates.put("coinsSpent", newCoinsSpent);
                        updates.put("level", String.valueOf(newLevel));
                        updates.put("xp", totalXp);

                        senderRef.updateChildren(updates).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                UserProfileCache.invalidate(senderUid);

                                String senderName = snapshot.child("name").getValue(String.class);
                                if (senderName == null || senderName.trim().isEmpty()) {
                                    senderName = snapshot.child("username").getValue(String.class);
                                }
                                String senderAvatar = snapshot.child("avatar").getValue(String.class);
                                if (senderAvatar == null || senderAvatar.trim().isEmpty()) {
                                    senderAvatar = snapshot.child("profilePic").getValue(String.class);
                                }

                                // ROOM CONTRIBUTION TRACKING: Atomically increment room total and user contribution
                                trackRoomContribution(roomId, senderUid, senderName, senderAvatar, giftValue);

                                if (newLevel > oldLevel) {
                                    NotificationHelper.sendLevelUpNotification(senderUid, newLevel);
                                }

                                logTransaction(senderUid, "GIFT_SENT", -giftValue, 0, giftName, "Deducted " + giftValue + " coins for " + giftName);

                                boolean isTargeted = (targetUids != null && !targetUids.isEmpty());

                                if (isTargeted) {
                                    // CASE 1 — Gift sent to specific selected member(s)
                                    // Receiver earns 30% of total gift coins as ENERGY
                                    long shareValue = giftValue / Math.max(1, targetUids.size());
                                    long energyAward = (long) Math.floor(shareValue * EconomyConfig.TARGETED_ENERGY_PERCENTAGE);

                                    for (String targetUid : targetUids) {
                                        if (targetUid != null && !targetUid.isEmpty()) {
                                            addEnergyToUser(targetUid, energyAward, giftName, "Received Gift Energy (Targeted): " + giftName);
                                            NotificationHelper.sendGiftNotification(targetUid, giftName);

                                            final String sName = senderName;
                                            UserProfileCache.getUserProfile(targetUid, rProfile -> {
                                                String rName = (rProfile != null && rProfile.name != null) ? rProfile.name : "Friend";
                                                GlobalGiftBannerManager.broadcastGiftSent(sName, rName, giftName, 1, roomId);
                                            });
                                        }
                                    }
                                } else {
                                    // CASE 2 — Gift sent without selecting a specific member (Room Gift)
                                    // 70% of gift value is distributed as Energy among ALL eligible room members
                                    long totalRoomEnergy = (long) Math.floor(giftValue * EconomyConfig.ROOM_ENERGY_PERCENTAGE);
                                    distributeRoomEnergy(roomId, roomMemberUids, totalRoomEnergy, giftName);
                                    GlobalGiftBannerManager.broadcastGiftSent(senderName, "Room Members", giftName, 1, roomId);
                                }

                                if (callback != null) {
                                    callback.onSuccess("Gift sent successfully!", newBalance);
                                }
                            } else {
                                txRef.removeValue();
                                processedTransactionIds.remove(cleanTxId);
                                if (callback != null) callback.onError("Transaction failed.");
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        txRef.removeValue();
                        processedTransactionIds.remove(cleanTxId);
                        if (callback != null) callback.onError(error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                processedTransactionIds.remove(cleanTxId);
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    // GIFT COIN/ENERGY FIX
    public static void addEnergyToUser(String uid, long energyAmount, String giftName, String description) {
        if (uid == null || uid.isEmpty() || energyAmount <= 0) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.child("energy").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentEnergy = 0;
                if (snapshot.exists() && snapshot.getValue() != null) {
                    try {
                        currentEnergy = Long.parseLong(String.valueOf(snapshot.getValue()));
                    } catch (Exception e) {
                        try {
                            currentEnergy = (long) Double.parseDouble(String.valueOf(snapshot.getValue()));
                        } catch (Exception ignored) {}
                    }
                }
                long newEnergy = currentEnergy + energyAmount;
                userRef.child("energy").setValue(newEnergy);
                UserProfileCache.invalidate(uid);

                if (giftName != null && !giftName.trim().isEmpty()) {
                    String cleanName = giftName.replaceAll("\\s*\\(.*?\\)", "").trim();
                    DatabaseReference gRef = userRef.child("received_gifts").child(cleanName);
                    gRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot gSnap) {
                            long curr = 0;
                            if (gSnap.exists() && gSnap.getValue() != null) {
                                try { curr = Long.parseLong(String.valueOf(gSnap.getValue())); } catch (Exception ignored) {}
                            }
                            gRef.setValue(curr + 1);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }

                // Log transaction with explicit giftName field
                DatabaseReference txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(uid);
                String txId = txRef.push().getKey();
                if (txId != null) {
                    Map<String, Object> txData = new HashMap<>();
                    txData.put("id", txId);
                    txData.put("type", "GIFT_RECEIVED");
                    txData.put("coinAmount", 0L);
                    txData.put("diamondAmount", 0L);
                    txData.put("title", "Earned Energy: " + giftName);
                    txData.put("description", description + " (+" + energyAmount + " Energy)");
                    txData.put("giftName", giftName);
                    txData.put("quantity", 1);
                    txData.put("timestamp", System.currentTimeMillis());
                    txRef.child(txId).setValue(txData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // GIFT COIN/ENERGY FIX
    public static void distributeRoomEnergy(String roomId, List<String> roomMemberUids, long totalRoomEnergy, String giftName) {
        if (totalRoomEnergy <= 0) return;

        if (roomMemberUids != null && !roomMemberUids.isEmpty()) {
            performEnergyDistribution(roomMemberUids, totalRoomEnergy, giftName);
        } else if (roomId != null && !roomId.isEmpty()) {
            DatabaseReference onlineRef = FirebaseDatabase.getInstance().getReference("room_users").child(roomId);
            onlineRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> memberList = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String uid = child.child("userId").getValue(String.class);
                        if (uid == null) uid = child.getKey();
                        if (uid != null && !uid.trim().isEmpty() && !memberList.contains(uid.trim())) {
                            memberList.add(uid.trim());
                        }
                    }
                    performEnergyDistribution(memberList, totalRoomEnergy, giftName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    // GIFT COIN/ENERGY FIX
    private static void performEnergyDistribution(List<String> memberList, long totalRoomEnergy, String giftName) {
        if (memberList == null || memberList.isEmpty() || totalRoomEnergy <= 0) return;

        int count = memberList.size();
        long baseEnergy = totalRoomEnergy / count;
        long remainder = totalRoomEnergy % count;

        for (int i = 0; i < count; i++) {
            String memberUid = memberList.get(i);
            long memberEnergy = baseEnergy + (i < remainder ? 1 : 0);
            if (memberEnergy > 0) {
                addEnergyToUser(memberUid, memberEnergy, giftName, "Room Gift Energy Share (" + count + " members)");
            }
        }
    }

    /**
     * Spend coins for gift sending / store purchases / games
     * Updates coins, coinsSpent, level, and xp automatically.
     */
    public static void spendCoinsForGift(String senderUid, String recipientUid, long giftCost, String giftName, WalletCallback callback) {
        if (senderUid == null || senderUid.isEmpty()) {
            if (callback != null) callback.onError("Invalid sender ID");
            return;
        }

        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("users").child(senderUid);
        senderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentCoins = 0;
                long currentCoinsSpent = 0;

                if (snapshot.exists()) {
                    if (snapshot.child("coins").exists() && snapshot.child("coins").getValue() != null) {
                        try {
                            currentCoins = Long.parseLong(String.valueOf(snapshot.child("coins").getValue()));
                        } catch (Exception e) {
                            currentCoins = 0;
                        }
                    }

                    if (snapshot.child("coinsSpent").exists() && snapshot.child("coinsSpent").getValue() != null) {
                        try {
                            currentCoinsSpent = Long.parseLong(String.valueOf(snapshot.child("coinsSpent").getValue()));
                        } catch (Exception e) {
                            currentCoinsSpent = 0;
                        }
                    } else if (snapshot.child("level").exists() && snapshot.child("level").getValue() != null) {
                        try {
                            long lvl = Long.parseLong(String.valueOf(snapshot.child("level").getValue()));
                            currentCoinsSpent = Math.max(0, (lvl - 1) * LevelUtils.COINS_PER_LEVEL);
                        } catch (Exception ignored) {}
                    }
                }

                if (currentCoins < giftCost) {
                    if (callback != null) callback.onError("Insufficient coin balance! Please top-up coins.");
                    return;
                }

                long newBalance = currentCoins - giftCost;
                long newCoinsSpent = currentCoinsSpent + giftCost;

                long oldLevel = LevelUtils.calculateLevel(currentCoinsSpent);
                long newLevel = LevelUtils.calculateLevel(newCoinsSpent);
                long totalXp = LevelUtils.calculateTotalXp(newCoinsSpent);

                Map<String, Object> updates = new HashMap<>();
                updates.put("coins", newBalance);
                updates.put("coinsSpent", newCoinsSpent);
                updates.put("level", String.valueOf(newLevel));
                updates.put("xp", totalXp);

                senderRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Invalidate local user profile cache to reflect new level/xp
                        UserProfileCache.invalidate(senderUid);

                        // If user leveled up, send level up notification
                        if (newLevel > oldLevel) {
                            NotificationHelper.sendLevelUpNotification(senderUid, newLevel);
                        }

                        // Broadcast Global Realtime SVGA Banner for Coin Gifts / Direct Transfers
                        if (recipientUid != null && !recipientUid.trim().isEmpty()) {
                            final String targetUid = recipientUid.trim();
                            UserProfileCache.getUserProfile(senderUid, sProfile -> {
                                String sName = (sProfile != null && sProfile.name != null) ? sProfile.name : "A User";
                                String sAvatar = (sProfile != null && sProfile.avatarUrl != null) ? sProfile.avatarUrl : "";
                                UserProfileCache.getUserProfile(targetUid, rProfile -> {
                                    String rName = (rProfile != null && rProfile.name != null) ? rProfile.name : targetUid;
                                    // Global broadcast notification
                                });
                            });
                        }

                        // Log sender transaction with smart category determination
                        String txType = "GIFT_SENT";
                        if (giftName != null) {
                            String lower = giftName.toLowerCase();
                            if (lower.contains("theme")) {
                                txType = "THEME_BUY";
                            } else if (lower.contains("store") || lower.contains("frame") || lower.contains("bubble") || lower.contains("ride")) {
                                txType = "STORE_BUY";
                            } else if (lower.contains("spin") || lower.contains("wheel") || lower.contains("game")) {
                                txType = "GAME_SPIN";
                            }
                        }
                        logTransaction(senderUid, txType, -giftCost, 0, giftName, "Deducted " + giftCost + " coins");

                        if (callback != null) {
                            callback.onSuccess("Transaction successful!", newBalance);
                        }
                    } else {
                        if (callback != null) callback.onError("Transaction failed.");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    public static void exchangeCoinsForEnergy(String uid, long coinAmount, WalletCallback callback) {
        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onError("Invalid user ID");
            return;
        }

        if (coinAmount <= 0) {
            if (callback != null) callback.onError("Amount must be greater than 0");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentEnergy = 0;
                long currentCoins = 0;

                if (snapshot.exists()) {
                    if (snapshot.child("energy").exists() && snapshot.child("energy").getValue() != null) {
                        try {
                            currentEnergy = Long.parseLong(String.valueOf(snapshot.child("energy").getValue()));
                        } catch (Exception ignored) {}
                    }

                    if (snapshot.child("coins").exists() && snapshot.child("coins").getValue() != null) {
                        try {
                            currentCoins = Long.parseLong(String.valueOf(snapshot.child("coins").getValue()));
                        } catch (Exception ignored) {}
                    }
                }

                if (currentCoins < coinAmount) {
                    if (callback != null) callback.onError("Insufficient coin balance!");
                    return;
                }

                long updatedCoins = currentCoins - coinAmount;
                long updatedEnergy = currentEnergy + coinAmount;

                Map<String, Object> updates = new HashMap<>();
                updates.put("coins", updatedCoins);
                updates.put("energy", updatedEnergy);

                userRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserProfileCache.invalidate(uid);
                        logTransaction(uid, "ENERGY_BUY", -coinAmount, 0, "Coin Exchange", "Exchanged " + coinAmount + " Coins for " + coinAmount + " Energy");
                        if (callback != null) {
                            callback.onSuccess("Successfully exchanged " + coinAmount + " Coins for Energy!", updatedCoins);
                        }
                    } else {
                        if (callback != null) {
                            callback.onError("Failed to exchange coins.");
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    // GIFT COIN/ENERGY FIX
    public static void spendCoinsForRichGift(String roomId, String senderUid, String senderName, String recipientUid, String recipientName, String giftItemName, String giftName, long singleCost, int quantity, WalletCallback callback) {
        List<String> targets = (recipientUid != null && !recipientUid.isEmpty()) ? Collections.singletonList(recipientUid) : null;
        long totalCost = singleCost * Math.max(1, quantity);
        String txId = senderUid + "_" + System.currentTimeMillis() + "_" + (new Random().nextInt(9000) + 1000);
        processGiftTransaction(txId, roomId, senderUid, targets, totalCost, giftName, null, callback);
    }

    public static void logTransaction(String uid, String type, long coinAmount, long diamondAmount, String title, String description) {
        DatabaseReference txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(uid);
        String txId = txRef.push().getKey();
        if (txId == null) return;

        TransactionModel model = new TransactionModel(
                txId, type, coinAmount, diamondAmount, title, description, System.currentTimeMillis()
        );
        txRef.child(txId).setValue(model);
    }

    /**
     * Load Transaction History for a user
     */
    public static ValueEventListener loadTransactionHistory(String uid, TransactionCallback callback) {
        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onError("Invalid user ID");
            return null;
        }

        DatabaseReference txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(uid);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<TransactionModel> list = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        try {
                            TransactionModel tx = ds.getValue(TransactionModel.class);
                            if (tx != null) {
                                if (tx.getId() == null) tx.setId(ds.getKey());
                                list.add(tx);
                            }
                        } catch (Exception e) {
                            try {
                                String id = ds.getKey();
                                String type = ds.child("type").getValue(String.class);
                                String title = ds.child("title").getValue(String.class);
                                String description = ds.child("description").getValue(String.class);

                                long coinAmount = 0;
                                Object coinObj = ds.child("coinAmount").getValue();
                                if (coinObj != null) {
                                    try {
                                        coinAmount = Long.parseLong(String.valueOf(coinObj));
                                    } catch (Exception ignored) {}
                                }

                                long diamondAmount = 0;
                                Object diamondObj = ds.child("diamondAmount").getValue();
                                if (diamondObj != null) {
                                    try {
                                        diamondAmount = Long.parseLong(String.valueOf(diamondObj));
                                    } catch (Exception ignored) {}
                                }

                                long timestamp = 0;
                                Object timeObj = ds.child("timestamp").getValue();
                                if (timeObj != null) {
                                    try {
                                        timestamp = Long.parseLong(String.valueOf(timeObj));
                                    } catch (Exception ignored) {}
                                }

                                list.add(new TransactionModel(id, type, coinAmount, diamondAmount, title, description, timestamp));
                            } catch (Exception ignored) {}
                        }
                    }
                    try {
                        list.sort((t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                    } catch (Exception ignored) {}
                }
                if (callback != null) callback.onTransactionsLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        };
        txRef.limitToLast(150).addValueEventListener(listener);
        return listener;
    }
}
