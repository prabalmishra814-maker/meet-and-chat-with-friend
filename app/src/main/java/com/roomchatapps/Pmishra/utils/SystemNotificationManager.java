package com.roomchatapps.Pmishra.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.roomchatapps.Pmishra.ChatActivity;
import com.roomchatapps.Pmishra.NotificationActivity;
import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.UserDetailActivity;

public class SystemNotificationManager {

    public static final String CHANNEL_ID = "room_chat_system_channel";
    public static final String CHANNEL_NAME = "Room Chat System Notifications";

    /**
     * Initializes the Android NotificationChannel for API 26+ (Android 8.0+).
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for direct messages, followers, and room updates");
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Displays a native Android System Notification in the top status bar tray.
     */
    public static void showSystemNotification(Context context, String title, String message, String type, String senderId, String senderName, String targetId) {
        if (context == null) return;

        createNotificationChannel(context);

        Intent intent;
        if ("MESSAGE".equalsIgnoreCase(type) && senderId != null && !senderId.isEmpty()) {
            intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiverId", senderId);
            intent.putExtra("receiverName", senderName != null ? senderName : "User");
        } else if ("FOLLOW".equalsIgnoreCase(type) && senderId != null && !senderId.isEmpty()) {
            intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra("targetUid", senderId);
        } else {
            intent = new Intent(context, NotificationActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        int uniqueNotifId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, uniqueNotifId, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.img_20260904_135725)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify(uniqueNotifId, builder.build());
        } catch (SecurityException ignored) {
            // Handled if runtime POST_NOTIFICATIONS permission is pending
        } catch (Exception ignored) {}
    }
}
