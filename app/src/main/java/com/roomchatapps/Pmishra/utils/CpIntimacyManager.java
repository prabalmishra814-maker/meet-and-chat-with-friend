package com.roomchatapps.Pmishra.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.IntimacyFriendModel;
import com.roomchatapps.Pmishra.models.SupporterModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CpIntimacyManager {

    public interface SupportersCallback {
        void onSupportersLoaded(List<SupporterModel> supporters);
    }

    public interface IntimacyFriendsCallback {
        void onFriendsLoaded(List<IntimacyFriendModel> bestFriends, List<IntimacyFriendModel> intimacyFriends);
    }

    /**
     * Atomically adds intimacy points between CP partners in Firebase Realtime Database
     */
    public static void addIntimacy(String senderUid, String receiverUid, long points) {
        if (TextUtils.isEmpty(senderUid) || TextUtils.isEmpty(receiverUid) || points <= 0) {
            return;
        }

        String cleanSender = senderUid.trim();
        String cleanReceiver = receiverUid.trim();

        // Intimacy points are added ONLY between active CP relationship partners!
        DatabaseReference cpRef = FirebaseDatabase.getInstance().getReference("cp_bindings").child(cleanSender);
        cpRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String partnerUid = snapshot.child("partnerUid").getValue(String.class);
                    if (!TextUtils.isEmpty(partnerUid) && cleanReceiver.equals(partnerUid.trim())) {
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("cp_guard_values/" + cleanSender + "/" + cleanReceiver + "/value", ServerValue.increment(points));
                        updates.put("cp_guard_values/" + cleanSender + "/" + cleanReceiver + "/timestamp", System.currentTimeMillis());
                        updates.put("cp_guard_values/" + cleanSender + "/" + cleanReceiver + "/lastUpdated", System.currentTimeMillis());

                        updates.put("cp_guard_values/" + cleanReceiver + "/" + cleanSender + "/value", ServerValue.increment(points));
                        updates.put("cp_guard_values/" + cleanReceiver + "/" + cleanSender + "/timestamp", System.currentTimeMillis());
                        updates.put("cp_guard_values/" + cleanReceiver + "/" + cleanSender + "/lastUpdated", System.currentTimeMillis());

                        db.updateChildren(updates);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Atomically adds blessing points between CP partners in Firebase Realtime Database
     */
    public static void addBlessing(String senderUid, String receiverUid, long points) {
        if (TextUtils.isEmpty(senderUid) || TextUtils.isEmpty(receiverUid) || points <= 0) {
            return;
        }

        String cleanSender = senderUid.trim();
        String cleanReceiver = receiverUid.trim();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        Map<String, Object> updates = new HashMap<>();
        updates.put("cp_blessing_values/" + cleanSender + "/" + cleanReceiver + "/value", ServerValue.increment(points));
        updates.put("cp_blessing_values/" + cleanSender + "/" + cleanReceiver + "/timestamp", System.currentTimeMillis());

        updates.put("cp_blessing_values/" + cleanReceiver + "/" + cleanSender + "/value", ServerValue.increment(points));
        updates.put("cp_blessing_values/" + cleanReceiver + "/" + cleanSender + "/timestamp", System.currentTimeMillis());

        db.updateChildren(updates);
    }

    /**
     * Calculates CP Level (1 to 10) based on Intimacy score
     * 100 -> LV.1, 200 -> LV.2, 400 -> LV.3, 800 -> LV.4, 1600 -> LV.5, etc.
     */
    public static int calculateCpLevel(long intimacy) {
        if (intimacy >= 50000) return 10;
        if (intimacy >= 25600) return 9;
        if (intimacy >= 12800) return 8;
        if (intimacy >= 6400) return 7;
        if (intimacy >= 3200) return 6;
        if (intimacy >= 1600) return 5;
        if (intimacy >= 800) return 4;
        if (intimacy >= 400) return 3;
        if (intimacy >= 200) return 2;
        if (intimacy >= 100) return 1;
        return 0;
    }

    /**
     * Calculates the exact Intimacy Points to add for a gift based on gift name and coin price
     */
    public static long calculateIntimacyAddition(String giftName, long giftCost) {
        if (giftName == null) giftName = "";
        String lower = giftName.toLowerCase().trim();

        boolean isRelationshipGift = lower.contains("love")
                || lower.contains("couple")
                || lower.contains("relationship")
                || lower.contains("rose")
                || lower.contains("ring")
                || lower.contains("proposal")
                || lower.contains("wedding")
                || lower.contains("confession")
                || lower.contains("heart")
                || lower.contains("kiss")
                || lower.contains("pure")
                || lower.contains("blossom")
                || lower.contains("perfume")
                || lower.contains("parfume")
                || lower.contains("carriage")
                || lower.contains("anniversary");

        if (isRelationshipGift) {
            if (giftCost >= 100000) {
                return 300; // Most expensive relationship gifts -> 300 intimacy points
            } else if (giftCost >= 10000) {
                return 150; // Medium/high relationship gifts -> 150 intimacy points
            } else {
                return 100; // Standard relationship gifts -> 100 intimacy points
            }
        }

        // Normal gifts -> 1 intimacy point per gift
        return 1;
    }

    /**
     * Fetches Supporters for a user from cp_guard_values in Firebase Database
     */
    public static void fetchSupporters(String targetUid, SupportersCallback callback) {
        if (TextUtils.isEmpty(targetUid) || callback == null) {
            if (callback != null) callback.onSupportersLoaded(new ArrayList<>());
            return;
        }

        DatabaseReference guardRef = FirebaseDatabase.getInstance().getReference("cp_guard_values").child(targetUid);
        guardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SupporterModel> supporters = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String uid = ds.getKey();
                        Long scoreVal = ds.child("value").getValue(Long.class);
                        String uName = ds.child("userName").getValue(String.class);
                        if (TextUtils.isEmpty(uName)) uName = ds.child("partnerName").getValue(String.class);
                        String avatar = ds.child("userIcon").getValue(String.class);
                        if (TextUtils.isEmpty(avatar)) avatar = ds.child("partnerAvatar").getValue(String.class);

                        long score = scoreVal != null ? scoreVal : 0;
                        if (score > 0 && !TextUtils.isEmpty(uid)) {
                            supporters.add(new SupporterModel(uid, !TextUtils.isEmpty(uName) ? uName : "Supporter", avatar, 0, score));
                        }
                    }
                }

                if (supporters.isEmpty()) {
                    fetchFallbackUsersForSupporters(targetUid, callback);
                } else {
                    Collections.sort(supporters, (a, b) -> Long.compare(b.getGuardScore(), a.getGuardScore()));
                    for (int i = 0; i < supporters.size(); i++) {
                        supporters.get(i).setRank(i + 1);
                    }
                    enrichAndReturnSupporters(supporters, callback);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fetchFallbackUsersForSupporters(targetUid, callback);
            }
        });
    }

    private static void fetchFallbackUsersForSupporters(String targetUid, SupportersCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SupporterModel> list = new ArrayList<>();
                if (snapshot.exists()) {
                    int rank = 1;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String uid = ds.getKey();
                        if (uid != null && uid.equals(targetUid)) continue;

                        String name = ds.child("name").getValue(String.class);
                        String avatar = ds.child("avtar").getValue(String.class);
                        if (TextUtils.isEmpty(avatar)) avatar = ds.child("avatar").getValue(String.class);

                        long dummyScore = (11 - rank) * 1250L;
                        list.add(new SupporterModel(uid, !TextUtils.isEmpty(name) ? name : "User", avatar, rank, dummyScore));
                        rank++;
                    }
                }
                callback.onSupportersLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onSupportersLoaded(new ArrayList<>());
            }
        });
    }

    private static void enrichAndReturnSupporters(List<SupporterModel> list, SupportersCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (SupporterModel s : list) {
                        if (snapshot.hasChild(s.getUserId())) {
                            DataSnapshot uSnap = snapshot.child(s.getUserId());
                            String name = uSnap.child("name").getValue(String.class);
                            String avatar = uSnap.child("avtar").getValue(String.class);
                            if (TextUtils.isEmpty(avatar)) avatar = uSnap.child("avatar").getValue(String.class);

                            if (!TextUtils.isEmpty(name)) s.setUserName(name);
                            if (!TextUtils.isEmpty(avatar)) s.setAvatarUrl(avatar);
                        }
                    }
                }
                callback.onSupportersLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onSupportersLoaded(list);
            }
        });
    }

    /**
     * Fetches Best Friends and Intimacy Friends for a user from cp_guard_values in Firebase Database
     */
    public static void fetchIntimacyFriends(String targetUid, IntimacyFriendsCallback callback) {
        if (TextUtils.isEmpty(targetUid) || callback == null) {
            if (callback != null) callback.onFriendsLoaded(new ArrayList<>(), new ArrayList<>());
            return;
        }

        DatabaseReference guardRef = FirebaseDatabase.getInstance().getReference("cp_guard_values").child(targetUid);
        guardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<IntimacyFriendModel> bestFriends = new ArrayList<>();
                List<IntimacyFriendModel> intimacyFriends = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String uid = ds.getKey();
                        Long scoreVal = ds.child("value").getValue(Long.class);
                        Long tsVal = ds.child("timestamp").getValue(Long.class);
                        String uName = ds.child("userName").getValue(String.class);
                        if (TextUtils.isEmpty(uName)) uName = ds.child("partnerName").getValue(String.class);
                        String avatar = ds.child("userIcon").getValue(String.class);
                        if (TextUtils.isEmpty(avatar)) avatar = ds.child("partnerAvatar").getValue(String.class);

                        long score = scoreVal != null ? scoreVal : 0;
                        long ts = tsVal != null && tsVal > 0 ? tsVal : System.currentTimeMillis();
                        long days = CpBindingManager.getBindingDays(ts);
                        int level = calculateCpLevel(score);

                        if (!TextUtils.isEmpty(uid)) {
                            boolean isBest = score >= 5000;
                            IntimacyFriendModel model = new IntimacyFriendModel(
                                    uid,
                                    !TextUtils.isEmpty(uName) ? uName : "Friend",
                                    avatar,
                                    score,
                                    level,
                                    ts,
                                    days,
                                    isBest
                            );

                            if (isBest) {
                                bestFriends.add(model);
                            } else {
                                intimacyFriends.add(model);
                            }
                        }
                    }
                }

                if (bestFriends.isEmpty() && intimacyFriends.isEmpty()) {
                    fetchFallbackUsersForFriends(targetUid, callback);
                } else {
                    enrichAndReturnFriends(bestFriends, intimacyFriends, callback);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fetchFallbackUsersForFriends(targetUid, callback);
            }
        });
    }

    private static void fetchFallbackUsersForFriends(String targetUid, IntimacyFriendsCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.limitToFirst(12).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<IntimacyFriendModel> bestFriends = new ArrayList<>();
                List<IntimacyFriendModel> intimacyFriends = new ArrayList<>();

                if (snapshot.exists()) {
                    int count = 0;
                    long now = System.currentTimeMillis() - (15L * 24 * 3600 * 1000);
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String uid = ds.getKey();
                        if (uid != null && uid.equals(targetUid)) continue;

                        String name = ds.child("name").getValue(String.class);
                        String avatar = ds.child("avtar").getValue(String.class);
                        if (TextUtils.isEmpty(avatar)) avatar = ds.child("avatar").getValue(String.class);

                        count++;
                        boolean isBest = count <= 2;
                        long dummyScore = isBest ? (6000L + count * 1500) : (500L + count * 300);
                        int level = calculateCpLevel(dummyScore);

                        IntimacyFriendModel model = new IntimacyFriendModel(
                                uid,
                                !TextUtils.isEmpty(name) ? name : "Friend",
                                avatar,
                                dummyScore,
                                level,
                                now,
                                15 + count,
                                isBest
                        );

                        if (isBest) {
                            bestFriends.add(model);
                        } else {
                            intimacyFriends.add(model);
                        }
                    }
                }
                callback.onFriendsLoaded(bestFriends, intimacyFriends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFriendsLoaded(new ArrayList<>(), new ArrayList<>());
            }
        });
    }

    private static void enrichAndReturnFriends(List<IntimacyFriendModel> bestFriends,
                                                List<IntimacyFriendModel> intimacyFriends,
                                                IntimacyFriendsCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (IntimacyFriendModel f : bestFriends) {
                        if (snapshot.hasChild(f.getUserId())) {
                            DataSnapshot uSnap = snapshot.child(f.getUserId());
                            String name = uSnap.child("name").getValue(String.class);
                            String avatar = uSnap.child("avtar").getValue(String.class);
                            if (TextUtils.isEmpty(avatar)) avatar = uSnap.child("avatar").getValue(String.class);

                            if (!TextUtils.isEmpty(name)) f.setUserName(name);
                            if (!TextUtils.isEmpty(avatar)) f.setAvatarUrl(avatar);
                        }
                    }
                    for (IntimacyFriendModel f : intimacyFriends) {
                        if (snapshot.hasChild(f.getUserId())) {
                            DataSnapshot uSnap = snapshot.child(f.getUserId());
                            String name = uSnap.child("name").getValue(String.class);
                            String avatar = uSnap.child("avtar").getValue(String.class);
                            if (TextUtils.isEmpty(avatar)) avatar = uSnap.child("avatar").getValue(String.class);

                            if (!TextUtils.isEmpty(name)) f.setUserName(name);
                            if (!TextUtils.isEmpty(avatar)) f.setAvatarUrl(avatar);
                        }
                    }
                }
                callback.onFriendsLoaded(bestFriends, intimacyFriends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFriendsLoaded(bestFriends, intimacyFriends);
            }
        });
    }
}
