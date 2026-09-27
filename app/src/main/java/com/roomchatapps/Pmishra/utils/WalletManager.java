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
        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onError("Invalid user ID");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
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

                long updatedCoins = currentCoins + amount;
                userRef.child("coins").setValue(updatedCoins).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Log transaction history
                        logTransaction(uid, "TOPUP", amount, 0, "Coin Top-Up", packageTitle + " Pack (" + amount + " Coins)");
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

                        // Add diamonds to recipient if recipient exists
                        if (recipientUid != null && !recipientUid.isEmpty() && !recipientUid.equals(senderUid)) {
                            addDiamondsToRecipient(recipientUid, giftCost, giftName);
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

    private static void addDiamondsToRecipient(String recipientUid, long giftCost, String giftName) {
        DatabaseReference recipientRef = FirebaseDatabase.getInstance().getReference("users").child(recipientUid);
        recipientRef.child("diamonds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentDiamonds = 0;
                if (snapshot.exists() && snapshot.getValue() != null) {
                    try {
                        currentDiamonds = Long.parseLong(String.valueOf(snapshot.getValue()));
                    } catch (Exception e) {
                        currentDiamonds = 0;
                    }
                }
                long newDiamonds = currentDiamonds + giftCost;
                recipientRef.child("diamonds").setValue(newDiamonds);
                logTransaction(recipientUid, "GIFT_RECEIVED", 0, giftCost, "Received Gift: " + giftName, "Earned " + giftCost + " diamonds");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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
    public static void loadTransactionHistory(String uid, TransactionCallback callback) {
        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onError("Invalid user ID");
            return;
        }

        DatabaseReference txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(uid);
        txRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<TransactionModel> list = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        TransactionModel tx = ds.getValue(TransactionModel.class);
                        if (tx != null) {
                            if (tx.getId() == null) tx.setId(ds.getKey());
                            list.add(tx);
                        }
                    }
                    list.sort((t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                }
                if (callback != null) callback.onTransactionsLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }
}
