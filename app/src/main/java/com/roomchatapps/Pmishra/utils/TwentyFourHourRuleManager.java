package com.roomchatapps.Pmishra.utils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TwentyFourHourRuleManager {

    /**
     * Configurable flag to enable or disable the 24-hour rule restriction
     */
    public static boolean ENABLE_24_HOUR_RULE = false;

    public static final long TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000L;

    public interface RuleCallback {
        void onCheckCompleted(boolean isEligible, long remainingTimeMs);
    }

    /**
     * Checks if 24 hours have elapsed since the user's last recorded room event.
     */
    public static void check24HourRule(String uid, String roomId, RuleCallback callback) {
        if (!ENABLE_24_HOUR_RULE) {
            if (callback != null) callback.onCheckCompleted(true, 0);
            return;
        }

        if (uid == null || uid.isEmpty()) {
            if (callback != null) callback.onCheckCompleted(true, 0);
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("last_room_rejoin")
                .child(roomId != null ? roomId : "default");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long lastTimeObj = snapshot.getValue(Long.class);
                    long lastTime = lastTimeObj != null ? lastTimeObj : 0;
                    long now = System.currentTimeMillis();
                    long elapsed = now - lastTime;

                    if (elapsed >= TWENTY_FOUR_HOURS_MS) {
                        if (callback != null) callback.onCheckCompleted(true, 0);
                    } else {
                        long remaining = TWENTY_FOUR_HOURS_MS - elapsed;
                        if (callback != null) callback.onCheckCompleted(false, remaining);
                    }
                } else {
                    if (callback != null) callback.onCheckCompleted(true, 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onCheckCompleted(true, 0);
            }
        });
    }

    /**
     * Updates the timestamp of the last room event for the user.
     */
    public static void record24HourEvent(String uid, String roomId) {
        if (uid == null || uid.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("last_room_rejoin")
                .child(roomId != null ? roomId : "default");
        ref.setValue(System.currentTimeMillis());
    }
}
