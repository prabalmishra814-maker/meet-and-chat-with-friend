package com.roomchatapps.Pmishra.utils;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.models.NotificationModel;

public class NotificationHelper {

    public static void sendNotification(String targetUserId, String title, String message, String type, String targetId) {
        if (targetUserId == null || targetUserId.isEmpty()) return;

        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null || currentUid.equals(targetUserId)) return; // Don't send notification to self

        // Fetch sender details from Firebase
        FirebaseDatabase.getInstance().getReference("users").child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String senderName = "A User";
                        String senderAvatar = "";
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String avatar = snapshot.child("avtar").getValue(String.class);
                            if (name != null && !name.isEmpty()) senderName = name;
                            if (avatar != null && !avatar.isEmpty()) senderAvatar = avatar;
                        }

                        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                                .getReference("notifications")
                                .child(targetUserId);

                        String notifId = notifRef.push().getKey();
                        if (notifId == null) return;

                        NotificationModel notification = new NotificationModel(
                                notifId,
                                title,
                                message,
                                type,
                                currentUid,
                                senderName,
                                senderAvatar,
                                System.currentTimeMillis(),
                                false,
                                targetId
                        );

                        notifRef.child(notifId).setValue(notification);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    public static void sendMessageNotification(String targetUserId, String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) messageText = "Sent a message";
        sendNotification(targetUserId, "New Message 💬", messageText, "MESSAGE", targetUserId);
    }

    public static void sendGiftNotification(String targetUserId, String giftName) {
        sendNotification(targetUserId, "New Gift Received 🎁", "Sent you a " + giftName + "!", "GIFT", null);
    }

    public static void sendFollowNotification(String targetUserId) {
        sendNotification(targetUserId, "New Follower 👤", "Started following you.", "FOLLOW", targetUserId);
    }

    public static void sendCommentNotification(String targetUserId, String postId) {
        sendNotification(targetUserId, "New Comment 💬", "Commented on your post.", "COMMENT", postId);
    }

    public static void sendRoomInviteNotification(String targetUserId, String roomId) {
        sendNotification(targetUserId, "Room Party Invitation 🎙️", "Invited you to join an audio room!", "ROOM_INVITE", roomId);
    }

    public static void sendSystemNotification(String targetUserId, String title, String message) {
        sendNotification(targetUserId, title, message, "SYSTEM", null);
    }
}
