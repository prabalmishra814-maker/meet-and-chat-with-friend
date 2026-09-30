package com.roomchatapps.Pmishra.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserProfileCache {

    public static class UserProfile {
        public String uid;
        public String name;
        public String avatarUrl;
        public String equippedFrame = "";
        public long coinsSpent = 0;
        public long energy = 0;
        public long level = 1;
        public long totalXp = 0;
        public int currentLevelXp = 0;
    }

    public interface Callback {
        void onLoaded(UserProfile profile);
    }

    private static final Map<String, UserProfile> cache = new ConcurrentHashMap<>();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static void postToMain(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

    public static void getUserProfile(String uid, Callback callback) {
        if (uid == null || uid.trim().isEmpty() || callback == null) return;

        String cleanUid = uid.trim();
        if (cache.containsKey(cleanUid)) {
            postToMain(() -> callback.onLoaded(cache.get(cleanUid)));
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(cleanUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile profile = new UserProfile();
                profile.uid = cleanUid;
                if (snapshot.exists()) {
                    profile.name = snapshot.child("name").getValue(String.class);
                    String avatar = snapshot.child("avtar").getValue(String.class);
                    if (avatar == null || avatar.trim().isEmpty()) {
                        avatar = snapshot.child("avatar").getValue(String.class);
                    }
                    if (avatar == null || avatar.trim().isEmpty()) {
                        avatar = snapshot.child("userIcon").getValue(String.class);
                    }
                    if (avatar == null || avatar.trim().isEmpty()) {
                        avatar = snapshot.child("photoUrl").getValue(String.class);
                    }
                    if (avatar == null || avatar.trim().isEmpty()) {
                        avatar = snapshot.child("image").getValue(String.class);
                    }
                    profile.avatarUrl = avatar;

                    String frame = snapshot.child("equipped_frame").getValue(String.class);
                    if (frame == null || frame.trim().isEmpty()) {
                        frame = snapshot.child("equippedFrame").getValue(String.class);
                    }
                    if (frame == null || frame.trim().isEmpty()) {
                        frame = snapshot.child("frame").getValue(String.class);
                    }
                    if (frame == null || frame.trim().isEmpty()) {
                        frame = snapshot.child("frameId").getValue(String.class);
                    }
                    profile.equippedFrame = (frame != null) ? frame.trim() : "";

                    long spent = 0;
                    if (snapshot.child("coinsSpent").exists()) {
                        try {
                            spent = Long.parseLong(String.valueOf(snapshot.child("coinsSpent").getValue()));
                        } catch (Exception ignored) {}
                    } else if (snapshot.child("level").exists()) {
                        try {
                            long lvl = Long.parseLong(String.valueOf(snapshot.child("level").getValue()));
                            spent = Math.max(0, (lvl - 1) * LevelUtils.COINS_PER_LEVEL);
                        } catch (Exception ignored) {}
                    }

                    profile.coinsSpent = spent;
                    if (snapshot.child("energy").exists() && snapshot.child("energy").getValue() != null) {
                        try {
                            profile.energy = Long.parseLong(String.valueOf(snapshot.child("energy").getValue()));
                        } catch (Exception ignored) {}
                    }
                    profile.level = LevelUtils.calculateLevel(spent);
                    profile.totalXp = LevelUtils.calculateTotalXp(spent);
                    profile.currentLevelXp = LevelUtils.calculateCurrentXpInLevel(spent);
                }
                cache.put(cleanUid, profile);
                postToMain(() -> callback.onLoaded(profile));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                UserProfile fallback = new UserProfile();
                fallback.uid = cleanUid;
                postToMain(() -> callback.onLoaded(fallback));
            }
        });
    }

    public static UserProfile getDirectCachedProfile(String uid) {
        if (uid == null || uid.trim().isEmpty()) return null;
        return cache.get(uid.trim());
    }

    public static void invalidate(String uid) {
        if (uid != null) cache.remove(uid.trim());
    }

    public static void clear() {
        cache.clear();
    }
}
