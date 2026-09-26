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
        public String equippedFrame;
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
                    profile.equippedFrame = snapshot.child("equipped_frame").getValue(String.class);
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

    public static void invalidate(String uid) {
        if (uid != null) cache.remove(uid.trim());
    }

    public static void clear() {
        cache.clear();
    }
}
