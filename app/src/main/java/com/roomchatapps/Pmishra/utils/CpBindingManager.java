package com.roomchatapps.Pmishra.utils;

import android.text.TextUtils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CpBindingManager {

    /**
     * Calculates days elapsed since the CP binding timestamp
     */
    public static long getBindingDays(long timestamp) {
        if (timestamp <= 0) {
            return 1;
        }

        long diffMillis = System.currentTimeMillis() - timestamp;
        if (diffMillis <= 0) {
            return 1;
        }

        long days = (diffMillis / (1000L * 60 * 60 * 24)) + 1;
        return Math.max(1, days);
    }

    /**
     * Creates bidirectional CP binding records in Firebase Realtime Database
     */
    public static void createCpBinding(String uid1, String name1, String avatar1,
                                       String uid2, String name2, String avatar2) {
        if (TextUtils.isEmpty(uid1) || TextUtils.isEmpty(uid2)) {
            return;
        }

        long now = System.currentTimeMillis();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("cp_bindings");

        Map<String, Object> data1 = new HashMap<>();
        data1.put("partnerUid", uid2);
        data1.put("partnerName", !TextUtils.isEmpty(name2) ? name2 : "Partner");
        data1.put("partnerAvatar", !TextUtils.isEmpty(avatar2) ? avatar2 : "");
        data1.put("timestamp", now);

        Map<String, Object> data2 = new HashMap<>();
        data2.put("partnerUid", uid1);
        data2.put("partnerName", !TextUtils.isEmpty(name1) ? name1 : "Partner");
        data2.put("partnerAvatar", !TextUtils.isEmpty(avatar1) ? avatar1 : "");
        data2.put("timestamp", now);

        db.child(uid1).setValue(data1);
        db.child(uid2).setValue(data2);
    }
}
