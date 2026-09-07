package com.roomchatapps.Pmishra.utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.ReferralModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReferralManager {

    public interface CodeCallback {
        void onCodeReady(String code);
        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface ReferralsListCallback {
        void onReferralsLoaded(List<ReferralModel> referrals, long totalCoinsEarned);
        void onError(String error);
    }

    public static void getOrCreateReferralCode(String uid, CodeCallback callback) {
        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onError("Invalid user ID");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.child("referralCode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    String code = String.valueOf(snapshot.getValue());
                    if (callback != null) callback.onCodeReady(code);
                } else {
                    String newCode = generateCode(uid);
                    DatabaseReference codeRef = FirebaseDatabase.getInstance().getReference("referral_codes").child(newCode);

                    codeRef.setValue(uid).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            userRef.child("referralCode").setValue(newCode);
                            if (callback != null) callback.onCodeReady(newCode);
                        } else {
                            if (callback != null) callback.onError("Failed to generate referral code.");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    private static String generateCode(String uid) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder("ROOM");
        for (int i = 0; i < 3; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static void applyReferralCode(String currentUid, String rawCodeInput, ActionCallback callback) {
        if (currentUid == null || rawCodeInput == null || rawCodeInput.trim().isEmpty()) {
            if (callback != null) callback.onError("Please enter a valid referral code");
            return;
        }

        String code = rawCodeInput.trim().toUpperCase();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
        userRef.child("usedReferralCode").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (callback != null) callback.onError("You have already claimed a referral bonus code!");
                    return;
                }

                // Check if code exists in database
                DatabaseReference codeRef = FirebaseDatabase.getInstance().getReference("referral_codes").child(code);
                codeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot codeSnap) {
                        if (!codeSnap.exists() || codeSnap.getValue() == null) {
                            if (callback != null) callback.onError("Invalid referral code. Please check and try again.");
                            return;
                        }

                        String referrerUid = String.valueOf(codeSnap.getValue());
                        if (referrerUid.equals(currentUid)) {
                            if (callback != null) callback.onError("You cannot use your own referral code!");
                            return;
                        }

                        // Code valid -> process rewards
                        processReferralRewards(currentUid, referrerUid, code, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (callback != null) callback.onError(error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    private static void processReferralRewards(String currentUid, String referrerUid, String code, ActionCallback callback) {
        // Mark current user as redeemed
        FirebaseDatabase.getInstance().getReference("users").child(currentUid).child("usedReferralCode").setValue(code);

        // Reward current user (+100 coins)
        WalletManager.addCoins(currentUid, 100, "Referral Welcome Bonus", null);

        // Reward referrer (+200 coins)
        WalletManager.addCoins(referrerUid, 200, "Friend Invited", null);

        // Fetch current user info to add to referrer's list
        FirebaseDatabase.getInstance().getReference("users").child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = "New User";
                        String avatar = "";
                        if (snapshot.exists()) {
                            String n = snapshot.child("name").getValue(String.class);
                            String a = snapshot.child("avtar").getValue(String.class);
                            if (n != null && !n.isEmpty()) name = n;
                            if (a != null && !a.isEmpty()) avatar = a;
                        }

                        DatabaseReference refRef = FirebaseDatabase.getInstance().getReference("referrals").child(referrerUid).child(currentUid);
                        ReferralModel model = new ReferralModel(currentUid, currentUid, name, avatar, 200, System.currentTimeMillis());
                        refRef.setValue(model);

                        // Push notification to referrer
                        NotificationHelper.sendSystemNotification(referrerUid, "Referral Bonus Claimed! 🎉", name + " joined using your code! +200 Coins added to wallet.");

                        if (callback != null) callback.onSuccess("🎉 Referral code applied! +100 Coins added to your wallet.");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (callback != null) callback.onSuccess("🎉 Referral code applied! +100 Coins added to your wallet.");
                    }
                });
    }

    public static void loadUserReferrals(String uid, ReferralsListCallback callback) {
        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onError("Invalid user ID");
            return;
        }

        DatabaseReference refRef = FirebaseDatabase.getInstance().getReference("referrals").child(uid);
        refRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ReferralModel> list = new ArrayList<>();
                long totalEarned = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        ReferralModel item = ds.getValue(ReferralModel.class);
                        if (item != null) {
                            if (item.getId() == null) item.setId(ds.getKey());
                            list.add(item);
                            totalEarned += item.getRewardCoins();
                        }
                    }
                    list.sort((r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
                }

                if (callback != null) callback.onReferralsLoaded(list, totalEarned);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    public static void shareReferralInvite(Context context, String referralCode, String userName) {
        if (context == null || referralCode == null) return;

        String shareText = "🎉 Join me on Room Chat! Voice rooms, live chat, and fun games await.\n\n" +
                "Use my referral code: *" + referralCode + "* to get 100 FREE Coins on signup! 🪙\n\n" +
                "Download Room Chat now!";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join Room Chat!");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        context.startActivity(Intent.createChooser(shareIntent, "Invite Friends via"));
    }
}
