package com.roomchatapps.Pmishra.utils;

import android.text.TextUtils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class CpIntimacyManager {

    /**
     * Atomically adds intimacy points between CP partners in Firebase Realtime Database
     */
    public static void addIntimacy(String senderUid, String receiverUid, long points) {
        if (TextUtils.isEmpty(senderUid) || TextUtils.isEmpty(receiverUid) || points <= 0) {
            return;
        }

        String cleanSender = senderUid.trim();
        String cleanReceiver = receiverUid.trim();

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
     */
    public static int calculateCpLevel(long intimacy) {
        if (intimacy >= 300000) return 10;
        if (intimacy >= 230000) return 9;
        if (intimacy >= 170000) return 8;
        if (intimacy >= 120000) return 7;
        if (intimacy >= 80000) return 6;
        if (intimacy >= 50000) return 5;
        if (intimacy >= 30000) return 4;
        if (intimacy >= 15000) return 3;
        if (intimacy >= 5000) return 2;
        return 1;
    }
}
