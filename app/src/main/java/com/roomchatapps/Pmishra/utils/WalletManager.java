package com.roomchatapps.Pmishra.utils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.TransactionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                long currentCoinsSpent = 0;

                if (snapshot.exists()) {
                    if (snapshot.child("coins").exists() && snapshot.child("coins").getValue() != null) {
                        try {
                            currentCoins = Long.parseLong(String.valueOf(snapshot.child("coins").getValue()));
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

                // If transaction is a Top-Up / Recharge, also increase coinsSpent, level, and XP!
                if ("TOPUP".equalsIgnoreCase(txType) || (txTitle != null && txTitle.toLowerCase().contains("top-up"))) {
                    long newCoinsSpent = currentCoinsSpent + amount;
                    long newLevel = LevelUtils.calculateLevel(newCoinsSpent);
                    long totalXp = LevelUtils.calculateTotalXp(newCoinsSpent);

                    updates.put("coinsSpent", newCoinsSpent);
                    updates.put("level", String.valueOf(newLevel));
                    updates.put("xp", totalXp);
                }

                userRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserProfileCache.invalidate(uid);
                        logTransaction(uid, txType, amount, 0, txTitle, txDescription);
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
     * Spend coins for gift sending / store purchases / games
     */
    public static void spendCoinsForGift(String senderUid, String recipientUid, long giftCost, String giftName, WalletCallback callback) {
        spendCoinsForRichGift(null, senderUid, null, recipientUid, null, null, giftName, giftCost, 1, callback);
    }

    /**
     * Rich Gift Transaction with Quantity Selection, 10 Lakh Minimum Validation, 30% Energy Awarded to Receiver.
     */
    public static void spendCoinsForRichGift(String roomId, String senderUid, String senderName,
                                             String recipientUid, String recipientName,
                                             String giftId, String giftName,
                                             long singleGiftPrice, int quantity,
                                             WalletCallback callback) {
        if (senderUid == null || senderUid.isEmpty()) {
            if (callback != null) callback.onError("Invalid sender ID");
            return;
        }

        // Enforce Mandatory 10 Lakh Minimum Gift Price Rule
        if (!EconomyConfig.isValidGiftValue(singleGiftPrice)) {
            if (callback != null) callback.onError("Every gift must have a minimum value of 10 Lakh coins!");
            return;
        }

        int validQty = Math.max(1, quantity);
        long totalCoinCost = singleGiftPrice * (long) validQty;

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

                if (currentCoins < totalCoinCost) {
                    if (callback != null) callback.onError("Insufficient coin balance! Please top-up coins.");
                    return;
                }

                long newBalance = currentCoins - totalCoinCost;
                long newCoinsSpent = currentCoinsSpent + totalCoinCost;

                long oldLevel = LevelUtils.calculateLevel(currentCoinsSpent);
                long newLevel = LevelUtils.calculateLevel(newCoinsSpent);
                long totalXp = LevelUtils.calculateTotalXp(newCoinsSpent);

                Map<String, Object> updates = new HashMap<>();
                updates.put("coins", newBalance);
                updates.put("coinsSpent", newCoinsSpent);
                updates.put("level", String.valueOf(newLevel));
                updates.put("xp", totalXp);

                long systemCut = EconomyConfig.calculateSystemCut(totalCoinCost);
                long energyAwarded = EconomyConfig.calculateEnergyAwarded(totalCoinCost);

                senderRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserProfileCache.invalidate(senderUid);

                        if (newLevel > oldLevel) {
                            NotificationHelper.sendLevelUpNotification(senderUid, newLevel);
                        }

                        // Build rich transaction model
                        String txId = FirebaseDatabase.getInstance().getReference().push().getKey();
                        TransactionModel richTx = new TransactionModel(
                                txId, "GIFT_SENT", -totalCoinCost,
                                giftName + (validQty > 1 ? " (x" + validQty + ")" : ""),
                                "Sent " + giftName + " x" + validQty + " (60% Energy Awarded: " + energyAwarded + " ⚡)",
                                System.currentTimeMillis()
                        );
                        richTx.setRoomId(roomId);
                        richTx.setSenderId(senderUid);
                        richTx.setSenderName(senderName);
                        richTx.setReceiverId(recipientUid);
                        richTx.setReceiverName(recipientName);
                        richTx.setGiftId(giftId);
                        richTx.setGiftName(giftName);
                        richTx.setGiftValue(singleGiftPrice);
                        richTx.setQuantity(validQty);
                        richTx.setTotalCoinCost(totalCoinCost);
                        richTx.setSystemCut(systemCut);
                        richTx.setEnergyPercentage(60);
                        richTx.setEnergyAwarded(energyAwarded);

                        logRichTransaction(senderUid, richTx);
                        if (roomId != null && !roomId.isEmpty()) {
                            logRoomTransaction(roomId, richTx);
                        }

                        // Award 60% Energy directly to receiver's energy balance (NO COINS, NO DIAMONDS!)
                        if (recipientUid != null && !recipientUid.isEmpty() && !recipientUid.equals(senderUid)) {
                            addGiftEnergyToRecipient(recipientUid, energyAwarded, giftName, senderName, richTx);
                            NotificationHelper.sendGiftNotification(recipientUid, giftName);
                        }

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

    /**
     * Credits exactly 60% Energy directly to recipient's energy balance (NO COINS, NO DIAMONDS!).
     */
    public static void addGiftEnergyToRecipient(String recipientUid, long energyAwarded, String giftName, String senderName, TransactionModel originalTx) {
        if (recipientUid == null || recipientUid.trim().isEmpty() || energyAwarded <= 0) return;

        DatabaseReference recipientRef = FirebaseDatabase.getInstance().getReference("users").child(recipientUid.trim());
        recipientRef.child("energy").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentEnergy = 0;
                if (snapshot.exists() && snapshot.getValue() != null) {
                    try {
                        currentEnergy = Long.parseLong(String.valueOf(snapshot.getValue()));
                    } catch (Exception e) {
                        currentEnergy = 0;
                    }
                }

                long updatedEnergy = currentEnergy + energyAwarded;
                recipientRef.child("energy").setValue(updatedEnergy).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserProfileCache.invalidate(recipientUid);
                        String sender = (senderName != null && !senderName.trim().isEmpty()) ? senderName : "User";
                        String gift = (giftName != null && !giftName.trim().isEmpty()) ? giftName : "Gift";

                        String rxTxId = FirebaseDatabase.getInstance().getReference().push().getKey();
                        TransactionModel rxTx = new TransactionModel(
                                rxTxId, "GIFT_RECEIVED", 0,
                                "Received Gift: " + gift,
                                "Earned " + energyAwarded + " Energy ⚡ from " + sender + " (60% Gift Energy Rule)",
                                System.currentTimeMillis()
                        );
                        if (originalTx != null) {
                            rxTx.setRoomId(originalTx.getRoomId());
                            rxTx.setSenderId(originalTx.getSenderId());
                            rxTx.setSenderName(sender);
                            rxTx.setReceiverId(recipientUid);
                            rxTx.setGiftId(originalTx.getGiftId());
                            rxTx.setGiftName(gift);
                            rxTx.setQuantity(originalTx.getQuantity());
                            rxTx.setSystemCut(originalTx.getSystemCut());
                            rxTx.setEnergyPercentage(60);
                            rxTx.setEnergyAwarded(energyAwarded);
                        }
                        logRichTransaction(recipientUid, rxTx);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public static void addGiftCoinsToRecipient(String recipientUid, long giftCost, String giftName, String senderName) {
        long energyAwarded = EconomyConfig.calculateEnergyAwarded(giftCost);
        addGiftEnergyToRecipient(recipientUid, energyAwarded, giftName, senderName, null);
    }

    /**
     * Coin -> Energy Exchange System
     */
    public static void exchangeCoinsForEnergy(String uid, long coinsToExchange, WalletCallback callback) {
        if (uid == null || uid.isEmpty() || coinsToExchange <= 0) {
            if (callback != null) callback.onError("Invalid exchange parameters");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentCoins = 0;
                long currentEnergy = 0;

                if (snapshot.exists()) {
                    if (snapshot.child("coins").exists() && snapshot.child("coins").getValue() != null) {
                        try {
                            currentCoins = Long.parseLong(String.valueOf(snapshot.child("coins").getValue()));
                        } catch (Exception ignored) {}
                    }
                    if (snapshot.child("energy").exists() && snapshot.child("energy").getValue() != null) {
                        try {
                            currentEnergy = Long.parseLong(String.valueOf(snapshot.child("energy").getValue()));
                        } catch (Exception ignored) {}
                    }
                }

                if (currentCoins < coinsToExchange) {
                    if (callback != null) callback.onError("Insufficient coins for exchange!");
                    return;
                }

                long energyGained = (long) Math.floor(coinsToExchange * EconomyConfig.COIN_TO_ENERGY_RATE);
                long newCoins = currentCoins - coinsToExchange;
                long newEnergy = currentEnergy + energyGained;

                Map<String, Object> updates = new HashMap<>();
                updates.put("coins", newCoins);
                updates.put("energy", newEnergy);

                userRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserProfileCache.invalidate(uid);
                        logTransaction(uid, "COIN_EXCHANGE", -coinsToExchange, 0,
                                "Coin -> Energy Exchange",
                                "Exchanged " + CoinUtils.formatCoins(coinsToExchange) + " coins for " + energyGained + " Energy");
                        if (callback != null) {
                            callback.onSuccess("Exchanged " + CoinUtils.formatCoins(coinsToExchange) + " coins for " + energyGained + " Energy! ⚡", newCoins);
                        }
                    } else {
                        if (callback != null) callback.onError("Exchange failed. Please try again.");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    public static void logTransaction(String uid, String type, long coinAmount, long diamondAmount, String title, String description) {
        DatabaseReference txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(uid);
        String txId = txRef.push().getKey();
        if (txId == null) return;

        TransactionModel model = new TransactionModel(
                txId, type, coinAmount, title, description, System.currentTimeMillis()
        );
        txRef.child(txId).setValue(model);
    }

    public static void logRichTransaction(String uid, TransactionModel model) {
        if (uid == null || uid.isEmpty() || model == null) return;
        DatabaseReference txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(uid);
        String txId = model.getId() != null ? model.getId() : txRef.push().getKey();
        if (txId == null) return;
        model.setId(txId);
        txRef.child(txId).setValue(model);
    }

    public static void logRoomTransaction(String roomId, TransactionModel model) {
        if (roomId == null || roomId.isEmpty() || model == null) return;
        DatabaseReference roomTxRef = FirebaseDatabase.getInstance().getReference("room_transactions").child(roomId);
        String txId = model.getId() != null ? model.getId() : roomTxRef.push().getKey();
        if (txId == null) return;
        model.setId(txId);
        roomTxRef.child(txId).setValue(model);
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

                                long timestamp = 0;
                                Object timeObj = ds.child("timestamp").getValue();
                                if (timeObj != null) {
                                    try {
                                        timestamp = Long.parseLong(String.valueOf(timeObj));
                                    } catch (Exception ignored) {}
                                }

                                list.add(new TransactionModel(id, type, coinAmount, title, description, timestamp));
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
