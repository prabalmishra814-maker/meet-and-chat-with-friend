package com.roomchatapps.Pmishra.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.NotificationModel;
import com.roomchatapps.Pmishra.utils.SystemNotificationManager;

public class AppNotificationService extends Service {

    private DatabaseReference userNotifRef;
    private ValueEventListener notifEventListener;
    private final long serviceStartTime = System.currentTimeMillis();

    @Override
    public void onCreate() {
        super.onCreate();
        SystemNotificationManager.createNotificationChannel(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupPersistentNotificationListener();
        return START_STICKY;
    }

    private void setupPersistentNotificationListener() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null || currentUid.trim().isEmpty()) return;

        if (userNotifRef != null && notifEventListener != null) {
            userNotifRef.removeEventListener(notifEventListener);
        }

        userNotifRef = FirebaseDatabase.getInstance().getReference("notifications").child(currentUid);
        notifEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    NotificationModel notif = ds.getValue(NotificationModel.class);
                    if (notif != null && notif.getTimestamp() >= serviceStartTime - 5000) {
                        if (!notif.isRead()) {
                            SystemNotificationManager.showSystemNotification(
                                    getApplicationContext(),
                                    notif.getTitle() != null ? notif.getTitle() : "Notification 🔔",
                                    notif.getMessage() != null ? notif.getMessage() : "New update received",
                                    notif.getType() != null ? notif.getType() : "SYSTEM",
                                    notif.getSenderId(),
                                    notif.getSenderName(),
                                    notif.getTargetId()
                            );
                            // Mark as read in Firebase so notification fires only once
                            ds.getRef().child("read").setValue(true);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        userNotifRef.addValueEventListener(notifEventListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (userNotifRef != null && notifEventListener != null) {
            userNotifRef.removeEventListener(notifEventListener);
        }
    }
}
