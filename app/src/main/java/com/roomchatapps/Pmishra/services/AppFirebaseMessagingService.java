package com.roomchatapps.Pmishra.services;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.roomchatapps.Pmishra.utils.SystemNotificationManager;

import java.util.Map;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid != null && !currentUid.trim().isEmpty()) {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUid)
                    .child("fcmToken")
                    .setValue(token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "Room Chat Notification 🔔";
        String body = "You have a new update";
        String type = "SYSTEM";
        String senderId = "";
        String senderName = "User";

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        Map<String, String> data = remoteMessage.getData();
        if (data != null && !data.isEmpty()) {
            if (data.containsKey("title")) title = data.get("title");
            if (data.containsKey("body")) body = data.get("body");
            if (data.containsKey("type")) type = data.get("type");
            if (data.containsKey("senderId")) senderId = data.get("senderId");
            if (data.containsKey("senderName")) senderName = data.get("senderName");
        }

        SystemNotificationManager.showSystemNotification(
                getApplicationContext(),
                title,
                body,
                type,
                senderId,
                senderName,
                null
        );
    }
}
